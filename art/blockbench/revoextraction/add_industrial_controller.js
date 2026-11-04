(() => {
const fs=require('fs');const p=revoTexturePass;
ModelProject.all.find(x=>x.name==='wooden_drill_assembled').select();
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');ctx.imageSmoothingEnabled=false;ctx.drawImage(Texture.all[0].img,0,0,512,512);
let cursor=4,y=384,row=0;const cache=new Map();
function rect(x,y,w,h,c){ctx.fillStyle=c;ctx.fillRect(x,y,w,h);}
function tile(mat,w,h){
 let key=mat+':'+w.toFixed(4)+':'+h.toFixed(4);if(cache.has(key))return cache.get(key);
 let W=Math.max(1,Math.ceil(w)),H=Math.max(1,Math.ceil(h));if(cursor+W+3>512){cursor=4;y+=row+3;row=0;}if(y+H+3>512)throw Error('Atlas exhausted');
 let u=cursor,v=y;const base=mat==='steel'?[95,104,112]:mat==='amber'?[158,111,31]:mat==='button'?[63,69,74]:[39,43,47];
 for(let yy=0;yy<H;yy++)for(let xx=0;xx<W;xx++){
  let n=((xx*11+yy*17)%7)-3;if((xx+yy)%2===0&&((xx*3+yy*5)%11===0))n-=5;
  if(xx===0||yy===0)n+=13;if(xx===W-1||yy===H-1)n-=15;
  rect(u+xx,v+yy,1,1,`rgb(${base.map(c=>Math.max(0,c+n)).join(',')})`);
 }
 if(mat==='panel'){
  rect(u+1,v+1,12,6,'#11161b');rect(u+2,v+2,10,4,'#243e2c');
  rect(u+2,v+2,7,1,'#75aa5b');rect(u+2,v+3,8,2,'#4f873e');rect(u+2,v+5,10,1,'#2d5a31');
  rect(u+10,v+2,2,2,'#c6a64b');rect(u+2,v+8,10,1,'#777e83');
  for(let x=2;x<12;x+=3)rect(u+x,v+11,2,1,'#111519');
  for(let x of [1,12]){rect(u+x,v+1,1,1,'#828c95');rect(u+x,v+12,1,1,'#161a1d');}
 }
 if(mat==='shell'&&W>=8&&H>=8){for(let xx of [2,W-3])for(let yy of [2,H-3])rect(u+xx,v+yy,1,1,'#121619');}
 let r=[u,v,u+w,v+h];cursor+=W+3;row=Math.max(row,H);cache.set(key,r);return r;
}
function make(assembled=false){
 const scale=assembled?3:1,offset=assembled?[16,16,0]:[0,0,0];
 const body=assembled?Group.all.find(g=>g.name==='body'):new Group({name:'controller'}).init();
 const newCubes=[];
 function cube(name,a,b,mat){let c=new Cube({name:'industrial_controller_'+name,from:a.map((v,i)=>(v+offset[i])/scale),to:b.map((v,i)=>(v+offset[i])/scale),box_uv:false,autouv:0}).addTo(body).init();
  const d=b.map((v,i)=>v-a[i]);for(const [face,f] of Object.entries(c.faces)){
   let wh=['up','down'].includes(face)?[d[0],d[2]]:['east','west'].includes(face)?[d[2],d[1]]:[d[0],d[1]];
   f.uv=tile(mat==='panel'&&face!=='north'?'shell':mat,wh[0],wh[1]).slice();f.texture=Texture.all[0].uuid;
  }newCubes.push(c);return c;
 }
 cube('housing',[0,0,1],[16,16,assembled?6:16],'shell');
 cube('rim_left',[0,0,0],[1,16,1],'steel');cube('rim_right',[15,0,0],[16,16,1],'steel');
 cube('rim_top',[1,15,0],[15,16,1],'steel');cube('rim_bottom',[1,0,0],[15,1,1],'steel');
 cube('display_panel',[1,1,.25],[15,15,.70],'panel');
 cube('selector',[4,3,-.25],[6,5,.25],'button');cube('confirm_button',[10,3,-.25],[12,5,.25],'amber');
 if(assembled)cube('rear_mount',[1,22.15-16,6],[15,16,9.1],'shell');
 return newCubes;
}
const result=[];
for(const name of ['wooden_drill_controller','wooden_drill_assembled','wooden_drill_unassembled']){
 ModelProject.all.find(x=>x.name===name).select();Undo.initEdit({elements:Cube.all,textures:Texture.all,outliner:true});
 if(name==='wooden_drill_controller'){
  for(const c of Cube.all.slice())c.remove();for(const g of Group.all.slice())g.remove();make(false);
 }else if(name==='wooden_drill_assembled'){
  for(const c of Cube.all.filter(c=>c.name.startsWith('industrial_controller_')))c.remove();make(true);
 }else{
  let g=Group.all.find(g=>g.name==='controller_0_1_1');
  for(const c of Cube.all.filter(c=>c.parent===g))c.remove();
  const component=ModelProject.all.find(x=>x.name==='wooden_drill_controller');
  const native=component.elements.filter(c=>c instanceof Cube);
  for(const c of native){let e=c.getSaveCopy();e.uuid=guid();e.from=e.from.map((v,i)=>(v+[16,16,0][i])/3);e.to=e.to.map((v,i)=>(v+[16,16,0][i])/3);e.origin=[0,0,0];for(const f of Object.values(e.faces))if(f.texture!==null)f.texture=Texture.all[0].uuid;new Cube(e).addTo(g).init();}
 }
 Canvas.updateAll();Undo.finishEdit('Add full-height industrial drill controller');result.push({name,cubes:Cube.all.length});
}
const image=canvas.toDataURL('image/png');
fs.writeFileSync(p.assets+'/textures/block/wooden_drill_atlas.png',Buffer.from(image.split(',')[1],'base64'));
globalThis.revoControllerAtlas=image;
for(const project of ModelProject.all.filter(x=>x.save_path.startsWith(p.dir+'/')&&x.name.startsWith('wooden_drill_'))){
 project.select();Undo.initEdit({textures:Texture.all});for(const t of Texture.all){t.fromDataURL(image);t.uv_width=t.uv_height=512;}Canvas.updateAll();Undo.finishEdit('Append controller atlas patches');
}
ModelProject.all.find(x=>x.name==='wooden_drill_assembled').select();
return {updated:result,controllerExtentPhysical:{min:[16,16,0],max:[32,32,6]},newPatches:cache.size};
})()
