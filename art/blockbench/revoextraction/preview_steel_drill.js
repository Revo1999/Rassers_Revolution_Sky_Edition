(async()=>{
const fs=require('fs'),dir='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition/art/blockbench/revoextraction/steel';
ModelProject.all.filter(p=>p.name==='steel_drill_assembled').at(-1).select();Canvas.updateAll();
const view=Preview.selected;view.controls.target.set(8,7.5,8);
for(const [name,position] of [['front-left',[-29,23,-33]],['front-right',[44,23,-33]],['rear-left',[-29,24,45]],['underside',[36,4,-32]]]){
 view.camera.position.set(...position);view.controls.update();Canvas.updateAll();
 await new Promise(resolve=>view.screenshot({width:1000,height:900},data=>{fs.writeFileSync(dir+'/'+name+'.png',Buffer.from(data.split(',')[1],'base64'));resolve();}));
}
view.camera.position.set(-29,23,-33);view.controls.update();return {preview:dir+'/front-left.png',cubes:Cube.all.length};
})()
