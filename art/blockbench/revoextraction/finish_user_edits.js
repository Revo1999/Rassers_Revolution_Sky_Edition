(() => {
const fs=require('fs');const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const tex=Texture.all[0],cubes=Cube.all.slice();
Undo.initEdit({elements:cubes,textures:[tex],outliner:true});
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');ctx.imageSmoothingEnabled=false;ctx.drawImage(tex.img,0,0,512,512);
let x=4,y=320,row=0;
function remap(c){
 let d=c.to.map((v,i)=>v-c.from[i]);
 for(const [name,f] of Object.entries(c.faces)){
  if(f.texture===null)continue;
  let wh=['up','down'].includes(name)?[d[0],d[2]]:['east','west'].includes(name)?[d[2],d[1]]:[d[0],d[1]];
  let W=wh[0]*3,H=wh[1]*3;const old=f.uv.slice();
  if(x+Math.ceil(W)+3>512){x=4;y+=row+3;row=0;}
  if(y+H+3>512)throw new Error('Patch space exhausted');
  for(let yy=0;yy<Math.ceil(H);yy++)for(let xx=0;xx<Math.ceil(W);xx++){
   ctx.drawImage(tex.img,Math.floor(old[0]+Math.min(xx,Math.abs(old[2]-old[0])-.01)),Math.floor(old[1]+Math.min(yy,Math.abs(old[3]-old[1])-.01)),1,1,x+xx,y+yy,1,1);
  }
  f.uv=[x,y,x+W,y+H];x+=Math.ceil(W)+3;row=Math.max(row,Math.ceil(H));
 }
}
const moved=[];
for(const c of cubes){
 if(['input_bracket','gear_disc_0','shaft_bearing','create_shaft_coupling'].includes(c.name)){
  for(const arr of [c.from,c.to,c.origin])arr[1]-=5/3;moved.push(c.name);
 }
 if(c.name.startsWith('stone_foot_')){
  const cap=cubes.find(x=>x.name==='footing_iron_cap_'+c.name);c.to[1]=cap.from[1];c.faces.up.texture=null;cap.faces.down.texture=null;remap(c);
 }
 if(c.name==='output_upper_connector'){
  c.to[1]=cubes.find(x=>x.name==='horizontal_side_rail_17_3').from[1];remap(c);
 }
 if(c.name==='hatchblock_output_lip'){c.from[1]+=1/6;c.to[1]+=1/6;c.origin[1]+=1/6;}
 if(c.name==='bit_axle'){
  c.from[1]=.8;c.from[0]=c.from[2]=7.35;c.to[0]=c.to[2]=8.65;remap(c);
 }
}
const gear=Group.all.find(g=>g.name==='drive_gear');gear.origin[1]=8;
tex.fromDataURL(canvas.toDataURL('image/png'));tex.uv_width=tex.uv_height=512;
fs.writeFileSync(root+'/revoextraction/src/main/resources/assets/revoextraction/textures/block/wooden_drill_atlas.png',Buffer.from(canvas.toDataURL('image/png').split(',')[1],'base64'));
Canvas.updateAll();Undo.finishEdit('Align drive, join final drill disc and remove coplanar overlaps');
return {preservedCubes:Cube.all.length,moved,gearPivot:gear.origin,shaftCentreY:(Cube.all.find(c=>c.name==='create_shaft_coupling').from[1]+Cube.all.find(c=>c.name==='create_shaft_coupling').to[1])/2,axleBottom:Cube.all.find(c=>c.name==='bit_axle').from[1]};
})()
