"""Rebuild editable sluice assets. No Blockbench/Blender installation required."""
from pathlib import Path
import base64, io, json, math, uuid, zipfile
from PIL import Image, ImageDraw
import sys

REPO = Path(__file__).resolve().parents[1]
ROOT = REPO/'art'/'blockbench'/'revoextraction'/'mechanical-sluice'
if (ROOT/'models/mechanical_sluice.bbmodel').exists() and '--reset-blockout' not in sys.argv:
    raise SystemExit('The native Blockbench source is authoritative. Use export_sluice_runtime.py; --reset-blockout deliberately replaces the authored model.')
for directory in [ROOT, ROOT/'assets', ROOT/'models']:
    directory.mkdir(parents=True, exist_ok=True)
JAR = Path(r'C:\Users\Victor\curseforge\minecraft\Instances\Entropy Zero  To The Sky\mods\create-1.21.1-6.0.10.jar')
FACES = ['north','east','south','west','up','down']
materials = {}
def texture(name, color, resource=None):
    # Original small pixel tiles; Create resources are resolved at runtime by game models.
    im=Image.new('RGB',(16,16),color); px=im.load()
    for y in range(16):
        for x in range(16):
            noise=((x*73+y*37+x*y*11)%13)-6
            px[x,y]=tuple(max(0,min(255,c+noise)) for c in color)
    d=ImageDraw.Draw(im)
    if name in ('casing','chute'):
        d.rectangle((0,0,15,15),outline=(57,59,55),width=2)
        d.line((2,2,13,2),fill=(158,157,138))
        for x,y in [(2,2),(13,2),(2,13),(13,13)]: d.point((x,y),fill=(187,185,164))
    if name=='wood':
        for y in (0,5,10,15):d.line((0,y,15,y),fill=(69,54,36))
    if name=='steel':
        d.line((0,1,15,1),fill=(148,153,143));d.line((0,14,15,14),fill=(54,60,56))
    if name=='brass':
        d.line((0,0,15,0),fill=(221,197,113));d.line((0,15,15,15),fill=(116,91,44))
    if name=='water':
        for y in range(0,16,4):d.line((2,y,11,y),fill=(98,177,217))
    path=ROOT/'assets'/f'{name}.png';im.save(path)
    materials[name]={'path':f'assets/{name}.png','resource':resource or f'revoextraction:block/sluice/{name}'}

texture('casing',(106,111,98),'create:block/andesite_casing')
texture('steel',(94,103,98),'create:block/axis')
texture('wood',(114,87,52))
texture('brass',(178,147,75),'create:block/brass_block')
texture('copper',(162,100,71),'create:block/copper_casing')
texture('chute',(106,112,105))
texture('gravel',(126,122,119))
texture('water',(46,112,172))
texture('gem',(53,170,129))
texture('dark',(48,55,51))

parts=[]
def box(name,lo,hi,mat='steel',group='body',rotation=None,pivot=None):
    p={'name':name,'from':lo,'to':hi,'material':mat,'group':group}
    if rotation:p['rotation']=rotation
    if pivot:p['pivot']=pivot
    parts.append(p)

# Coordinates in Minecraft model units. X across trough, Y up, +Z downstream.
# The controller occupies x=-8..8, y=0..16, z=0..16.
for i in range(3):
    z=i*16;h=10-i*2
    box(f'bed_{i}',[-6,h-1,z],[6,h,z+16],'dark')
    for side in (-1,1):
        a,b=((-8,-6) if side<0 else (6,8))
        box(f'andesite_rail_{i}_{side}',[a,h-3,z],[b,h+3,z+16],'casing')
        box(f'rail_cap_{i}_{side}',[a-.2,h+3,z],[b+.2,h+4,z+16],'steel')
        for zz in (z+1,z+13):
            box(f'leg_{i}_{side}_{zz}',[a,0,zz],[b,h-3,zz+2],'casing')
        # Each block section has a continuous wet surface and a downhill edge sheet.
    box(f'water_bed_{i}',[-6,h+.08,z],[6,h+.65,z+16],'water','water')
    if i<2:box(f'water_step_{i}',[-6,h-1.95,z+15.8],[6,h+.65,z+16.2],'water','water')
    for j in (5,11):
        box(f'riffle_{i}_{j}',[-6,h,z+j],[6,h+1.05,z+j+1],'wood')
box('downstream_waterfall',[-6,0,48],[6,6.65,48.4],'water','water')
box('outlet_lip',[-6,4.5,47],[6,5.5,49],'steel')

# Rear pipe port at the upper block center; water drops inside rear manifold.
box('rear_manifold',[-6,16,0],[6,29,4],'casing','feeder')
box('pipe_flange',[-4,20,-1],[4,28,1],'copper','feeder')
box('pipe_bore',[-2,22,-1.12],[2,26,-1.02],'dark','feeder')
box('inlet_spout',[-3,13.5,3],[3,17,5],'copper')
box('inlet_water',[-2,10.65,4.1],[2,14.5,4.9],'water','water')

# Floating open grille, supported by slide shoes rather than unsupported vibration.
for x in (-5.8,5):box(f'tray_side_{x}',[x,13.4,5],[x+.8,15,18],'steel','shaker')
for z in (5,17.2):box(f'tray_end_{z}',[-5.8,13.4,z],[5.8,15,z+.8],'steel','shaker')
for x in range(-4,5,2):box(f'grille_long_{x}',[x,13.8,5.8],[x+.65,14.35,17.2],'steel','shaker')
for z in range(7,17,2):box(f'grille_cross_{z}',[-5,13.8,z],[5,14.35,z+.65],'steel','shaker')
for side in (-1,1):
    a,b=((-9,-7.5) if side<0 else (7.5,9))
    box(f'static_slide_{side}',[a,7,12],[b,9.3,20],'steel')
    a,b=((-10.5,-9) if side<0 else (9,10.5))
    box(f'moving_shoe_{side}',[a,7.3,14],[b,9,17],'brass','shaker')
    box(f'tray_outrigger_{side}',[min(side*5.8,side*9.5),13.8,15],[max(side*5.8,side*9.5),14.6,16],'steel','shaker')
    a,b=((-10.3,-9.3) if side<0 else (9.3,10.3))
    box(f'slider_hanger_{side}',[a,8,15],[b,14.3,16],'steel','shaker')
    a,b=((-9,-6) if side<0 else (6,9))
    box(f'bearing_{side}',[a,5,5],[b,11,11],'casing')

# Standard 4x4 shaft end at each X face; crank on operator's west side.
box('transverse_shaft',[-8,6,6],[8,10,10],'steel','drive',pivot=[0,8,8])
box('crank_axle',[-11,6.5,6.5],[-8,9.5,9.5],'steel','drive',pivot=[0,8,8])
box('crank_hub',[-11,5,5],[-9,11,11],'steel','crank',pivot=[-10,8,8])
box('eccentric_pin',[-11.2,7.4,8.4],[-8.8,8.6,9.6],'brass','crank',pivot=[-10,8,8])
box('connecting_rod',[-10.45,7.55,9],[-9.55,8.45,16],'steel','rod',pivot=[-10,8,9])

# Gravel feeder: open rim, stepped tapered hopper, throat and reciprocating slide gate.
for side in (-1,1):
    a,b=((-7,-6) if side<0 else (6,7))
    box(f'feeder_pillar_{side}',[a,15,7],[b,27,9],'casing','feeder')
for level,(half,y) in enumerate([(6.5,29),(5.5,27),(4.5,25),(3.5,23)]):
    for s in (-1,1):
        a,b=((-half,-half+1) if s<0 else (half-1,half))
        box(f'hopper_wall_x_{level}_{s}',[a,y,9-half],[b,y+2,9+half],'chute','feeder')
        a,b=((-half,-half+1) if s<0 else (half-1,half))
        box(f'hopper_wall_z_{level}_{s}',[-half+1,y,9+a],[half-1,y+2,9+b],'chute','feeder')
for x in (-7,6):box(f'hopper_lip_x_{x}',[x,31,2],[x+1,32,16],'steel','feeder')
for z in (2,15):box(f'hopper_lip_z_{z}',[-6,31,z],[6,32,z+1],'steel','feeder')
for x in (-2.5,1.5):box(f'feeder_throat_x_{x}',[x,18,6.5],[x+1,23,11.5],'chute','feeder')
for z in (6.5,10.5):box(f'feeder_throat_z_{z}',[-1.5,18,z],[1.5,23,z+1],'chute','feeder')
box('metering_gate',[-2.5,17.5,6.5],[2.5,18,11.5],'brass','gate')
box('gate_rod',[-8.5,17.5,8.5],[-2.5,18,9.5],'steel','gate')
box('gate_slide_housing',[-9,16.2,7],[-7,20,11],'casing','feeder')
box('feeder_link_upright',[-10.3,9,15],[-9.5,18,16],'steel','gate')
box('feeder_link_transfer',[-10.3,17.5,8.5],[-9.5,18.3,16],'steel','gate')
box('feeder_link_cross',[-10.3,17.5,8.5],[-8.4,18.3,9.5],'steel','gate')

# Separate context fittings are not included in the deliverable machine model.
box('supply_pipe',[-2,22,-24],[2,26,-1],'copper','context')
for z in (-17,-2):box(f'pipe_coupling_{z}',[-3,21,z],[3,27,z+2],'copper','context')
box('pump_body',[-5,19,-30],[5,29,-20],'copper','context')
box('pump_axis',[-12,22,-27],[12,26,-23],'steel','pump_drive',pivot=[0,24,-25])
box('pump_supply',[-2,22,-42],[2,26,-30],'copper','context')
for x in (-5,3):box(f'pump_support_{x}',[x,0,-28],[x+2,19,-22],'casing','context')

manifest={
 'name':'Mechanical Sluice','units_per_block':16,'axis':'+Z downstream, +Y up, X across',
 'target':'Minecraft Java 1.21.1 / NeoForge 21.1.265 / Create 6.0.10 / RevoExtraction',
 'materials':materials,'parts':parts,
 'ports':[
  {'name':'water_in','position':[0,24,0],'normal':[0,0,-1],'kind':'fluid','note':'Upper rear face; connect Fluid Pipe supplied by Mechanical Pump'},
  {'name':'shaft_west','position':[-8,8,8],'normal':[-1,0,0],'kind':'kinetic'},
  {'name':'shaft_east','position':[8,8,8],'normal':[1,0,0],'kind':'kinetic'},
  {'name':'gravel_in','position':[0,32,8],'normal':[0,1,0],'kind':'item'},
  {'name':'products','position':[8,8,40],'normal':[1,0,0],'kind':'item','note':'Final segment extraction capability and automatic export'},
  {'name':'tail_water','position':[0,6,48],'normal':[0,0,1],'kind':'visual_outlet'}],
 'mechanism':{'crank_center':[-10,8,8],'radius':1,'rod_length':7,'slider_y':8,
              'slider_z':'8 + cos(theta) + sqrt(49 - sin(theta)^2)',
              'shaker_translation_z':'slider_z - 16','shaft_axis':'X'},
 'processing':{'minimum_rpm':16,'nominal_rpm':32,'water_mb_per_tick':5,'ticks_per_gravel_at_32rpm':100,
             'input_slots':1,'output_slots':4,'water_buffer_mb':4000,
             'conditions':'Water flows independently while supplied. Work needs water, rotation, gravel and output space. Consume gravel only on completed work.'}}
(ROOT/'assembly.json').write_text(json.dumps(manifest,indent=2))

# Blockbench Generic Model: each independent moving component has its own pivot/group.
tex_names=list(materials);tex_index={n:i for i,n in enumerate(tex_names)}
textures=[]
for n in tex_names:
    textures.append({'path':str(ROOT/materials[n]['path']),'name':n+'.png','id':str(tex_index[n]),
                     'uuid':str(uuid.uuid5(uuid.NAMESPACE_DNS,'sluice-tex-'+n)),
                     'source':'data:image/png;base64,'+base64.b64encode((ROOT/materials[n]['path']).read_bytes()).decode(),
                     'width':16,'height':16,'uv_width':16,'uv_height':16,'mode':'bitmap','saved':True})
elements=[];groups={}
for p in parts:
    if p['group'] in ('context','pump_drive'):continue
    uid=str(uuid.uuid5(uuid.NAMESPACE_DNS,'sluice-'+p['name']))
    dx,dy,dz=[b-a for a,b in zip(p['from'],p['to'])]
    def uv(face):
        w,h=(dx,dz) if face in ('up','down') else ((dz,dy) if face in ('east','west') else (dx,dy))
        return [0,0,w,h]
    el={'name':p['name'],'type':'cube','uuid':uid,'from':p['from'],'to':p['to'],'origin':p.get('pivot',[0,0,0]),
        'rotation':p.get('rotation',[0,0,0]),'box_uv':False,'rescale':False,
        'faces':{f:{'uv':uv(f),'texture':tex_index[p['material']]} for f in FACES}}
    elements.append(el);groups.setdefault(p['group'],[]).append(uid)
outliner=[]
pivots={'drive':[0,8,8],'crank':[-10,8,8],'rod':[-10,8,9],'shaker':[0,8,16],'gate':[0,18,9]}
for name,children in groups.items():
    outliner.append({'name':name,'uuid':str(uuid.uuid5(uuid.NAMESPACE_DNS,'sluice-group-'+name)),
                     'origin':pivots.get(name,[0,0,0]),'rotation':[0,0,0],'children':children,
                     'export':True,'isOpen':True,'visibility':True})
model={'meta':{'format_version':'4.10','model_format':'free','box_uv':False},'name':'mechanical_sluice',
       'resolution':{'width':16,'height':16},'elements':elements,'outliner':outliner,'textures':textures,
       'animations':[],'unhandled_root_fields':{}}
(ROOT/'models'/'mechanical_sluice.bbmodel').write_text(json.dumps(model,indent=2))

# Runtime partial model JSONs: static bed sections use local coordinates; moving
# pieces retain controller coordinates for a custom renderer to transform.
exports={}
for p in parts:
    if p['group'] in ('context','pump_drive','water'):continue
    if p['group']=='body':
        # Group long bed sections by their block; rear manifold belongs to controller.
        section=min(2,max(0,int(p['from'][2]//16)))
        key=f'body_{section}';offset=[8,0,-16*section]
    else:key=p['group'];offset=[8,0,0]
    exports.setdefault(key,{'parent':'minecraft:block/block','textures':{},'elements':[]})
    out=exports[key];mat=p['material'];out['textures'][mat]=materials[mat]['resource']
    out['textures']['particle']='create:block/andesite_casing'
    dx,dy,dz=[b-a for a,b in zip(p['from'],p['to'])]
    out['elements'].append({'name':p['name'],
      'from':[a+b for a,b in zip(p['from'],offset)],'to':[a+b for a,b in zip(p['to'],offset)],
      'faces':{f:{'texture':'#'+mat,'uv':[0,0,*((dx,dz) if f in ('up','down') else ((dz,dy) if f in ('east','west') else (dx,dy)))]} for f in FACES}})
for name,data in exports.items():(ROOT/'models'/f'{name}.json').write_text(json.dumps(data,indent=2))

stats={'machine_cubes':len(elements),'maximum_quads':len(elements)*6,'maximum_triangles':len(elements)*12,
       'groups':len(groups),'textures':len(textures),'texture_size':'16x16 each, repeat tiled',
       'runtime_note':'Partial models need a block entity renderer and multiblock registration; water rendered separately.'}
(ROOT/'model-stats.json').write_text(json.dumps(stats,indent=2))
# Install Java runtime resources. Baked segments keep ordinary 0..16 scale;
# moving partials are divided by three and restored by the block entity renderer.
ASSETS=REPO/'revoextraction/src/main/resources/assets/revoextraction'
DATA=REPO/'revoextraction/src/main/resources/data/revoextraction'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n')
def runtime_element(p,offset,scale=1):
    dx,dy,dz=[b-a for a,b in zip(p['from'],p['to'])]
    return {'name':p['name'],
        'from':[(a+b)/scale for a,b in zip(p['from'],offset)],
        'to':[(a+b)/scale for a,b in zip(p['to'],offset)],
        'faces':{f:{'texture':'#'+p['material'],'uv':[0,0,*((dx,dz) if f in ('up','down') else ((dz,dy) if f in ('east','west') else (dx,dy)))]} for f in FACES}}
runtime={}
for p in parts:
    group=p['group']
    if group in ('context','pump_drive','water'):continue
    if group=='body':
        section=min(2,max(0,int(p['from'][2]//16)))
        key=f'body_{section}';offset=[8,0,-16*section];scale=1
    elif group=='feeder':key='feeder';offset=[8,-16,0];scale=1
    else:key=group;offset=[8,0,0];scale=3
    runtime.setdefault(key,{'parent':'minecraft:block/block','textures':{'particle':'create:block/andesite_casing'},'elements':[]})
    runtime[key]['textures'][p['material']]=materials[p['material']]['resource']
    runtime[key]['elements'].append(runtime_element(p,offset,scale))
for key,data in runtime.items():write(ASSETS/'models/block/sluice'/f'{key}.json',data)
for name,mat in materials.items():
    if mat['resource'].startswith('revoextraction:'):
        dst=ASSETS/'textures/block/sluice'/f'{name}.png';dst.parent.mkdir(parents=True,exist_ok=True)
        dst.write_bytes((ROOT/mat['path']).read_bytes())
variants={}
for facing,angle in [('south',0),('west',90),('north',180),('east',270)]:
    for i in range(3):variants[f'facing={facing},part={i}']={'model':f'revoextraction:block/sluice/body_{i}','y':angle}
write(ASSETS/'blockstates/mechanical_sluice.json',{'variants':variants})
write(ASSETS/'blockstates/gravel_feeder.json',{'variants':{f'facing={f}':{'model':'revoextraction:block/sluice/feeder','y':a} for f,a in [('south',0),('west',90),('north',180),('east',270)]}})
for name,parent in [('mechanical_sluice','body_0'),('gravel_feeder','feeder')]:
    write(ASSETS/'models/item'/f'{name}.json',{'parent':f'revoextraction:block/sluice/{parent}'})
    entry={'type':'minecraft:item','name':f'revoextraction:{name}'}
    if name=='mechanical_sluice':entry['conditions']=[{'condition':'minecraft:block_state_property','block':'revoextraction:mechanical_sluice','properties':{'part':'0'}}]
    write(DATA/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
item_parts=[p for p in parts if p['group'] in ('body','drive','crank','rod','shaker')]
write(ASSETS/'models/item/mechanical_sluice.json',{'parent':'minecraft:block/block',
 'textures':{'particle':'create:block/andesite_casing',**{name:mat['resource'] for name,mat in materials.items()}},
 'elements':[runtime_element(p,[24,0,0],3) for p in item_parts],
 'display':{'gui':{'rotation':[30,225,0],'translation':[0,3,0],'scale':[1.1,1.1,1.1]},
            'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]}}})
write(DATA/'recipe/mechanical_sluice.json',{'type':'minecraft:crafting_shaped','pattern':['ABA','CSC','AAA'],'key':{'A':{'item':'create:andesite_alloy'},'B':{'item':'minecraft:iron_bars'},'C':{'item':'create:andesite_casing'},'S':{'item':'create:shaft'}},'result':{'id':'revoextraction:mechanical_sluice','count':1}})
write(DATA/'recipe/gravel_feeder.json',{'type':'minecraft:crafting_shaped','pattern':['I I','ICI',' P '],'key':{'I':{'item':'minecraft:iron_ingot'},'C':{'item':'create:chute'},'P':{'item':'create:fluid_pipe'}},'result':{'id':'revoextraction:gravel_feeder','count':1}})
langpath=ASSETS/'lang/en_us.json';lang=json.loads(langpath.read_text())
lang.update({'block.revoextraction.mechanical_sluice':'Mechanical Sluice','block.revoextraction.gravel_feeder':'Gravel Feeder',
 'gui.revoextraction.sluice_control':'Mechanical Sluice','gui.revoextraction.sluice_output':'Sluice Collection Tray','gui.revoextraction.sluice_feeder':'Gravel Feeder',
 'gui.revoextraction.sluice_status_0':'Incomplete channel','gui.revoextraction.sluice_status_1':'Add aligned gravel feeder',
 'gui.revoextraction.sluice_status_2':'Needs at least 16 RPM','gui.revoextraction.sluice_status_3':'Needs pumped water',
 'gui.revoextraction.sluice_status_4':'Needs gravel','gui.revoextraction.sluice_status_5':'Collection tray full',
 'gui.revoextraction.sluice_status_6':'Washing gravel'})
write(langpath,lang)
for tag in ['mineable/pickaxe','needs_stone_tool']:
    path=REPO/'revoextraction/src/main/resources/data/minecraft/tags/block'/f'{tag}.json'
    data=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
    for name in ['mechanical_sluice','gravel_feeder']:
        value=f'revoextraction:{name}'
        if value not in data['values']:data['values'].append(value)
    write(path,data)
write(REPO/'revoextraction/src/main/resources/data/create/tags/block/non_movable.json',{'replace':False,'values':['revoextraction:mechanical_sluice']})
# Editor playback: same crank/slider equations as the game renderer, at 24fps.
animators={}
for group in ['drive','crank','rod','shaker','gate']:
    keys=[]
    for frame in range(97):
        time=frame/24;theta=time*math.tau/4;s=math.sin(theta);c=math.cos(theta)
        root=math.sqrt(49-s*s);delta=8+c+root-16
        if group in ['drive','crank']:channels={'rotation':[-math.degrees(theta),0,0]}
        elif group=='rod':channels={'position':[0,s,c-1],'rotation':[math.degrees(math.atan2(s,root)),0,0]}
        else:channels={'position':[0,0,delta]}
        for channel,values in channels.items():
            keys.append({'channel':channel,'data_points':[dict(zip(['x','y','z'],map(str,values)))],'uuid':str(uuid.uuid5(uuid.NAMESPACE_DNS,f'sluice-key-{group}-{channel}-{frame}')),'time':time,'color':-1,'interpolation':'linear'})
    uid=str(uuid.uuid5(uuid.NAMESPACE_DNS,'sluice-group-'+group))
    animators[uid]={'name':group,'type':'bone','keyframes':keys}
model['animations']=[{'uuid':str(uuid.uuid5(uuid.NAMESPACE_DNS,'sluice-working')),'name':'working','loop':'loop','length':4,'snapping':24,'animators':animators}]
write(ROOT/'models/mechanical_sluice.bbmodel',model)
stats['runtime_note']='Native Java partial models; game renderer restores moving parts to physical scale; water is a separate flow pass.'
write(ROOT/'model-stats.json',stats)
print(json.dumps(stats,indent=2))
