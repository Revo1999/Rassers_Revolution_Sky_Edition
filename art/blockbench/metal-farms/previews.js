(async()=>{
const fs=require('fs'),dir='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition/art/blockbench/metal-farms/previews';
for(const kind of ['steel_tree_grower','steel_mob_harvester','iron_greenhouse']){
 ModelProject.all.filter(p=>p.name===kind+'_assembled').at(-1).select();Canvas.updateAll();const v=Preview.selected;
 v.controls.target.set(8,kind==='steel_tree_grower'?7:7.5,8);
 for(const [angle,position] of [['front',[39,24,-36]],['rear',[39,24,44]]]){
  v.camera.position.set(...position);v.controls.update();await new Promise(ok=>v.screenshot({width:1000,height:900},data=>{fs.writeFileSync(dir+'/'+kind+'-'+angle+'.png',Buffer.from(data.split(',')[1],'base64'));ok();}));
 }
}
return {previews:dir};
})()
