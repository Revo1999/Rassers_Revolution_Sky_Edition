"""Export the saved native Blockbench source through Blockbench's Java codec.

Requires the user's local Blockbench MCP server. Preserves the editable source;
generated Java export tabs are separate projects. No external image services.
"""
from pathlib import Path
import base64, hashlib, json, urllib.request

R=Path(__file__).resolve().parents[1]
A=R/'art/blockbench/revoextraction/mechanical-sluice'
DEST=R/'revoextraction/src/main/resources/assets/revoextraction'
SOURCE=A/'models/mechanical_sluice.bbmodel'
URL='http://localhost:3000/bb-mcp'
headers={'Accept':'application/json, text/event-stream','Content-Type':'application/json'}
def request(method,params,identity=1):
    req=urllib.request.Request(URL,data=json.dumps({'jsonrpc':'2.0','id':identity,'method':method,'params':params}).encode(),headers=headers)
    with urllib.request.urlopen(req,timeout=120) as response:
        if response.headers.get('mcp-session-id'):headers['Mcp-Session-Id']=response.headers['mcp-session-id']
        result=json.loads(response.read())
    if result.get('error'):raise RuntimeError(result['error'])
    return result.get('result',{})
def call(name,args):
    result=request('tools/call',{'name':name,'arguments':args})
    if result.get('isError'):raise RuntimeError(result)
    return result
def literal(value):return json.dumps(value).replace('/',chr(92)+'u002f')
def text_result(result):
    for block in result.get('content',[]):
        if block['type']=='text':
            try:return json.loads(block['text'])
            except:pass
    return None
def embedded(result):
    for b in result.get('content',[]):
        if b['type']=='resource' and 'text' in b.get('resource',{}):return b['resource']['text']
    meta=text_result(result)
    if meta and meta.get('content') and not meta.get('truncated'):return meta['content']
    raise RuntimeError('Native codec did not return a complete export')
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n')

request('initialize',{'protocolVersion':'2024-11-05','capabilities':{},'clientInfo':{'name':'Revo native sluice exporter','version':'1'}})
capabilities=text_result(call('get_capabilities',{}))
current=capabilities.get('project',{}).get('uuid') if capabilities else None
source=json.loads(SOURCE.read_text())
groups={g['uuid']:g for g in source.get('groups',[])}
ownership={}
def walk(nodes,parent=None):
    for node in nodes:
        if isinstance(node,str):ownership[node]=parent
        else:
            info=groups.get(node['uuid'],node)
            walk(node.get('children',[]),info.get('name',parent))
walk(source['outliner'])
textures=source['textures']
resources={}
stock_materials={'casing':'andesite_casing','copper':'copper_casing','drive_housing':'gearbox',
                 'panel':'mixer_base_side','steel':'axis','chute':'basin','brass':'brass_block',
                 'native_brass':'brass_casing','native_cog':'cogwheel',
                 'native_iron':'industrial_iron_block','native_axis_top':'axis_top'}
for i,t in enumerate(textures):
    name=Path(t['name']).stem
    resources[i]='create:block/'+stock_materials[name] if name in stock_materials else f'revoextraction:block/sluice/{name}'
    image=base64.b64decode(t['source'].split(',',1)[1])
    (A/'assets'/f'{name}.png').write_bytes(image)
    if resources[i].startswith('revoextraction:') and name not in ['water','gem','gravel']:
        path=DEST/'textures/block/sluice'/f'{name}.png';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(image)

partials={};physical=[]
for e in source['elements']:
    group=ownership.get(e['uuid'])
    if not group:raise ValueError('Element outside named native group: '+e['name'])
    physical.append({'name':e['name'],'from':e['from'],'to':e['to'],'material':Path(textures[next(f['texture'] for f in e['faces'].values() if f.get('texture') is not None)]['name']).stem,'group':group,'pivot':e.get('origin',[0,0,0]),'rotation':e.get('rotation',[0,0,0])})
    if group.startswith('water'):continue
    if group=='body':
        section=min(2,max(0,int(e['from'][2]//16)));key=f'body_{section}';offset=[8,0,-16*section];scale=1
    elif group=='feeder':key='feeder';offset=[8,-16,0];scale=1
    else:key=group;offset=[8,0,0];scale=3
    partials.setdefault(key,[]).append((e,offset,scale))
partials['item']=[(e,[24,0,0],3) for e in source['elements'] if ownership[e['uuid']] in ['body','drive','crank','rod','shaker','trough','spring_front','spring_rear']]

reports=[]
for key,elements in partials.items():
    ids=sorted({f['texture'] for e,_,_ in elements for f in e['faces'].values() if f.get('texture') is not None})
    tex=[{'index':i,'name':textures[i]['name'],'source':textures[i]['source'],'resource':resources[i]} for i in ids]
    cubes=[]
    for e,offset,scale in elements:
        cube={'name':e['name'],'from':[(v+o)/scale for v,o in zip(e['from'],offset)],'to':[(v+o)/scale for v,o in zip(e['to'],offset)],'origin':[(v+o)/scale for v,o in zip(e.get('origin',[0,0,0]),offset)],'rotation':e.get('rotation',[0,0,0]),'faces':e['faces'],'box_uv':False,'autouv':0}
        cubes.append(cube)
    payload={'name':'sluice_'+key,'textures':tex,'cubes':cubes}
    code='(async()=>{const data='+literal(payload)+';newProject(Formats.java_block);Project.name=data.name;Project.texture_width=Project.texture_height=16;Project.box_uv=false;const created=[],tex={};Undo.initEdit({elements:created,textures:[],outliner:true});for(const t of data.textures){const resource=t.resource.split(":");tex[t.index]=new Texture({name:t.name,id:String(t.index),namespace:resource[0],folder:"block",uv_width:16,uv_height:16}).fromDataURL(t.source).add(false);}for(const def of data.cubes){const c=new Cube(def).init();created.push(c);for(const name of Object.keys(def.faces)){const f=def.faces[name];c.faces[name].texture=f.texture===null?null:tex[f.texture].uuid;c.faces[name].uv=f.uv.slice();c.faces[name].rotation=f.rotation||0;}}await new Promise(ok=>setTimeout(ok,60));Canvas.updateAll();Undo.finishEdit("Native sluice Java partial export");return {project:Project.uuid,cubes:Cube.all.length};})()'
    call('risky_eval',{'code':code})
    exported=json.loads(embedded(call('export_model',{'codec_id':'java_block','result_format':'embedded','max_content_length':2000000})))
    exported.pop('groups',None);exported.pop('format_version',None);exported['parent']='minecraft:block/block';exported['render_type']='minecraft:cutout'
    # The codec retains exact UVs and rotations; resolve stock vs custom material links.
    exported['textures']={str(i):resources[i] for i in ids}
    exported['textures']['particle']='create:block/andesite_casing'
    if key=='item':
        exported['display']={'gui':{'rotation':[30,225,0],'translation':[0,3,0],'scale':[1.1,1.1,1.1]},'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]}}
        path=DEST/'models/item/mechanical_sluice.json'
    else:path=DEST/'models/block/sluice'/f'{key}.json'
    write(path,exported)
    if key!='item':write(A/'models'/f'{key}.json',exported)
    reports.append({'part':key,'cubes':len(exported['elements']),'codec':'Blockbench java_block'})
if current:call('risky_eval',{'code':'(()=>{const p=ModelProject.all.find(p=>p.uuid==='+literal(current)+');if(p)p.select();return {project:Project.uuid};})()'})
assembly=json.loads((A/'assembly.json').read_text());assembly['parts']=physical
assembly['mechanism']={'type':'guided vertical pin-and-slot yoke','shaft_center':[0,8,8],'eccentric_radius':.5,'lift':'.5*sin(theta)','translation_z':0,'slot_y':[7.5,8.5],'slot_z':[6.3,9.7],'spring_height':1.4,'moving_groups':['trough','shaker','rod'],'fixed_groups':['body','feeder','water','water_inlet'],'spring_groups':['spring_front','spring_rear']}
assembly['materials']={Path(t['name']).stem:{'path':'assets/'+t['name'],'resource':resources[i]} for i,t in enumerate(textures)}
write(A/'assembly.json',assembly)
stats={'machine_cubes':len(source['elements']),'maximum_quads':len(source['elements'])*6,'maximum_triangles':len(source['elements'])*12,'groups':len(source.get('groups',[])),'textures':len(textures),'texture_size':'16x16 materials; vanilla water 32x1024 (32 frames)','runtime_note':'Native Blockbench Java exports, cutout partials and separate vanilla water pass.'}
write(A/'model-stats.json',stats)
write(A/'native-export.json',{'blockbench':'5.2.2','mcp':'1.10.1','source_sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'parts':reports})
write(A/'assets/vanilla/vanilla_manifest.json',{'source':'Minecraft Java 1.21.1 client resources','attribution':'Minecraft water textures © Mojang AB; Create casing textures © The Create Team','water_flow':{'file':'water_flow.png','frames':32,'frame_size':[32,32],'preview_tint':'#3f76e4','runtime':'minecraft:block/water_flow; biome tinted; resource-pack aware'},'native_materials':'Clustered dithering painted in Blockbench; original Create casing and copper textures referenced from the installed mod at runtime.'})
print(json.dumps({'result':'PASS','native_exports':reports,'stats':stats},indent=2))
