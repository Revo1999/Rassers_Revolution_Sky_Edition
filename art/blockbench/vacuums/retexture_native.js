(async()=>{
 const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition',dir=root+'/art/blockbench/vacuums',report=[];
 for(let tier=1;tier<=3;tier++){
  const path=dir+'/vacuum_'+tier+'.bbmodel';let project=ModelProject.all.slice().reverse().find(p=>p.save_path===path);
  if(project)project.select();else{newProject(Formats.java_block);Codecs.project.parse(JSON.parse(fs.readFileSync(path,'utf8')),path);}
  Undo.initEdit({textures:Texture.all,bitmap:true});
  for(const texture of Texture.all){const png=fs.readFileSync(root+'/revoindustry/src/main/resources/assets/revoindustry/textures/block/'+texture.name);texture.fromDataURL('data:image/png;base64,'+png.toString('base64'));}
  await new Promise(ok=>setTimeout(ok,150));Undo.finishEdit('Repaint vacuum materials with four-tone single-pixel dithering');Canvas.updateAll();
  fs.writeFileSync(path,Codecs.project.compile());Project.saved=true;
  const view=Preview.selected;view.controls.target.set(0,8,-.5);view.camera.position.set(25,28,-36);view.controls.update();Canvas.updateAll();
  await new Promise(ok=>view.screenshot({width:700,height:700},url=>{fs.writeFileSync(dir+'/vacuum_'+tier+'-preview.png',Buffer.from(url.split(',')[1],'base64'));ok();}));
  report.push({tier,textures:Texture.all.length,cubes:Cube.all.length});
 }return report;
})()
