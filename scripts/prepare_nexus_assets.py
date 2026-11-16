"""Prepare a block-by-block Nexus blueprint, pixel textures and Blockbench MCP operations.

Geometry is authored through Blockbench MCP, never installed as a formed-machine mesh.
This writes editable art/design assets only; it does not register unfinished game content.
"""
from pathlib import Path
from collections import Counter
import base64, io, json, math, shutil, zipfile
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/nexus-portal'
WORK=ROOT/'.local/nexus'
DECOR=['deck_grate','ribbed_panel','reinforced_column','ceramic_panel','service_panel','foundation_chiseled']
TILES=['casing','ring','foundation','hazard','trim','vent','light_blue','light_amber','light_violet','glass','controller','input','output','fluid_in','fluid_out','energy','rail','core_frame','core_light',*DECOR]
BLOCK_PARTS=['casing','ring','foundation','hazard','trim','vent','light_blue','light_amber','light_violet','controller','input','output','fluid_in','fluid_out','energy','rail','beacon','core',*DECOR,'rail_north','rail_east','rail_south','rail_west']
DIRECTIONS={'north':(0,0,-1),'east':(1,0,0),'south':(0,0,1),'west':(-1,0,0)}

def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8',newline='\n')

def textures():
    atlas=Image.new('RGBA',(256,256),(0,0,0,0))
    with zipfile.ZipFile(ROOT/'.local/integration-mods/Modern-Industrialization-2.5.11.jar') as mi:
        def sprite(name):
            return Image.open(io.BytesIO(mi.read('assets/modern_industrialization/textures/block/'+name+'.png'))).convert('RGBA').crop((0,0,16,16))
        steel=sprite('steel_machine_casing')
        for index,name in enumerate(TILES):
            tile=Image.new('RGBA',(16,16),'#30313b');d=ImageDraw.Draw(tile)
            d.rectangle((1,1,14,14),fill='#3b3c47');d.line((1,1,14,1),fill='#62636d');d.line((14,2,14,14),fill='#20212a')
            for x,y in [(2,2),(13,2),(2,13),(13,13)]:d.rectangle((x,y,x+1,y+1),fill='#1d1f28');d.point((x,y),fill='#72727c')
            for x,y in [(4,5),(9,10),(5,11)]:d.line((x,y,x+2,y),fill='#41434d')
            if name=='ring':tile=steel.copy()
            if name=='foundation':
                d.rectangle((0,0,15,15),fill='#242630');d.line((0,0,15,0),fill='#494b56');d.line((0,8,15,8),fill='#171a22');d.line((7,0,7,7),fill='#171a22');d.line((3,9,3,15),fill='#171a22')
            if name=='trim':
                d.rectangle((2,2,13,13),fill='#74747d');d.line((2,2,13,2),fill='#b1aeb3');d.line((13,3,13,13),fill='#43434e')
            if name=='hazard':
                d.rectangle((1,1,14,14),fill='#d5a323')
                for yy in range(2,14):
                    for xx in range(2,14):
                        if (xx+yy)%12<5:d.point((xx,yy),fill='#24232b')
                d.line((1,1,14,1),fill='#f7c958')
            if name=='vent':
                for yy in [4,7,10]:d.rectangle((3,yy,12,yy+1),fill='#171b24');d.line((4,yy,11,yy),fill='#0d1119')
            if name.startswith('light_') or name=='core_light':
                color={'light_blue':('#6383b7','#a9d6ff','#edf8ff'),'light_amber':('#bd7719','#ffbc3c','#fff1aa'),'light_violet':('#713baf','#be66ff','#efd3ff'),'core_light':('#7438a8','#b86eff','#e7d6ff')}[name]
                d.rectangle((2,4,13,11),fill='#121923');d.rectangle((3,5,12,10),fill=color[0]);d.rectangle((4,6,11,9),fill=color[1]);d.line((5,7,10,7),fill=color[2])
                for xx in [4,7,10]:d.point((xx,9),fill=color[2])
            if name=='core_light':
                d.rectangle((0,0,15,15),fill='#552283')
                d.rectangle((2,2,13,13),fill='#8b43bf');d.rectangle((4,4,11,11),fill='#ba7ee9')
                d.rectangle((6,5,9,10),fill='#ecd3ff');d.line((5,4,10,4),fill='#e1b8fa')
                for xx,yy in [(2,3),(3,12),(12,2),(13,11),(5,6),(10,9)]:d.point((xx,yy),fill='#d69bf3')
            if name=='glass':
                tile=Image.new('RGBA',(16,16),(28,34,48,88));d=ImageDraw.Draw(tile);d.line((2,4,4,2),fill=(151,164,190,125))
            if name=='controller':
                d.rectangle((2,3,13,11),fill='#111722');d.rectangle((3,4,12,5),fill='#a6cfef');d.rectangle((3,7,7,8),fill='#9a63c6');d.line((9,8,12,8),fill='#c79838')
            overlays={'input':'output','output':'output_item','fluid_in':'output_fluid','fluid_out':'output_fluid','energy':'output_energy'}
            if name in overlays:
                tile=steel.copy();tile.alpha_composite(sprite('overlays/'+overlays[name]))
                if name in ['output','fluid_out']:tile.alpha_composite(sprite('overlays/'+('item_auto' if name=='output' else 'fluid_auto')))
            if name=='rail':
                d.rectangle((0,0,15,15),fill='#a77512');d.line((0,0,15,0),fill='#f2c543');d.line((0,14,15,14),fill='#61430e')
            if name=='deck_grate':
                d.rectangle((0,0,15,15),fill='#242b34');d.rectangle((1,1,14,14),outline='#69747d')
                for xx in [3,6,9,12]:d.line((xx,3,xx,12),fill='#55636d');d.line((xx+1,3,xx+1,12),fill='#111820')
                for yy in [5,10]:d.line((2,yy,13,yy),fill='#7a858c')
                for xx,yy in [(1,1),(14,1),(1,14),(14,14)]:d.point((xx,yy),fill='#a5abb0')
            if name=='ribbed_panel':
                d.rectangle((0,0,15,15),fill='#39424f');d.rectangle((1,1,14,14),outline='#697583')
                for yy in [3,6,9,12]:d.line((2,yy,13,yy),fill='#89919b');d.line((2,yy+1,13,yy+1),fill='#222c38')
            if name=='reinforced_column':
                d.rectangle((0,0,15,15),fill='#2b303a');d.rectangle((3,0,12,15),fill='#69727e')
                d.line((3,0,3,15),fill='#a9afb7');d.line((11,0,11,15),fill='#424b58')
                for yy in [2,13]:d.rectangle((1,yy,14,yy+1),fill='#4b5562');d.point((2,yy),fill='#b1b6bc');d.point((13,yy),fill='#b1b6bc')
            if name=='ceramic_panel':
                d.rectangle((0,0,15,15),fill='#414954');d.rectangle((1,1,14,14),fill='#888e96')
                d.rectangle((3,3,12,12),fill='#aeb1b6');d.line((2,2,13,2),fill='#d2d1d1');d.line((13,3,13,13),fill='#6a727e')
                d.line((4,11,10,11),fill='#979ea7')
            if name=='service_panel':
                d.rectangle((1,1,14,14),fill='#1a222e');d.rectangle((2,2,13,13),outline='#68717e')
                for yy in [5,8,11]:d.rectangle((4,yy,10,yy),fill='#364555')
                d.rectangle((11,4,12,5),fill='#d4a343');d.rectangle((11,7,12,8),fill='#a7c6d6')
                d.rectangle((6,2,9,3),fill='#929aa2')
            if name=='foundation_chiseled':
                d.rectangle((0,0,15,15),fill='#292e38');d.line((0,0,15,0),fill='#666b74');d.line((0,15,15,15),fill='#171c25')
                for xx in [3,7,11]:d.rectangle((xx,3,xx+1,12),fill='#141b25');d.line((xx+2,3,xx+2,12),fill='#484f5b')
            atlas.paste(tile,((index%16)*16,(index//16)*16))
    atlas.save(ART/'textures/nexus_atlas.png')
    frames=[]
    for frame in range(64):
        image=Image.new('RGBA',(256,256),(0,0,0,0));pixels=image.load()
        for yy in range(176):
            for xx in range(176):
                x=(xx//2*2+1-88)/88;y=(yy//2*2+1-88)/88;r=math.hypot(x,y)
                if r>1:continue
                angle=math.atan2(y,x);wave=math.sin(3*angle-13*r-frame*math.tau/64)
                levels=['#100923','#211044','#39136a','#5c209a','#8533c8','#b354e4','#d598f4']
                index=max(0,min(6,int((wave+1)*2.2+(1-r)*1.2)))
                if r>.94:index=max(0,index-2)
                rgb=tuple(int(levels[index][i:i+2],16) for i in (1,3,5));pixels[xx,yy]=(*rgb,255)
        frames.append(image)
    frames[0].save(ART/'textures/nexus_vortex_preview.png')
    strip=Image.new('RGBA',(256,256*64))
    for i,frame in enumerate(frames):strip.paste(frame,(0,i*256))
    strip.save(ART/'textures/nexus_vortex.png');write(ART/'textures/nexus_vortex.png.mcmeta',{'animation':{'frametime':3,'interpolate':True,'width':256,'height':256}})
    frames[0].save(ART/'nexus_vortex.gif',save_all=True,append_images=frames[1:],duration=150,loop=0,disposal=2)
    return atlas

def faces(tile,texture='nexus_atlas',front=None):
    def uv(name):i=TILES.index(name);return [(i%16)*16,(i//16)*16,(i%16)*16+16,(i//16)*16+16]
    return {side:{'uv':uv(front if side=='north' and front else tile),'texture':texture} for side in ['north','south','east','west','up','down']}

def cube(name,from_,to,tile,parent,front=None,**extra):
    return {'op':'add_cube','name':name,'from':from_,'to':to,'box_uv':False,'parent':parent,'faces':faces(tile,front=front),**extra}

def rail_pieces(connections):
    """Post plus half-arms that meet both the post and the neighbouring block exactly."""
    yield 'post',[6,0,6],[10,16,10]
    arms={'north':([7,0,0],[9,0,6]),'east':([10,0,7],[16,0,9]),'south':([7,0,10],[9,0,16]),'west':([0,0,7],[6,0,9])}
    for direction,connected in connections.items():
        if not connected:continue
        a,b=arms[direction]
        for label,low,high in [('lower',5,8),('upper',11,14)]:
            yield direction+' '+label,[a[0],low,a[2]],[b[0],high,b[2]]

def main():
    (ART/'textures').mkdir(parents=True,exist_ok=True);WORK.mkdir(parents=True,exist_ok=True)
    reference=Path('C:/Users/victo/Downloads/Futuristisk Minecraft-portal ved solnedgang.png')
    if reference.exists():shutil.copy2(reference,ART/'reference.png')
    textures();cells={}
    def put(x,y,z,part,section,replace=False):
        if (x,y,z) in cells and not replace:return
        cells[x,y,z]={'position':[x,y,z],'part':part,'section':section,'facing':'north'}
    for y in [0,1]:
        for x in range(-10,11):
            for z in range(9):put(x,y,z,'foundation' if y==0 else 'casing','foundation')
    for z,height in [(-2,1),(-1,2)]:
        for y in range(height):
            for x in range(-2,3):put(x,y,z,'trim','stairs')
    for x in range(-10,11):
        if abs(x)>2:put(x,1,0,'light_blue' if abs(x)%4==0 else 'vent','foundation',True)
        put(x,1,8,'hazard','foundation',True)
    for x in [-10,10]:
        for z in range(9):put(x,1,z,'hazard','foundation',True)
    for x in range(-8,9):
        for y in range(2,19):
            r=math.hypot(x,y-10)
            if 5.7<=r<=7.9:
                for z in [4,5]:put(x,y,z,'ring' if z==4 else 'casing','ring')
    for sign in [-1,1]:
        for x in [sign*8,sign*9]:
            for y in range(2,14):
                for z in range(3,6):put(x,y,z,'casing','towers',True)
        for y in range(4,10):put(sign*9,y,2,'hazard','towers')
        for y in [3,10,12]:put(sign*9,y,2,'light_blue' if y>3 else 'light_amber','towers')
        for x in [sign*8,sign*9]:
            for z in range(3,6):put(x,14,z,'trim','towers')
        for y in [15,16]:put(sign*9,y,4,'beacon','beacons')
        for x in [sign*6,sign*7]:put(x,10,3,'light_violet' if abs(x)==6 else 'light_blue','ring')
        for x in range(3,11):put(sign*x,2,0,'rail','rails')
        for z in range(1,9):put(sign*10,2,z,'rail','rails')
    for x in range(-9,10):put(x,2,8,'rail','rails')
    for y in [3,17]:put(0,y,3,'light_violet','ring')
    put(0,18,4,'beacon','beacons');put(0,2,4,'core','core')
    for x,part in [(-6,'input'),(-5,'output'),(5,'fluid_in'),(6,'fluid_out')]:put(x,1,0,part,'ports',True)
    put(0,1,0,'controller','ports',True);put(0,1,8,'energy','ports',True)
    # Decorative blocks follow structural roles rather than random per-block noise.
    for (x,y,z),cell in cells.items():
        part=cell['part']
        if cell['section']=='foundation' and y==1 and part=='casing' and (z in [2,6] or abs(x)==5):cell['part']='deck_grate'
        elif part=='foundation' and ((z==0 and abs(x)%4==0) or (abs(x)==10 and z in [2,6,8])):cell['part']='foundation_chiseled'
        elif cell['section']=='towers' and part=='casing':
            if abs(x)==8 and z==3:cell['part']='service_panel' if y in [5,11] else 'reinforced_column'
            elif z==5 and y in [4,7,10,13]:cell['part']='ribbed_panel'
        elif part=='ring' and math.hypot(x,y-10)<6.9:cell['part']='ceramic_panel'
        elif cell['section']=='ring' and part=='casing' and abs(x)==abs(y-10):cell['part']='ribbed_panel'
    for (x,y,z),cell in cells.items():
        if cell['part']!='rail':continue
        connections={}
        for direction,(dx,dy,dz) in DIRECTIONS.items():
            neighbour=cells.get((x+dx,y+dy,z+dz))
            connections[direction]=neighbour is not None and neighbour['part'] not in ['beacon','core']
        cell['connections']=connections
    counts=dict(sorted(Counter(c['part'] for c in cells.values()).items()))
    bounds={axis:[min(p[i] for p in cells),max(p[i] for p in cells)+1] for i,axis in enumerate('xyz')}
    blueprint={'status':'ART_AND_DESIGN_ONLY_NOT_REGISTERED','units':'Minecraft blocks; Blockbench 16 model units per block','origin':'Centre axis X=0.5; front is -Z; floor Y=0','bounds':bounds,'dimensions':{a:b[1]-b[0] for a,b in bounds.items()},'blockCount':len(cells),'counts':counts,'blockModelParts':BLOCK_PARTS,'cells':sorted(cells.values(),key=lambda c:(c['position'][1],c['position'][2],c['position'][0])),'portalVisual':{'centre':[.5,10.5,4.15],'diameter':11,'render':'special surface only; no frame/base transformation'},'colliderDesign':{'footprint':[21,21],'height':5,'shape':'stepped conical annulus; individual placed blocks'}}
    write(ART/'portal-blueprint.json',blueprint)
    sections=['foundation','stairs','ring','towers','ports','rails','beacons','core']
    ops=[{'op':'add_group','name':name,'origin':[8,0,0]} for name in sections]
    for cell in blueprint['cells']:
        x,y,z=cell['position'];part=cell['part'];p=[x*16,y*16,z*16];name=f'{part} @ {x},{y},{z}'
        if part=='rail':
            for suffix,a,b in rail_pieces(cell['connections']):ops.append(cube(name+' '+suffix,[p[i]+a[i] for i in range(3)],[p[i]+b[i] for i in range(3)],'rail','rails'))
        elif part=='beacon':
            for suffix,a,b,tile in [('base',[2,0,2],[14,3,14],'core_frame'),('stem',[5,3,5],[11,14,11],'light_blue'),('cap',[3,14,3],[13,16,13],'trim')]:ops.append(cube(name+' '+suffix,[p[i]+a[i] for i in range(3)],[p[i]+b[i] for i in range(3)],tile,'beacons'))
        elif part=='core':
            for suffix,a,b,tile in [('bottom',[0,0,0],[16,3,16],'core_frame'),('top',[0,13,0],[16,16,16],'core_frame'),('left',[0,3,0],[3,13,16],'core_frame'),('right',[13,3,0],[16,13,16],'core_frame'),('crystal',[5,5,5],[11,11,11],'core_light')]:
                extra={'origin':[p[i]+8 for i in range(3)],'rotation':[0,0,45]} if suffix=='crystal' else {}
                ops.append(cube(name+' '+suffix,[p[i]+a[i] for i in range(3)],[p[i]+b[i] for i in range(3)],tile,'core',**extra))
        else:
            is_port=part in ['input','output','fluid_in','fluid_out','energy','controller']
            ops.append(cube(name,p,[v+16 for v in p],'ring' if is_port else part,cell['section'],front=part if is_port else None))
    ops.append({'op':'add_group','name':'portal_vortex','origin':[8,168,66.4]})
    # Separate front/back faces by 1/64 block rather than stacking opposite quads.
    ops.append({'op':'add_cube','name':'Animated portal surface ONLY','from':[-80,80,66.275],'to':[96,256,66.525],'box_uv':False,'parent':'portal_vortex','faces':{side:{'uv':[0,0,176,176],'texture':'nexus_vortex_preview'} for side in ['north','south']}})
    ops.extend([{'op':'add_animation','name':'portal_swirl_preview','length':16,'loop':'loop','snapping':24},*[{'op':'set_keyframe','animation':'portal_swirl_preview','bone':'portal_vortex','channel':'rotation','time':time,'value':[0,0,time*22.5],'interpolation':'linear'} for time in range(0,17,2)]])
    write(WORK/'assembly-operations.json',ops)
    for part in BLOCK_PARTS:
        if part=='rail' or part.startswith('rail_'):
            connections={direction:part=='rail_'+direction for direction in DIRECTIONS}
            pieces=list(rail_pieces(connections))
            if part!='rail':pieces=pieces[1:]
            local=[{'op':'add_group','name':part,'origin':[8,8,8]},*[cube(part+' '+suffix,a,b,'rail',part) for suffix,a,b in pieces]]
            write(WORK/f'{part}-operations.json',local);continue
        members=[c for c in ops if c.get('op')=='add_cube' and c['name'].startswith(part+' @ ')]
        if not members:continue
        # Rebase a single placed block's components into 0..16 for individual exports.
        first=next(c for c in blueprint['cells'] if c['part']==part);position=first['position'];prefix=f'{part} @ '+','.join(map(str,position))
        local=[{'op':'add_group','name':part,'origin':[8,8,8]}]
        for op in members:
            if not (op['name']==prefix or op['name'].startswith(prefix+' ')):continue
            copy=dict(op);copy['parent']=part;copy['name']=op['name'].replace(prefix,part)
            for key in ['from','to']:copy[key]=[op[key][i]-position[i]*16 for i in range(3)]
            if 'origin' in op:copy['origin']=[op['origin'][i]-position[i]*16 for i in range(3)]
            local.append(copy)
        write(WORK/f'{part}-operations.json',local)
    write(ART/'blockstates/nexus_rail.json',{'multipart':[{'apply':{'model':'revoindustry:block/nexus_rail'}},*[{'when':{direction:'true'},'apply':{'model':'revoindustry:block/nexus_rail_'+direction}} for direction in DIRECTIONS]]})
    (ART/'CREDITS.txt').write_text('Reference image: supplied by the pack owner.\nNative steel casing and hatch sprites: Modern Industrialization 2.5.11, AztechMC / contributors; copied from the pinned mod artifact.\nDark Nexus panels, hazard markings, pixel lamps and vortex: authored Revo project artwork.\nEditable geometry: Blockbench MCP. One assembly cube per placed block, except explicitly named block-model details and the animated portal surface.\n',encoding='utf-8',newline='\n')
    print(json.dumps({'dimensions':blueprint['dimensions'],'placedBlocks':len(cells),'counts':counts,'mcpOperations':len(ops)},indent=2))

if __name__=='__main__':main()
