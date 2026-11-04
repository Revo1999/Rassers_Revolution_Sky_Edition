(() => {
const fs=require('fs');
const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/revoextraction';
const assets=root+'/revoextraction/src/main/resources/assets/revoextraction';
fs.mkdirSync(dir,{recursive:true});fs.mkdirSync(assets+'/textures/block',{recursive:true});
const materials={oak:0,timber:1,iron:2,stone:3,end:4,brass:5,dark:6,hole:7,door:8,strap:9};
function atlas(){
 const canvas=document.createElement('canvas');canvas.width=canvas.height=256;const c=canvas.getContext('2d');
 const palettes=[[153,104,43],[111,77,34],[117,121,128],[106,108,107],[144,101,43],[173,126,49],[64,68,75],[27,31,35],[133,90,36],[108,112,121]];
 for(let k=0;k<16;k++){
  let base=palettes[k%palettes.length];let ox=k%4*64,oy=Math.floor(k/4)*64;
  for(let y=0;y<64;y++)for(let x=0;x<64;x++){
   let n=((x*17+y*37+x*y*3+k*11)%13)-6;
   if(k===0||k===8){n+=((Math.floor(x/7)+Math.floor(y/4)*3)%5)-2;if(y%8===0)n-=27;if(y%8===1)n+=16;if((x+Math.floor(y/8)*19)%32===0)n-=15;}
   if(k===1){n+=((Math.floor(x/3)*7)%11)-5;if(x%8===0)n-=17;if(x%8===1)n+=9;}
   if(k===4){let r=Math.max(Math.abs(x%16-7.5),Math.abs(y%16-7.5));n+=(Math.floor(r)%3===0?-23:7);}
   if(k===3){if(y%12===0||(x+Math.floor(y/12)*9)%20===0)n-=25;if(y%12===1)n+=13;}
   if(k===2||k===6||k===9){n=Math.floor(n/2)+(y%16===0?13:0);}
   if(k===7)n=Math.floor(n/3);
   c.fillStyle=`rgb(${base.map(v=>Math.max(0,Math.min(255,v+n))).join(',')})`;c.fillRect(ox+x,oy+y,1,1);
  }
  if(k===9){for(let y=3;y<64;y+=16)for(let x=3;x<64;x+=16){c.fillStyle='#34383d';c.fillRect(ox+x,oy+y,4,4);c.fillStyle='#bfc1c3';c.fillRect(ox+x,oy+y,3,2);c.fillStyle='#787c83';c.fillRect(ox+x+1,oy+y+2,2,1);}}
 }
 return canvas.toDataURL('image/png');
}
globalThis.revoBuild=(name)=>{
 const assembled=name==='assembled',scale=assembled?3:1;
 Project.texture_width=Project.texture_height=256;Project.box_uv=false;
 Project.credit='Revo1999 — authored in Blockbench through MCP';
 const data=atlas();const texture=new Texture({name:'wooden_drill_atlas.png',id:'0',folder:'block',namespace:'revoextraction'}).fromDataURL(data).add(false);
 texture.uv_width=texture.uv_height=256;
 fs.writeFileSync(assets+'/textures/block/wooden_drill_atlas.png',Buffer.from(data.split(',')[1],'base64'));
 const groups={};const cubes=[];Undo.initEdit({elements:cubes,textures:[texture],outliner:true});
 function group(n,pivot=[0,0,0]){return groups[n]||(groups[n]=new Group({name:n,origin:pivot.map(v=>v/scale)}).init());}
 function cube(n,f,t,mat='oak',g='body',rotation=null,origin=null){
  const dims=t.map((v,i)=>v-f[i]);const id=materials[mat],u=id%4*64+2,v=Math.floor(id/4)*64+2;
  const props={name:n,from:f.map(v=>v/scale),to:t.map(v=>v/scale),box_uv:false,autouv:0};
  if(rotation)props.rotation=rotation;if(origin)props.origin=origin.map(v=>v/scale);
  let o=new Cube(props).addTo(group(g)).init();
  for(const face of ['north','south','east','west','up','down']){
   let size=face==='up'||face==='down'?[dims[0],dims[2]]:face==='east'||face==='west'?[dims[2],dims[1]]:[dims[0],dims[1]];
   o.faces[face].texture=texture.uuid;o.faces[face].uv=[u,v,u+size[0],v+size[1]];
  }
  cubes.push(o);return o;
 }
 function frame(f,t,axis='z',g='body'){
  const [x,y,z]=f,[X,Y,Z]=t;
  cube('frame_left_'+cubes.length,[x,y,z],[x+2,Y,Z],'strap',g);
  cube('frame_right_'+cubes.length,[X-2,y,z],[X,Y,Z],'strap',g);
  cube('frame_top_'+cubes.length,[x+2,Y-2,z],[X-2,Y,Z],'strap',g);
  cube('frame_bottom_'+cubes.length,[x+2,y,z],[X-2,y+2,Z],'strap',g);
 }
 function bit(cx,base,cz,g='drill_bit',size=1){
  group(g,[cx,base+13*size,cz]);
  cube('bit_axle',[cx-1.5*size,base+2*size,cz-1.5*size],[cx+1.5*size,base+18*size,cz+1.5*size],'dark',g);
  for(let i=0;i<7;i++){
   const width=(3+i*1.65)*size,y=base+i*1.8*size,angle=[0,22.5,45,-22.5][i%4];
   cube('helical_cutting_flight_'+i,[cx-width/2,y,cz-width/2],[cx+width/2,y+1.35*size,cz+width/2],'iron',g,[0,angle,0],[cx,y,cz]);
  }
 }
 if(name==='support'){
  cube('upright_oak_post',[0,0,0],[16,16,16],'timber');
  for(let y of [1,13]){cube('iron_tie_front_'+y,[0,y,-.1],[16,y+2,.6],'strap');cube('iron_tie_back_'+y,[0,y,15.4],[16,y+2,16.1],'strap');cube('iron_tie_left_'+y,[-.1,y,.6],[.6,y+2,15.4],'strap');cube('iron_tie_right_'+y,[15.4,y,.6],[16.1,y+2,15.4],'strap');}
  for(let face of ['up','down'])cubes[0].faces[face].uv=[2,66,18,82];
 } else if(name==='casing'){
  cube('planked_casing',[0,0,0],[16,16,16],'oak');
  for(let y of [1,13])cube('front_iron_band_'+y,[0,y,-.12],[16,y+2,.4],'strap');
 } else if(name==='controller'){
  cube('rear_casing',[0,0,3],[16,16,16],'oak');cube('recessed_door',[3,3,1],[13,13,3],'door');frame([1,1,0],[15,15,2]);cube('latch',[10,7,-.8],[13,9,1],'dark');
 } else if(name==='output_hatch'){
  cube('rear_shell',[0,0,12],[16,16,16],'oak');
  cube('left_wall',[0,0,1],[3,16,12],'oak');cube('right_wall',[13,0,1],[16,16,12],'oak');
  cube('top_wall',[3,13,1],[13,16,12],'oak');cube('bottom_wall',[3,0,1],[13,3,12],'oak');
  cube('dark_hatch_interior',[3,3,11],[13,13,12],'hole');frame([1,1,0],[15,15,1]);cube('output_lip',[2,1,-1],[14,3,3],'iron');
 } else if(name==='input'){
  cube('kinetic_housing',[0,0,1],[16,16,16],'oak');frame([2,2,0],[14,14,2]);
  cube('bearing',[5,5,-1],[11,11,1],'dark');cube('shaft_socket',[6,6,-2],[10,10,1],'iron');
 } else if(name==='core'){
  for(let x of [0,13])cube('core_frame_'+x,[x,0,0],[x+3,16,16],'oak');
  cube('top_crossbeam',[3,13,0],[13,16,16],'strap');cube('lower_crossbeam',[3,0,0],[13,2,16],'dark');bit(8,2,8,'drill_bit',.66);
 } else if(assembled){
  group('body');group('drill_bit',[24,18,24]);group('drive_gear',[46,29,24]);
  for(let x of [2,36])for(let z of [2,36]){
   cube('stone_foot_'+x+'_'+z,[x-1,0,z-1],[x+11,5,z+11],'stone');
   cube('timber_leg_'+x+'_'+z,[x+1,5,z+1],[x+9,43,z+9],'timber');
   for(let y of [16,39]){
    cube('corner_iron_front_'+x+'_'+z+'_'+y,[x,y,z-.5],[x+10,y+5,z+2],'strap');
    cube('corner_iron_side_'+x+'_'+z+'_'+y,[x-.5,y,z+2],[x+2,y+5,z+10],'strap');
   }
  }
  for(let y of [17,39]){
   for(let z of [3,39])cube('horizontal_front_rail_'+y+'_'+z,[11,y,z],[37,y+5,z+6],'oak');
   for(let x of [3,39])cube('horizontal_side_rail_'+y+'_'+x,[x,y,11],[x+6,y+5,37],'oak');
  }
  cube('mechanism_wooden_body',[9,22,9],[39,39,39],'oak');
  cube('front_maintenance_door',[17,24,7.5],[31,36,9],'door');frame([15,22,7],[33,38,8]);
  cube('door_latch',[26,28,6],[29,31,7.5],'dark');
  cube('top_bearing_lower',[17,43,17],[31,45,31],'dark');cube('top_bearing_oak',[18,45,18],[30,47,30],'oak');cube('top_bearing_cap',[20,47,20],[28,48,28],'iron');
  cube('bit_mount',[17,19,17],[31,22,31],'dark');bit(24,2,24);
  // Real open hatch aligned to the lower-front interaction block (north).
  cube('hatch_back',[19,6,6],[29,14,7],'hole');
  cube('hatch_left',[16,4,1],[19,16,8],'oak');cube('hatch_right',[29,4,1],[32,16,8],'oak');
  cube('hatch_top',[19,14,1],[29,16,8],'oak');cube('hatch_floor',[19,4,1],[29,6,8],'oak');
  frame([17,5,0],[31,15,1]);cube('output_chute_lip',[17,4,-1],[31,6,4],'iron');
  // Right hand side drive: shaft extends to the exact outer block face.
  cube('input_bracket',[39,23,18],[42,35,30],'strap');
  for(let angle of [0,45])cube('gear_disc_'+angle,[43,22.6,17.6],[46,35.4,30.4],'brass','drive_gear',[angle,0,0],[44.5,29,24]);
  for(let a=0;a<8;a++){
   const rad=a*Math.PI/4,dy=Math.cos(rad)*9,dz=Math.sin(rad)*9;
   let h=a%2?3:4,depth=a%2?4:3;
   cube('gear_tooth_'+a,[43,29+dy-h/2,24+dz-depth/2],[46,29+dy+h/2,24+dz+depth/2],'brass','drive_gear');
  }
  cube('shaft_bearing',[45.5,25,20],[47,33,28],'dark');cube('create_shaft_coupling',[46,27,22],[48,31,26],'iron');
 }
 if(assembled){
  for(const c of cubes.filter(c=>c.parent.name==='drill_bit')){
   for(const arr of [c.from,c.to,c.origin]){arr[0]=8+(arr[0]-8)*1.4;arr[2]=8+(arr[2]-8)*1.4;arr[1]=2/3+(arr[1]-2/3)*1.25;}
   for(const [name,f] of Object.entries(c.faces)){let d=c.to.map((v,i)=>v-c.from[i]);let wh=name==='up'||name==='down'?[d[0],d[2]]:name==='east'||name==='west'?[d[2],d[1]]:[d[0],d[1]];f.uv[2]=f.uv[0]+wh[0]*3;f.uv[3]=f.uv[1]+wh[1]*3;}
  }
  for(const c of cubes.filter(c=>c.name.startsWith('hatch_')||c.name==='output_chute_lip'||/^frame_.*_(56|57|58|59)$/.test(c.name))){
   for(const arr of [c.from,c.to]){let x=arr[0];arr[0]=arr[2];arr[2]=x;arr[1]+=1;}
   for(const [name,f] of Object.entries(c.faces)){let d=c.to.map((v,i)=>v-c.from[i]);let wh=name==='up'||name==='down'?[d[0],d[2]]:name==='east'||name==='west'?[d[2],d[1]]:[d[0],d[1]];f.uv[2]=f.uv[0]+wh[0]*3;f.uv[3]=f.uv[1]+wh[1]*3;}
  }
 }
 Canvas.updateAll();Undo.finishEdit('Build '+name+' wooden drill asset');
 globalThis.revoAsset={name,cubes,groups,texture,scale,root,dir,assets};
 return {name,cubes:cubes.length,quads:cubes.length*6,triangles:cubes.length*12,texture:[texture.width,texture.height],groups:Object.keys(groups)};
};
return {ready:true};
})()
