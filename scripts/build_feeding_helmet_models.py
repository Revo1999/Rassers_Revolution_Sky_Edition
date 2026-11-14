"""Build three native helmet models in the user's Blockbench via MCP, then export game assets."""
from pathlib import Path
import base64, json, zipfile, io
from PIL import Image, ImageDraw
from blockbench_mcp_client import Blockbench, literal, embedded

R=Path(__file__).resolve().parents[1]
A=R/'art/blockbench/revoindustry/feeding-helmets';A.mkdir(parents=True,exist_ok=True)
D=R/'revoindustry/src/main/resources/assets/revoindustry'
IDS=['feeding_helmet','advanced_feeding_helmet','elite_feeding_helmet']
bb=Blockbench();bb.call('get_capabilities',{'include_tools':True})
client=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
z=zipfile.ZipFile(client)
skin='data:image/png;base64,'+base64.b64encode(z.read('assets/minecraft/textures/entity/player/wide/steve.png')).decode()
allparts=[];renders=[]
for tier,name in enumerate(IDS):
    parts=[]
    def part(name,a,b,material): parts.append({'name':name,'from':a,'to':b,'material':material})
    part('crown',[-4,8,-4],[4,9.5,4],'shell')
    part('helmet_top',[-4.7,6.8,-4.5],[4.7,8,4.5],'shell')
    for x in [-4.8,3.9]:part('shell_side_'+str(x),[x,3,-3],[x+.9,6.8,4.5],'shell')
    part('rear_shell',[-3.9,2.7,3.8],[3.9,6.8,4.8],'shell')
    part('brow_brim',[-4.6,6.1,-5.1],[4.6,6.8,-3.5],'steel')
    part('ration_module',[4.8,4.6,.2],[6.8,7.4,3.2],'canvas' if tier==0 else 'steel')
    part('hose_coupling',[4.8,3.6,-1.4],[6,4.6,.9],'brass')
    part('hose_down',[4.85,1.4,-2.1],[5.75,3.6,-1.2],'rubber')
    part('hose_to_face',[4.85,.5,-4.9],[5.75,1.4,-1.2],'rubber')
    part('hose_to_mouth',[.8,.5,-5],[4.85,1.4,-4.1],'rubber')
    part('mouthpiece',[-.8,.3,-5.3],[1.2,1.6,-4.4],'steel')
    part('left_strap',[-4.9,1.1,1.5],[-4.1,3,2.4],'canvas')
    if tier>=1:
        part('left_storage_pod',[-6.5,3.3,-.7],[-4.8,6.6,2.9],'canvas')
        part('reinforced_crown_rail',[-.6,9.5,-2.8],[.6,10.1,3.2],'steel')
        part('side_control',[6.8,5,.7],[7.35,6.8,2.8],'control')
        part('front_mount',[-1,7,-5.3],[1,8.1,-4.5],'steel')
        part('hose_armour',[5.75,1.3,-2],[6.25,3.3,-1.3],'steel')
    if tier==2:
        part('rear_storage_manifold',[-3.6,3.7,4.8],[3.6,7.5,6.2],'steel')
        for x in [-3.1,-.8,1.5]:part('ration_canister_'+str(x),[x,3.5,6.2],[x+1.6,7.4,7.3],'tank')
        part('left_temple_plate',[-5.25,5.5,-3.1],[-4.796,7.5,.1],'steel')
        part('right_temple_plate',[4.8,5.5,-3.1],[5.25,7.5,.1],'steel')
        part('status_panel',[-2.2,7.1,-5.4],[2.2,7.9,-5.1],'control')
    x=y=2;row=0
    for p in parts:
        dx,dy,dz=[b-a for a,b in zip(p['from'],p['to'])];w=int(2*(dx+dz)+1);h=int(dy+dz+1)
        if x+w+2>128:x=2;y+=row+3;row=0
        p['uv']=[x,y];p['patch']=[w,h];x+=w+3;row=max(row,h)
        assert y+h<128
    allparts.append(parts)
    bb.call('create_project',{'name':name.replace('_',' ').title(),'format':'free'})
    payload={'name':name,'tier':tier,'parts':parts,'skin':skin}
    code='''(async()=>{const d=DATA;Project.texture_width=Project.texture_height=128;Project.box_uv=true;
    const cv=document.createElement("canvas");cv.width=cv.height=128;const cx=cv.getContext("2d");
    const colors={shell:d.tier===0?[91,101,55]:d.tier===1?[69,85,68]:[43,56,48],steel:d.tier===2?[70,83,76]:[112,122,101],canvas:[128,112,72],brass:[184,145,73],rubber:[35,41,34],control:[47,69,55],tank:[107,133,94]};
    for(const p of d.parts){const col=colors[p.material],u=p.uv[0],v=p.uv[1],w=p.patch[0],h=p.patch[1];for(let y=-1;y<h+1;y++)for(let x=-1;x<w+1;x++){const grain=((x*17+y*11+p.name.length*7)%13===0)?10:((x+y)%4===0?-5:0);const edge=y===0?9:0;cx.fillStyle="rgb("+col.map(c=>Math.max(0,c+grain+edge)).join(",")+")";cx.fillRect(u+x,v+y,1,1);}}
    const tex=new Texture({name:d.name+".png",uv_width:128,uv_height:128}).fromDataURL(cv.toDataURL()).add(false);
    const guideTex=new Texture({name:"fit_reference.png",uv_width:64,uv_height:64}).fromDataURL(d.skin).add(false);
    const group=new Group({name:"helmet",origin:[0,0,0]}).init(),created=[];Undo.initEdit({elements:created,outliner:true,textures:[tex,guideTex],bitmap:true});
    for(const p of d.parts){const c=new Cube({name:p.name,from:p.from,to:p.to,box_uv:true,uv_offset:p.uv,autouv:0}).init().addTo(group);created.push(c);for(const f of Object.values(c.faces))f.texture=tex.uuid;c.mapAutoUV();}
    const guide=new Cube({name:"fit_reference_do_not_export",from:[-4,0,-4],to:[4,8,4],box_uv:true,uv_offset:[0,0],export:false}).init();created.push(guide);for(const f of Object.values(guide.faces))f.texture=guideTex.uuid;guide.mapAutoUV();
    await new Promise(r=>setTimeout(r,120));Canvas.updateAll();Undo.finishEdit("Build military feeding helmet and native-density materials");return {cubes:Cube.all.length};})()'''.replace('DATA',literal(payload))
    bb.call('risky_eval',{'code':code})
    native=embedded(bb.call('export_model',{'codec_id':'project','result_format':'embedded','max_content_length':2000000}))
    (A/(name+'.bbmodel')).write_text(native);m=json.loads(native)
    texture=next(t for t in m['textures'] if t['name']==name+'.png')
    image=base64.b64decode(texture['source'].split(',',1)[1]);target=D/'textures/models/armor'/f'{name}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(image);(A/f'{name}.png').write_bytes(image)
    target=D/'textures/item'/f'{name}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(image)
    native_id=json.loads(next(b['text'] for b in bb.call('get_project_info',{})['content'] if b['type']=='text'))['project']['uuid']
    cubes=[e for e in m['elements'] if e.get('name')!='fit_reference_do_not_export']
    payload={'name':name,'cubes':cubes,'texture':texture['source']}
    code='''(async()=>{const d=DATA;newProject(Formats.java_block);Project.name="helmet_export_"+d.name;Project.texture_width=Project.texture_height=128;Project.box_uv=false;const t=new Texture({name:d.name+".png",uv_width:128,uv_height:128}).fromDataURL(d.texture).add(false);for(const e of d.cubes){const c=new Cube({...e,box_uv:false,autouv:0}).init();for(const [name,f] of Object.entries(e.faces)){c.faces[name].texture=t.uuid;c.faces[name].uv=f.uv.slice();c.faces[name].rotation=f.rotation||0;}}await new Promise(r=>setTimeout(r,60));Canvas.updateAll();Project.saved=true;return true;})()'''.replace('DATA',literal(payload))
    bb.call('risky_eval',{'code':code})
    model=json.loads(embedded(bb.call('export_model',{'codec_id':'java_block','result_format':'embedded','max_content_length':2000000})))
    bb.call('risky_eval',{'code':'(()=>{ModelProject.all.find(p=>p.uuid==='+literal(native_id)+').select();return true;})()'})
    model['elements']=[e for e in model['elements'] if e.get('name')!='fit_reference_do_not_export']
    assert len(model['elements'])==len(parts),(name,len(model['elements']),len(parts))
    for e in model['elements']:
        for key in ['from','to']:e[key]=[v+o for v,o in zip(e[key],[8,3,8])]
        for f in e['faces'].values():f['texture']='#0'
    model.pop('groups',None);model.pop('format_version',None);model['textures']={'0':'revoindustry:item/'+name};model['parent']='minecraft:block/block';model['render_type']='minecraft:cutout'
    model['display']={'gui':{'rotation':[25,225,0],'translation':[0,0,0],'scale':[1.15]*3},'ground':{'translation':[0,2,0],'scale':[.65]*3},'thirdperson_righthand':{'rotation':[75,0,0],'translation':[0,1,0],'scale':[.6]*3},'firstperson_righthand':{'rotation':[0,-35,0],'translation':[1,1,0],'scale':[.7]*3},'fixed':{'scale':[1]*3}}
    target=D/'models/item'/f'{name}.json';target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(model,indent=2))
    bb.call('risky_eval',{'code':'(()=>{Project.save_path='+literal((A/(name+'.bbmodel')).as_posix())+';Project.saved=true;return true;})()'})
    shot=bb.call('set_camera_angle',{'position':[25,16,-30],'target':[0,4,0],'projection':'orthographic','zoom':1.05,'max_size':900})
    for block in shot['content']:
        if block['type']=='image':
            png=base64.b64decode(block['data']);(A/(name+'-preview.png')).write_bytes(png);renders.append(Image.open(io.BytesIO(png)).convert('RGBA'))

# Worn armor is baked from the same native-authored boxes and box-UV offsets.
lines=['package dk.revo.revoindustry.feeding;','import net.minecraft.client.model.geom.builders.*;','import net.minecraft.client.model.geom.PartPose;','/** Generated from the native Blockbench sources by scripts/build_feeding_helmet_models.py. */','public final class FeedingHelmetGeometry {',' public static LayerDefinition create(int tier) {','  var mesh = new MeshDefinition(); var root = mesh.getRoot();','  for (String name : new String[]{"head","hat","body","right_arm","left_arm","right_leg","left_leg"}) root.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.ZERO);','  var head = root.getChild("head");','  switch (tier) {']
def f(v):return str(round(v,5))+'f'
for tier,parts in enumerate(allparts):
    lines.append('   case '+str(tier)+' -> {')
    for p in parts:
        a,b=p['from'],p['to'];args=[a[0],-b[1],a[2],b[0]-a[0],b[1]-a[1],b[2]-a[2]]
        lines.append('    head.addOrReplaceChild('+json.dumps(p['name'])+',CubeListBuilder.create().texOffs('+','.join(map(str,p['uv']))+').addBox('+','.join(map(f,args))+'),PartPose.ZERO);')
    lines.append('   }')
lines+=['   default -> throw new IllegalArgumentException("Unknown helmet tier");','  }','  return LayerDefinition.create(mesh,128,128);',' }','}']
(R/'revoindustry/src/main/java/dk/revo/revoindustry/feeding/FeedingHelmetGeometry.java').write_text('\n'.join(lines)+'\n')
sheet=Image.new('RGB',(1500,600),(231,230,218));draw=ImageDraw.Draw(sheet)
for i,im in enumerate(renders):
    im.thumbnail((500,550),Image.Resampling.LANCZOS);bg=Image.new('RGBA',im.size,(231,230,218,255));bg.alpha_composite(im);sheet.paste(bg.convert('RGB'),(i*500+(500-im.width)//2,0));draw.text((i*500+25,565),['I - 1 slot','II - 4 slots','III - 25 slots'][i],fill=(40,45,30))
sheet.save(A/'three-tiers.png')
(A/'geometry.json').write_text(json.dumps(allparts,indent=2))
print(A/'three-tiers.png')
