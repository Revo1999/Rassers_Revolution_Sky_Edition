(async () => {
const fs=require('fs'),p=revoTexturePass;
ModelProject.all.find(x=>x.name==='wooden_drill_assembled').select();
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');ctx.imageSmoothingEnabled=false;ctx.drawImage(Texture.all[0].img,0,0,512,512);
const stone=new Image();await new Promise((resolve,reject)=>{stone.onload=resolve;stone.onerror=reject;stone.src='data:image/png;base64,'+fs.readFileSync(p.dir+'/textures/stone_panel/blocks/stone.png').toString('base64');});
let cursor=4,row=0,y=448;const cache=new Map();
function rect(x,y,w,h,c){ctx.fillStyle=c;ctx.fillRect(x,y,w,h);}
function tile(mat,w,h){
 let key=mat+':'+w.toFixed(3)+':'+h.toFixed(3);if(cache.has(key))return cache.get(key).slice();
 let W=Math.max(1,Math.ceil(w)),H=Math.max(1,Math.ceil(h));if(cursor+W+3>512){cursor=4;y+=row+3;row=0;}if(y+H+3>512)throw Error('Atlas full');const u=cursor,v=y;
 const earthy=mat==='stone'||mat==='rim'||mat==='panel';
 if(earthy){for(let yy=0;yy<H;yy++)for(let xx=0;xx<W;xx++)ctx.drawImage(stone,xx%16,yy%16,1,1,u+xx,v+yy,1,1);}
 else{rect(u,v,W,H,mat==='ceramic'?'#b7af92':'#534533');}
 let image=ctx.getImageData(u,v,W,H);const a=image.data;
 for(let yy=0;yy<H;yy++)for(let xx=0;xx<W;xx++){
  let i=(yy*W+xx)*4,n=0;
  if(mat==='rim')n-=17;
  if(xx===0||yy===0)n+=10;if(xx===W-1||yy===H-1)n-=18;
  if((xx*7+yy*11)%17===0)n+=(xx+yy)%2?5:-7;
  for(let k=0;k<3;k++)a[i+k]=Math.max(0,Math.min(255,a[i+k]+n));
 }
 ctx.putImageData(image,u,v);
 if(mat==='panel'){
  // An ivory analogue meter behind a dark iron bezel; no digital screen.
  rect(u+1,v+1,12,8,'#4b4941');rect(u+2,v+2,10,6,'#c4ba98');rect(u+3,v+3,8,4,'#d7caa7');
  for(const [x,y] of [[3,3],[5,2],[8,2],[10,3]])rect(u+x,v+y,1,1,'#4a4235');
  rect(u+6,v+5,1,1,'#824734');rect(u+5,v+4,1,1,'#824734');rect(u+4,v+3,1,1,'#824734');rect(u+6,v+6,2,1,'#3f3932');
  rect(u+2,v+10,3,3,'#4a4032');rect(u+9,v+10,3,3,'#686458');
  for(const [x,y] of [[0,0],[13,0],[0,13],[13,13]])rect(u+x,v+y,1,1,'#59574e');
  rect(u+6,v+11,1,1,'#a28b59');rect(u+6,v+12,2,1,'#574d3b');
 }
 if(mat==='ceramic'&&W>=2&&H>=2){rect(u+1,v,1,H,'#e0d3af');rect(u,v+H-1,W,1,'#746d58');}
 let uv=[u,v,u+w,v+h];cache.set(key,uv);cursor+=W+3;row=Math.max(row,H);return uv.slice();
}
const result=[];
for(const name of ['wooden_drill_controller','wooden_drill_assembled','wooden_drill_unassembled']){
 ModelProject.all.find(x=>x.name===name).select();const cubes=Cube.all.filter(c=>c.name.startsWith('industrial_controller_'));
 Undo.initEdit({elements:cubes,textures:Texture.all});const scale=name==='wooden_drill_controller'?1:3;
 for(const c of cubes){
  const label=c.name.replace('industrial_controller_','');
  if(label==='selector'){c.to[1]+=1/scale;c.from[2]-=.5/scale;}
  let d=c.to.map((v,i)=>v-c.from[i]);
  for(const [face,f] of Object.entries(c.faces)){
   if(f.texture===null)continue;
   const mat=label==='display_panel'&&face==='north'?'panel':label.startsWith('rim_')?'rim':label==='selector'?'bakelite':label==='confirm_button'?'ceramic':'stone';
   const wh=['up','down'].includes(face)?[d[0],d[2]]:['east','west'].includes(face)?[d[2],d[1]]:[d[0],d[1]];
   f.uv=tile(mat,wh[0]*scale,wh[1]*scale);f.texture=Texture.all[0].uuid;
  }
 }
 Canvas.updateAll();Undo.finishEdit('Stone and analogue electrical panel for primitive drill');result.push({name,controllerPieces:cubes.length});
}
const data=canvas.toDataURL('image/png');fs.writeFileSync(p.assets+'/textures/block/wooden_drill_atlas.png',Buffer.from(data.split(',')[1],'base64'));
for(const project of ModelProject.all.filter(x=>x.save_path.startsWith(p.dir+'/')&&x.name.startsWith('wooden_drill_'))){project.select();Undo.initEdit({textures:Texture.all});for(const t of Texture.all){t.fromDataURL(data);t.uv_width=t.uv_height=512;}Canvas.updateAll();Undo.finishEdit('Append analogue panel patches');}
ModelProject.all.find(x=>x.name==='wooden_drill_assembled').select();return {updated:result,patches:cache.size,archive:'variants/modern-industrial'};
})()
