(async()=>{
 const fs=require('fs'),dir='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition/art/blockbench/furnaces';
 fs.mkdirSync(dir+'/previews',{recursive:true});const defs=JSON.parse(fs.readFileSync(dir+'/preview-input.json','utf8')),report=[];
 for(const def of defs){
  newProject(Formats.free);Project.name=def.name;Project.texture_width=Project.texture_height=16;
  Undo.initEdit({elements:[],textures:[],outliner:true});const textures={};for(const [name,data] of Object.entries(def.textures))textures[name]=new Texture({name:name+'.png',uv_width:16,uv_height:16}).fromDataURL(data).add(false);
  const groups={};for(const cube of def.cubes){if(!groups[cube.kind])groups[cube.kind]=new Group({name:cube.kind}).init();const c=new Cube({name:cube.name,from:cube.from,to:cube.to,box_uv:false}).addTo(groups[cube.kind]).init();for(const [face,f] of Object.entries(c.faces)){
   let texture='casing';if(['controller','input','output'].includes(cube.kind)&&face==='north')texture=cube.kind;if(cube.kind==='vent'&&face==='up')texture='vent';if(cube.kind==='fuel'&&!['up','down'].includes(face))texture='fuel';f.texture=textures[texture].uuid;f.uv=[0,0,16,16];
  }}Undo.finishEdit('Build native MI-style furnace assembly with casing-adapted hatches and roof vent');await new Promise(ok=>setTimeout(ok,80));Canvas.updateAll();
  Project.save_path=dir+'/'+def.name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
  const view=Preview.selected;view.controls.target.set(24,22,24);
  for(const [angle,pos] of [['front',[80,85,-80]],['rear',[-65,85,110]]]){view.camera.position.set(...pos);view.controls.update();Canvas.updateAll();await new Promise(ok=>view.screenshot({width:850,height:750},data=>{fs.writeFileSync(dir+'/previews/'+def.name+'-'+angle+'.png',Buffer.from(data.split(',')[1],'base64'));ok();}));}
  report.push({name:def.name,cubes:Cube.all.length,textures:Texture.all.length});
 }
 fs.writeFileSync(dir+'/native-model-report.json',JSON.stringify(report,null,2));return report;
})()
