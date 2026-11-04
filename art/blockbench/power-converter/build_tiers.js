(async()=>{
const fs=require('fs'),root="C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition",dir=root+'/art/blockbench/power-converter',assets=root+'/revoindustry/src/main/resources/assets/revoindustry',source=JSON.parse(fs.readFileSync(dir+'/power_converter.bbmodel','utf8')),result=[];
for(const name of ['power_converter','advanced_power_converter','elite_power_converter']){
 newProject(Formats.java_block);Codecs.project.parse(JSON.parse(JSON.stringify(source)));Project.name=name;const elements=Cube.all.slice(),textures=Texture.all.slice();Undo.initEdit({elements,textures,outliner:true});
 for(const tex of textures){const part=tex.name.replace('.png','');tex.namespace='revoindustry';tex.folder='block/'+name;tex.fromDataURL('data:image/png;base64,'+fs.readFileSync(assets+'/textures/block/'+name+'/'+part+'.png').toString('base64'));tex.uv_width=tex.uv_height=16;}
 await new Promise(ok=>setTimeout(ok,40));Undo.finishEdit('Apply shared tier materials');Canvas.updateAll();const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revoindustry:block/'+name+'/side';fs.writeFileSync(assets+'/models/block/'+name+'.json',JSON.stringify(raw,null,2));Project.save_path=dir+'/'+name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;result.push({name,cubes:elements.length});
}return result;
})()
