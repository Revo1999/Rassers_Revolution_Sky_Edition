"""Small native-density Java salt geode. Geometry/export use desktop Blockbench MCP.
Balanced 10-cube prop, vanilla opaque/cutout shading, 16 pixels/block, no PBR;
growth is a renderer scale driven by paid greenhouse progress, no baked animation.
Textures are Mojang's pinned 1.21.1 client assets, referenced by ID at runtime.
"""
from pathlib import Path
import json,base64,zipfile
from blockbench_mcp_client import Blockbench,literal,embedded

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/salt-geode'
DEST=ROOT/'revocultivation/src/main/resources/assets/revocultivation'
def main():
    bb=Blockbench();bb.call('get_capabilities',{'include_tools':True})
    jar=zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
    textures={name:'data:image/png;base64,'+base64.b64encode(jar.read(f'assets/minecraft/textures/block/{name}.png')).decode() for name in ['calcite','quartz_block_side']}
    cubes=[{'name':'geode_bed','from':[2,0,2],'to':[14,2,14],'material':'calcite'}]
    for i,(x,z,w,h) in enumerate([(3,3,3,5),(8,3,2,7),(11,7,2,4),(3,9,3,6),(8,9,2,9),(7,6,2,5)]):
        cubes.append({'name':f'salt_crystal_{i}','from':[x,2,z],'to':[x+w,2+h,z+w],'material':'quartz_block_side'})
    data={'textures':textures,'cubes':cubes}
    code='''(async()=>{const d=DATA;newProject(Formats.java_block);Project.name="salt_crystal_growth";Project.texture_width=Project.texture_height=16;Project.box_uv=false;
    const tx={};for(const [n,source] of Object.entries(d.textures))tx[n]=new Texture({name:n+".png",uv_width:16,uv_height:16}).fromDataURL(source).add(false);
    const created=[];Undo.initEdit({elements:created,outliner:true});for(const p of d.cubes){const c=new Cube({name:p.name,from:p.from,to:p.to,autouv:0,box_uv:false}).init();created.push(c);
    for(const [n,f] of Object.entries(c.faces)){f.texture=tx[p.material].uuid;let axes=n==="up"||n==="down"?[0,2]:n==="east"||n==="west"?[2,1]:[0,1];f.uv=[0,0,p.to[axes[0]]-p.from[axes[0]],p.to[axes[1]]-p.from[axes[1]]];}}
    await new Promise(r=>setTimeout(r,80));Canvas.updateAll();Undo.finishEdit("Build salt crystal geode");return Cube.all.length;})()'''.replace('DATA',literal(data))
    bb.call('risky_eval',{'code':code})
    ART.mkdir(parents=True,exist_ok=True)
    native=embedded(bb.call('export_model',{'codec_id':'project','result_format':'embedded','max_content_length':2000000}))
    (ART/'salt_crystal_growth.bbmodel').write_text(native)
    model=json.loads(embedded(bb.call('export_model',{'codec_id':'java_block','result_format':'embedded','max_content_length':2000000})))
    model['textures']={k:'minecraft:block/'+Path(v).stem for k,v in model['textures'].items()}
    model['textures']['particle']='minecraft:block/calcite'
    target=DEST/'models/block/salt_crystal_growth.json';target.write_text(json.dumps(model,indent=2)+'\n')
    (DEST/'models/item/salt_crystal_starter.json').write_text(json.dumps({'parent':'revocultivation:block/salt_crystal_growth','display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[1.25,1.25,1.25]},'ground':{'scale':[.5,.5,.5]},'fixed':{'scale':[.8,.8,.8]}}},indent=2)+'\n')
    shot=bb.call('set_camera_angle',{'position':[26,21,30],'target':[8,5,8],'projection':'orthographic','zoom':.65,'max_size':700})
    for content in shot['content']:
        if content['type']=='image':(ART/'preview.png').write_bytes(base64.b64decode(content['data']))
    bb.call('risky_eval',{'code':'(()=>{Project.save_path='+literal((ART/'salt_crystal_growth.bbmodel').as_posix())+';Project.saved=true;return true;})()'})
    print('Exported salt geode:',len(model['elements']),'cubes')

if __name__=='__main__':main()
