"""Build individual-block blueprints and Blockbench MCP operations for three Nexus machines.

Art/design assets only. The production controllers, recipes and native MI integration
remain a separate implementation step; assembly previews are never runtime megameshes.
"""
from pathlib import Path
from collections import Counter, deque
import io, json, math
from PIL import Image, ImageDraw
from prepare_nexus_assets import TILES as PORTAL_TILES

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/nexus-machines'
WORK=ROOT/'.local/nexus-machines'
EXTRA=['pressure_plate','hydraulic_cylinder','pressure_tank','mould_bed','molten','gantry_rail','bath_clean','bath_plate','bath_rinse','electrode','magnet_coil','accelerator_glass','injector_housing','control_casting','control_plating','control_collider','beam_cyan','beam_violet']
TILES=[*PORTAL_TILES,*EXTRA]
PARTS={}

def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8',newline='\n')

def paint():
    atlas=Image.open(ROOT/'art/blockbench/nexus-portal/textures/nexus_atlas.png').convert('RGBA')
    for name in EXTRA:
        im=Image.new('RGBA',(16,16),'#363e4a');d=ImageDraw.Draw(im)
        d.rectangle((1,1,14,14),outline='#7a8793');d.line((14,2,14,14),fill='#171f29')
        if name=='pressure_plate':
            d.rectangle((3,3,12,12),fill='#8a9097');d.rectangle((5,5,10,10),fill='#626d79')
            for x,y in [(2,2),(13,2),(2,13),(13,13)]:d.point((x,y),fill='#c4cad0')
        elif name=='hydraulic_cylinder':
            d.rectangle((5,0,10,15),fill='#a7b0b8');d.rectangle((7,0,8,15),fill='#d4dade')
            for yy in [2,12]:d.rectangle((2,yy,13,yy+1),fill='#4b5e70')
        elif name=='pressure_tank':
            for xx in [3,12]:d.line((xx,1,xx,14),fill='#8a9ba8')
            d.rectangle((5,4,10,10),fill='#17212e');d.rectangle((6,5,9,8),fill='#b3ccd5')
            d.line((8,8,9,6),fill='#c15d43')
        elif name=='mould_bed':
            d.rectangle((3,3,12,12),fill='#111923');d.rectangle((5,5,10,10),fill='#c37b34')
            d.rectangle((6,6,9,9),fill='#332c2b')
        elif name=='molten':
            d.rectangle((0,0,15,15),fill='#d35d22');d.rectangle((2,3,12,12),fill='#f29c34')
            for xx,yy in [(3,4),(8,3),(6,9),(11,11),(2,12)]:d.rectangle((xx,yy,xx+2,yy+1),fill='#ffe080')
        elif name=='gantry_rail':
            d.rectangle((4,0,11,15),fill='#141e2b');d.line((4,0,4,15),fill='#d5a73c');d.line((10,0,10,15),fill='#a6b5be')
        elif name.startswith('bath_'):
            palette={'bath_clean':('#285f67','#70c2b4'),'bath_plate':('#9a612e','#e2ad64'),'bath_rinse':('#365f87','#8cc9e0')}[name]
            d.rectangle((0,0,15,15),fill=palette[0])
            for x,y in [(2,3),(7,6),(3,12),(10,11)]:d.line((x,y,x+3,y),fill=palette[1])
        elif name=='electrode':
            d.rectangle((4,0,11,15),fill='#a97140');d.line((4,0,4,15),fill='#e0bc7e')
            for yy in [3,7,11]:d.line((5,yy,10,yy),fill='#654831')
        elif name=='magnet_coil':
            d.rectangle((2,2,13,13),fill='#274c5e')
            for yy in [3,5,7,9,11]:d.line((3,yy,12,yy),fill='#78aebb');d.line((3,yy+1,12,yy+1),fill='#203743')
            d.rectangle((0,6,2,9),fill='#adbac2');d.rectangle((13,6,15,9),fill='#adbac2')
        elif name=='accelerator_glass':
            im=Image.new('RGBA',(16,16),(38,70,87,70));d=ImageDraw.Draw(im)
            d.rectangle((0,0,15,15),outline=(126,167,184,220));d.line((2,5,5,2),fill=(176,221,234,140))
        elif name=='injector_housing':
            d.rectangle((2,2,13,13),fill='#202d3b');d.rectangle((4,4,11,11),fill='#8da5b8');d.rectangle((6,5,9,10),fill='#73c4d8')
            d.line((6,7,9,7),fill='#e6f8ff')
        elif name.startswith('control_'):
            d.rectangle((2,3,13,11),fill='#101c2a');d.rectangle((3,4,12,5),fill='#b5d9e5')
            color={'control_casting':'#edab51','control_plating':'#71c7b4','control_collider':'#b486dd'}[name]
            for xx in [4,7,10]:d.rectangle((xx,7,xx+1,9),fill=color)
        elif name.startswith('beam_'):
            d.rectangle((0,0,15,15),fill='#31aacb' if name=='beam_cyan' else '#8d49ce')
            d.rectangle((3,3,12,12),fill='#97e5f0' if name=='beam_cyan' else '#d3a9f8')
            d.rectangle((6,6,9,9),fill='#e4fbff')
        idx=TILES.index(name);atlas.paste(im,((idx%16)*16,(idx//16)*16))
    (ART/'textures').mkdir(parents=True,exist_ok=True);atlas.save(ART/'textures/nexus_machine_atlas.png');atlas.save(ART/'textures/nexus_machine_effects.png')

def faces(tile,front=None,facing='north'):
    def uv(name):i=TILES.index(name);return [(i%16)*16,(i//16)*16,(i%16)*16+16,(i//16)*16+16]
    texture='nexus_machine_effects' if tile in ['molten','beam_cyan','beam_violet'] else 'nexus_machine_atlas'
    return {s:{'uv':uv(front if front and s==facing else tile),'texture':texture} for s in ['north','south','east','west','up','down']}

def cube(name,a,b,tile,parent,front=None,facing='north',**extra):
    return {'op':'add_cube','name':name,'from':a,'to':b,'parent':parent,'box_uv':False,'faces':faces(tile,front,facing),**extra}

class Machine:
    def __init__(self,name,label,width=9,depth=9):
        self.name=name;self.label=label;self.width=width;self.depth=depth;self.cells={};self.visuals=[];self.anim=[];self.required_air=[]
    def put(self,x,y,z,part,section='structure',role=None,facing='north'):
        assert 0<=x<self.width and 0<=y<5 and 0<=z<self.depth
        self.cells[x,y,z]={'position':[x,y,z],'part':part,'section':section,'facing':facing,**({'role':role} if role else {})}
    def port(self,x,y,z,part,role,facing):self.put(x,y,z,part,'hatches',role,facing)
    def base(self):
        for x in range(9):
            for z in range(9):
                tile='hazard' if (x in [0,8] and z in [0,8]) else 'deck_grate' if x in [1,7] or z in [1,7] else 'casing'
                self.put(x,0,z,tile,'base')
    def group(self,name,origin):self.visuals.append({'op':'add_group','name':name,'origin':origin})
    def detail(self,name,a,b,tile,parent,**extra):self.visuals.append(cube(name,a,b,tile,parent,**extra))
    def animation(self,name,length,channels):
        self.anim.append({'op':'add_animation','name':name,'length':length,'loop':'loop','snapping':24})
        for bone,channel,keys in channels:
            for t,v in keys:self.anim.append({'op':'set_keyframe','animation':name,'bone':bone,'channel':channel,'time':t,'value':v,'interpolation':'linear'})
    def finish(self):
        sections=sorted({c['section'] for c in self.cells.values()})
        ops=[{'op':'add_group','name':s,'origin':[0,0,0]} for s in sections]
        for c in sorted(self.cells.values(),key=lambda c:(c['position'][1],c['position'][2],c['position'][0])):
            p=c['position'];part=c['part'];front=part if c['section']=='hatches' else None
            tile='ring' if front else part
            name=part+' @ '+','.join(map(str,p))
            if part in ['mould_bed','hydraulic_cylinder','bath_wall']:
                if part=='bath_wall':pieces=[('base',[0,0,0],[16,4,16],'ceramic_panel'),('window',[0,4,0],[16,16,16],'accelerator_glass')]
                else:
                    shape=[('bed',[0,0,0],[16,5,16]),('rim_0',[0,5,0],[3,10,16]),('rim_1',[13,5,0],[16,10,16]),('front',[3,5,0],[13,10,3]),('back',[3,5,13],[13,10,16])] if part=='mould_bed' else [('base',[3,0,3],[13,2,13]),('barrel',[4,2,4],[12,14,12]),('cap',[3,14,3],[13,16,13])]
                    pieces=[(s,a,b,part) for s,a,b in shape]
                for suffix,a,b,material in pieces:ops.append(cube(name+' '+suffix,[p[i]*16+a[i] for i in range(3)],[p[i]*16+b[i] for i in range(3)],material,c['section']))
                PARTS.setdefault(part,[cube(part+' '+s,a,b,t,part) for s,a,b,t in pieces])
            else:
                ops.append(cube(name,[v*16 for v in p],[v*16+16 for v in p],tile,c['section'],front,c['facing']))
                PARTS.setdefault(part,[cube(part,[0,0,0],[16,16,16],tile,part,front,'north')])
        ops+=self.visuals+self.anim
        counts=dict(sorted(Counter(c['part'] for c in self.cells.values()).items()))
        data={'status':'ART_AND_DESIGN_ONLY_NOT_REGISTERED','name':self.label,'dimensions':{'width':self.width,'height':5,'depth':self.depth},'placedBlocks':len(self.cells),'shape':'hollow toroidal accelerator with open centre' if self.width==21 else 'individual-block industrial frame','counts':counts,'cells':sorted(self.cells.values(),key=lambda c:(c['position'][1],c['position'][2],c['position'][0])),'requiredAir':self.required_air,'hatches':[c for c in self.cells.values() if c['section']=='hatches'],'visualGroups':[o['name'] for o in self.visuals if o['op']=='add_group'],'animations':[o['name'] for o in self.anim if o['op']=='add_animation'],'runtimeHatches':'Use existing native MI ItemHatch / fluid / energy implementations and matched casing behavior; preview overlays are not new hatch logic.'}
        write(ART/'blueprints'/f'{self.name}.json',data);write(WORK/f'{self.name}-operations.json',ops)
        return {'machine':self.name,'dimensions':data['dimensions'],'blocks':len(self.cells),'hatches':len(data['hatches']),'operations':len(ops)}

def casting():
    m=Machine('pressure_casting_plant','Pressure Casting Plant');m.base()
    for x in [2,6]:
        for z in [2,6]:
            for y in [1,2,3]:m.put(x,y,z,'reinforced_column','press_frame')
    for x in range(2,7):
        for z in range(2,7):
            if x in [2,6] or z in [2,6] or x==4 or z==4:m.put(x,4,z,'pressure_plate','press_frame')
    for x in range(3,6):
        for z in range(3,6):m.put(x,1,z,'mould_bed','mould_table')
    for y in [2,3]:m.put(4,y,4,'hydraulic_cylinder','hydraulics')
    for x in [2,6]:
        for y in [1,2,3]:m.put(x,y,7,'pressure_tank','pressure_storage')
    for x in [1,7]:
        for z in range(2,7):m.put(x,1,z,'ribbed_panel','cooling_manifold')
    for x in range(2,7):m.put(x,1,8,'service_panel','service_panel')
    for x in [0,8]:
        for z in [1,3,5,7]:m.put(x,1,z,'casing','service_panel')
    for x in [2,6]:m.put(x,3,2,'light_amber','press_frame')
    m.port(4,1,0,'control_casting','controller','north')
    for x,role in [(2,'mould_input'),(6,'mould_return')]:m.port(x,1,0,'input' if x==2 else 'output',role,'north')
    m.port(0,1,2,'input','alloy_feed','west');m.port(8,1,2,'output','finished_castings','east')
    m.port(0,1,6,'fluid_in','coolant_input','west');m.port(8,1,6,'fluid_out','warm_coolant_output','east')
    m.port(2,1,8,'fluid_in','pressure_gas_input','south');m.port(6,1,8,'energy','energy_input','south')
    m.group('press_platen',[72,48,72])
    m.detail('Moving press platen ONLY',[52,43,52],[92,47,92],'pressure_plate','press_platen')
    m.detail('Press warning edge',[52,43,51.5],[92,45,52],'hazard','press_platen')
    m.group('casting_in_mould',[72,27,72]);m.detail('Contained hot casting',[55,26,55],[89,28,89],'molten','casting_in_mould')
    m.animation('pressure_casting_cycle',8,[('press_platen','position',[(0,[0,0,0]),(2,[0,-15,0]),(4,[0,-15,0]),(6,[0,0,0]),(8,[0,0,0])])])
    return m

def plating():
    m=Machine('electroplating_line','Electroplating Line');m.base()
    for x in range(2,7):
        for z in range(1,8):m.put(x,1,z,'pressure_plate','bath_bases')
    for z in range(1,8):
        for x in [2,6]:m.put(x,2,z,'bath_wall','bath_walls')
    for z in [1,3,5,7]:
        for x in range(3,6):m.put(x,2,z,'bath_wall','bath_walls')
    for x in [1,7]:
        for z in [1,7]:
            for y in [1,2,3]:m.put(x,y,z,'reinforced_column','gantry_frame')
        for z in range(1,8):m.put(x,4,z,'gantry_rail','gantry_frame')
    for z in [1,7]:
        for x in range(2,7):m.put(x,4,z,'ribbed_panel','gantry_frame')
    for z,tile in [(2,'bath_clean'),(4,'bath_plate'),(6,'bath_rinse')]:
        m.group(tile,[72,40,z*16+8])
        m.detail(tile+' fluid volume',[48,33,z*16+.5],[96,43,z*16+15.5],tile,tile)
        m.required_air.extend([[x,2,z] for x in [3,4,5]])
        for x in [1,7]:m.put(x,1,z,'service_panel','bath_manifold')
    for x in range(2,7):m.put(x,1,8,'vent','service_cabinet')
    m.port(2,1,0,'input','substrate_input','north');m.port(4,1,0,'control_plating','controller','north');m.port(6,1,0,'output','plated_output','north')
    for z,role in [(2,'cleaning'),(4,'plating'),(6,'rinse')]:
        m.port(0,1,z,'fluid_in',role+'_input','west');m.port(8,1,z,'fluid_out',role+'_waste_output','east')
    m.port(4,1,8,'energy','energy_input','south')
    m.put(4,3,7,'light_blue','gantry_frame')
    m.group('gantry_carriage',[72,64,40])
    m.detail('Rack transfer bridge',[24,58,35],[120,62,45],'gantry_rail','gantry_carriage')
    for x in [1.5,7.5]:m.detail('Gantry roller '+str(x),[x*16-3,62,36],[x*16+3,66,44],'pressure_plate','gantry_carriage')
    m.group('hoist_cables',[72,58,40])
    for x in [3.4,5.6]:m.detail('Immersion guide '+str(x),[x*16-.5,50,39.5],[x*16+.5,58,40.5],'electrode','hoist_cables')
    m.group('plating_rack',[72,56,40])
    for x in [3.4,4.5,5.6]:
        m.detail('Rack suspension '+str(x),[x*16-1,54,38],[x*16+1,58,42],'electrode','plating_rack')
        m.detail('Substrate '+str(x),[x*16-4,50,37],[x*16+4,58,43],'magnet_coil','plating_rack')
    keys=[(0,[0,0,0]),(1,[0,-16,0]),(3,[0,-16,0]),(4,[0,0,0]),(6,[0,0,32]),(7,[0,-16,32]),(9,[0,-16,32]),(10,[0,0,32]),(12,[0,0,64]),(13,[0,-16,64]),(15,[0,-16,64]),(16,[0,0,64]),(18,[0,0,0])]
    travel=[(t,[0,0,v[2]]) for t,v in keys]
    m.animation('three_bath_plating_cycle',18,[('plating_rack','position',keys),('gantry_carriage','position',travel),('hoist_cables','position',travel),('hoist_cables','scale',[(t,[1,1-v[1]/8,1]) for t,v in keys])])
    return m

def collider():
    m=Machine('particle_collider','Particle Collider',21,21)
    for x in range(21):
        for z in range(21):
            r=math.hypot(x-10,z-10);a=math.atan2(z-10,x-10)
            for y in range(5):
                cross=(r-8)**2+(y-2)**2
                if cross>2.5**2:continue
                if cross<=1.2**2:m.required_air.append([x,y,z]);continue
                coil=abs(math.sin(a*4))<.22
                tile='magnet_coil' if coil and y in [1,2,3] else 'reinforced_column' if y==0 else 'accelerator_glass' if (y==4 or y==2 and r>9.4) and not coil else 'ceramic_panel' if y==4 else 'casing'
                m.put(x,y,z,tile,'accelerator_ring')
    # Two opposed injector stations stay inside the requested 21 x 21 footprint.
    m.put(10,3,1,'injector_housing','injector_north',facing='north');m.put(10,3,19,'injector_housing','injector_south',facing='south')
    m.port(10,1,0,'control_collider','controller','north')
    for x,y,z,part,role,face in [(8,1,0,'input','left_beam_target','north'),(12,1,20,'input','right_beam_target','south'),(12,1,0,'input','essence_and_containers','north'),(8,1,20,'output','contained_product','south'),(0,1,8,'fluid_in','cryofluid_input','west'),(0,1,12,'fluid_in','plasma_input','west'),(20,1,12,'fluid_out','spent_coolant_output','east'),(20,1,8,'energy','energy_input','east')]:
        assert (x,y,z) in m.cells,(x,y,z)
        m.port(x,y,z,part,role,face)
    m.group('beam_track_visual',[168,40,168])
    for i in range(64):
        a=(i+.5)*math.tau/64;cx=168+math.cos(a)*128;cz=168+math.sin(a)*128;length=math.tau*128/64
        m.detail('Contained beam track '+str(i),[cx-length/2,39.6,cz-.4],[cx+length/2,40.4,cz+.4],'beam_cyan','beam_track_visual',origin=[cx,40,cz],rotation=[0,-math.degrees(a)-90,0])
    for name,z,tile in [('beam_packet_a',40,'beam_cyan'),('beam_packet_b',296,'beam_violet')]:
        m.group(name,[168,40,168]);m.detail(name+' contained pulse',[165,37,z-3],[171,43,z+3],tile,name)
        # A white-hot core and a short diminishing trail make motion readable through glass.
        core=m.visuals[-1]
        idx=TILES.index(tile);u=(idx%16)*16+7;v=(idx//16)*16+7
        core['faces']={s:{'uv':[u,v,u+1,v+1],'texture':'nexus_machine_effects'} for s in core['faces']}
        sign=1 if name=='beam_packet_a' else -1
        start=-math.pi/2 if name=='beam_packet_a' else math.pi/2
        for i in range(1,6):
            a=start+sign*i*.055;cx=168+128*math.cos(a);cz=168+128*math.sin(a);half=2.5-i*.3
            m.detail(name+' light trail '+str(i),[cx-half,40-half,cz-half],[cx+half,40+half,cz+half],tile,name)
        # Inspection-glass fluorescence carries the buried beam's light up to the visible roof.
        m.detail(name+' inspection glow',[162,78.8,z-6],[174,79.4,z+6],tile,name)
        m.visuals[-1]['faces']=core['faces']
        for i in range(1,6):
            a=start+sign*i*.055;cx=168+128*math.cos(a);cz=168+128*math.sin(a);half=4.5-i*.6
            m.detail(name+' inspection trail '+str(i),[cx-half,78.9,cz-half],[cx+half,79.3,cz+half],tile,name)
    m.animation('opposed_beam_cycle',12,[(name,'rotation',[(i*1.5,[0,sign*i*45,0]) for i in range(9)]) for name,sign in [('beam_packet_a',1),('beam_packet_b',-1)]])
    # Independently pivoted lenses pulse together; the structure itself never changes scale.
    flash=[]
    idx=TILES.index('beam_cyan');u=(idx%16)*16+7;v=(idx//16)*16+7
    for i in range(32):
        a=i*math.tau/32;cx=168+128*math.cos(a);cz=168+128*math.sin(a)
        name='completion_flash_'+str(i);flash.append(name);m.group(name,[cx,79.5,cz])
        m.detail('Flash lens '+str(i),[cx-11,79.2,cz-11],[cx+11,79.8,cz+11],'beam_cyan',name,origin=[cx,79.5,cz],rotation=[0,-math.degrees(a),0])
        m.visuals[-1]['faces']={s:{'uv':[u,v,u+1,v+1],'texture':'nexus_machine_effects'} for s in m.visuals[-1]['faces']}
    # Hide completion lenses throughout normal operation; flash once on a successful recipe.
    for name in flash:
        for t in [0,12]:m.anim.append({'op':'set_keyframe','animation':'opposed_beam_cycle','bone':name,'channel':'scale','time':t,'value':[0,0,0],'interpolation':'linear'})
    m.animation('completion_flash',1.2,[(name,'scale',[(0,[0,0,0]),(.10,[1,1,1]),(.18,[1,1,1]),(.75,[0,0,0]),(1.2,[0,0,0])]) for name in flash]+[(name,'scale',[(0,[0,0,0]),(1.2,[0,0,0])]) for name in ['beam_packet_a','beam_packet_b']])
    next(o for o in m.anim if o.get('op')=='add_animation' and o['name']=='completion_flash')['loop']='once'
    return m

def main():
    ART.mkdir(parents=True,exist_ok=True);WORK.mkdir(parents=True,exist_ok=True);paint()
    machines=[casting(),plating(),collider()];summary=[m.finish() for m in machines]
    for name,ops in PARTS.items():write(WORK/f'part-{name}.json',[{'op':'add_group','name':name,'origin':[8,8,8]},*ops])
    write(ART/'machine-index.json',{'status':'art and design; gameplay implementation pending','machines':summary,'parts':sorted(PARTS),'atlas':'textures/nexus_machine_atlas.png','colliderShape':'donut / hollow torus; earlier cone proposal superseded'})
    (ART/'CREDITS.txt').write_text('Base Nexus artwork: Revo project. Native steel and hatch overlays: Modern Industrialization 2.5.11, AztechMC / contributors, inherited from the approved Nexus atlas.\nMachine-specific hydraulic, bath, magnet, control and beam tiles: authored Revo pixel artwork.\nGeometry authored using Blockbench MCP; each structural cube belongs to an individual blueprint block. Moving details and contained fluids/beams are separately identified preview visuals.\n',encoding='utf-8',newline='\n')
    print(json.dumps(summary,indent=2))

if __name__=='__main__':main()
