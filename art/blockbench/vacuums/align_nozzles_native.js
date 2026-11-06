(async()=>{
 const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition',dir=root+'/art/blockbench/vacuums',report=[];
 for(let tier=1;tier<=3;tier++){
  const path=dir+'/vacuum_'+tier+'.bbmodel',data=JSON.parse(fs.readFileSync(dir+'/vacuum_'+tier+'.json','utf8'));let project=ModelProject.all.slice().reverse().find(p=>p.save_path===path);
  if(project)project.select();else{newProject(Formats.java_block);Codecs.project.parse(JSON.parse(fs.readFileSync(path,'utf8')),path);}
  Undo.initEdit({elements:Cube.all});
  for(const part of data.elements){const cube=Cube.all.find(c=>c.name===part.name);if(!cube)throw new Error('Missing part '+part.name);cube.from=part.from.slice();cube.to=part.to.slice();for(const [face,f] of Object.entries(part.faces))cube.faces[face].uv=f.uv.slice();}
  Undo.finishEdit('Align nozzle mouths flush with pickup origin and preserve per-face texel density');Canvas.updateAll();
  fs.writeFileSync(path,Codecs.project.compile());Project.saved=true;
  const output=JSON.parse(Codecs.java_block.compile());for(const [k,v] of Object.entries(output.textures||{}))if(typeof v==='object')output.textures[k]=v.sprite;output.textures.particle=data.textures.metal;output.display=data.display;
  fs.writeFileSync(root+'/revoindustry/src/main/resources/assets/revoindustry/models/block/vacuum_'+tier+'.json',JSON.stringify(output,null,2));
  const view=Preview.selected;view.controls.target.set(0,8,-.5);view.camera.position.set(25,28,-36);view.controls.update();Canvas.updateAll();
  await new Promise(ok=>view.screenshot({width:700,height:700},url=>{fs.writeFileSync(dir+'/vacuum_'+tier+'-preview.png',Buffer.from(url.split(',')[1],'base64'));ok();}));report.push({tier,cubes:Cube.all.length,nozzleFront:Math.min(...Cube.all.filter(c=>c.name.startsWith('flange_')).map(c=>c.from[2]))});
 }return report;
})()
