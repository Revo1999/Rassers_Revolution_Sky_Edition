(async()=>{
 const fs=require('fs'),root="C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition",dir=root+'/art/blockbench/vacuums',report=[];
 for(let tier=1;tier<=3;tier++){
  const data=JSON.parse(fs.readFileSync(dir+'/vacuum_'+tier+'.json','utf8'));
  newProject(Formats.java_block);Project.name='vacuum_'+tier;Project.texture_width=Project.texture_height=16;
  Undo.initEdit({elements:[],textures:[],outliner:true});const textures={};
  for(const [name,path] of Object.entries(data.textures)){if(name==='particle')continue;const png=fs.readFileSync(root+'/revoindustry/src/main/resources/assets/'+path.replace(':','/textures/')+'.png');textures[name]=new Texture({name:path.split('/').pop()+'.png',folder:'block',namespace:'revoindustry',id:name,uv_width:16,uv_height:16}).fromDataURL('data:image/png;base64,'+png.toString('base64')).add(false);}
  const g=new Group({name:'single_block_vacuum'}).init();
  for(const part of data.elements){const c=new Cube({name:part.name,from:part.from,to:part.to,rotation:part.rotation?[0,0,part.rotation.angle]:[0,0,0],origin:part.rotation?part.rotation.origin:[8,8,8],box_uv:false}).addTo(g).init();for(const [face,f] of Object.entries(part.faces)){c.faces[face].texture=textures[f.texture.slice(1)].uuid;c.faces[face].uv=f.uv;}}
  Undo.finishEdit('Build industrial vacuum with large hollow intake pipe tier '+tier);Canvas.updateAll();
  Project.save_path=dir+'/vacuum_'+tier+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
  const output=JSON.parse(Codecs.java_block.compile());for(const [k,v] of Object.entries(output.textures||{}))if(typeof v==='object')output.textures[k]=v.sprite;output.textures.particle=data.textures.metal;output.display=data.display;
  fs.writeFileSync(root+'/revoindustry/src/main/resources/assets/revoindustry/models/block/vacuum_'+tier+'.json',JSON.stringify(output,null,2));
  const view=Preview.selected;view.controls.target.set(0,8,-.5);view.camera.position.set(25,28,-36);view.controls.update();Canvas.updateAll();await new Promise(ok=>setTimeout(ok,100));await new Promise(ok=>view.screenshot({width:700,height:700},url=>{fs.writeFileSync(dir+'/vacuum_'+tier+'-preview.png',Buffer.from(url.split(',')[1],'base64'));ok();}));
  report.push({tier,cubes:Cube.all.length,bounds:[0,16],textures:Texture.all.length});
 }fs.writeFileSync(dir+'/validation.json',JSON.stringify(report,null,2));return report;
})()
