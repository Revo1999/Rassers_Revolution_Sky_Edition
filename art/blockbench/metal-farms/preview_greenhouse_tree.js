(async()=>{
const fs=require('fs'),dir='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition/art/blockbench/metal-farms/previews';
for(const name of ['iron_greenhouse_assembled','steel_tree_grower_assembled']){
 ModelProject.all.filter(p=>p.name===name).at(-1).select();const view=Preview.selected,tree=name.startsWith('steel');
 const glass=Texture.all.find(t=>t.name==='greenhouse_glass.png');if(glass)glass.getMaterial().depthWrite=false;for(const c of Cube.all)if(c.parent.name==='glass')c.mesh.renderOrder=10;
 view.controls.target.set(8,tree?12:5,8);
 for(const [angle,pos] of [['front',tree?[35,30,-35]:[28,20,-28]],['rear',tree?[35,30,43]:[28,20,36]]]){
  view.camera.position.set(...pos);view.controls.update();Canvas.updateAll();await new Promise(ok=>view.screenshot({width:1000,height:1100},data=>{fs.writeFileSync(dir+'/'+name+'-refined-'+angle+'.png',Buffer.from(data.split(',')[1],'base64'));ok();}));
 }
 if(tree){
  const growth=Group.all.find(g=>g.name==='real_tree_preview');
  Undo.initEdit({outliner:true,elements:Cube.all.slice()});growth.visibility=false;for(const c of growth.children)c.visibility=false;
  view.controls.target.set(8,2.5,8);view.camera.position.set(24,17,-24);view.controls.update();Canvas.updateAll();
  await new Promise(ok=>view.screenshot({width:1000,height:700},data=>{fs.writeFileSync(dir+'/steel-tree-base-detail.png',Buffer.from(data.split(',')[1],'base64'));ok();}));
  growth.visibility=true;for(const c of growth.children)c.visibility=true;Canvas.updateAll();Undo.finishEdit('Inspect solid planter base and restore real oak preview');Project.saved=true;
 }
 if(!tree){
  view.controls.target.set(8,5,8);view.camera.position.set(24,17,-24);view.controls.update();Canvas.updateAll();
  await new Promise(ok=>view.screenshot({width:1000,height:700},data=>{fs.writeFileSync(dir+'/greenhouse-glass-detail.png',Buffer.from(data.split(',')[1],'base64'));ok();}));
  const lamps=Group.all.find(g=>g.name==='lights');Undo.initEdit({outliner:true,elements:lamps.children.slice()});
  view.controls.target.set(8,5,8);view.camera.position.set(24,7,-24);view.controls.update();
  for(const on of [false,true]){
   lamps.visibility=on;for(const c of lamps.children)c.visibility=on;Canvas.updateAll();
   await new Promise(ok=>view.screenshot({width:1000,height:700},data=>{fs.writeFileSync(dir+'/greenhouse-led-'+(on?'on':'off')+'.png',Buffer.from(data.split(',')[1],'base64'));ok();}));
  }
  lamps.visibility=true;for(const c of lamps.children)c.visibility=true;Canvas.updateAll();Undo.finishEdit('Inspect white LED strip on and off states');Project.saved=true;
  const cropGroups=Group.all.filter(g=>g.name.startsWith('crop_'));Undo.initEdit({outliner:true,elements:Cube.all.slice()});
  for(const kind of ['wheat','carrot','potato','beetroot']){for(const g of cropGroups){g.visibility=g.name==='crop_'+kind;for(const c of g.children)c.visibility=g.visibility;}view.camera.position.set(28,20,-28);view.controls.target.set(8,5,8);view.controls.update();Canvas.updateAll();await new Promise(ok=>view.screenshot({width:900,height:900},data=>{fs.writeFileSync(dir+'/greenhouse-'+kind+'.png',Buffer.from(data.split(',')[1],'base64'));ok();}));}
  for(const g of cropGroups){g.visibility=g.name==='crop_wheat';for(const c of g.children)c.visibility=g.visibility;}Canvas.updateAll();Undo.finishEdit('Preview each real crop species and restore wheat');Project.saved=true;
 }
}
return {previews:dir};
})()
