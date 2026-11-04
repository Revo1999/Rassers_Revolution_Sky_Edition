(async()=>{
const fs=require('fs');
const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/first-tier-machines',assets=root+'/revocultivation/src/main/resources/assets/revocultivation';
const backup=dir+'/history/pre-cage-textures';fs.mkdirSync(backup,{recursive:true});
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');ctx.imageSmoothingEnabled=false;
const original=new Image();await new Promise((ok,no)=>{original.onload=ok;original.onerror=no;original.src='data:image/png;base64,'+fs.readFileSync(dir+'/textures/first_tier_atlas.png').toString('base64')});ctx.drawImage(original,0,0);
let packX=4,packY=200,rowHeight=0;const islands=new Map(),audit=[],projects=[];
const clamp=v=>Math.max(0,Math.min(255,v));
function pixel(x,y,rgb){ctx.fillStyle='rgb('+rgb.map(clamp).join(',')+')';ctx.fillRect(x,y,1,1);}
function island(role,w,h,variant){
 const key=[role,w.toFixed(3),h.toFixed(3),variant].join(':');if(islands.has(key))return islands.get(key);
 const W=Math.ceil(w-1e-6),H=Math.ceil(h-1e-6),pad=3;
 if(packX+W+2*pad>508){packX=4;packY+=rowHeight;rowHeight=0;}
 if(packY+H+2*pad>508)throw new Error('Harvester atlas packing overflow');
 const u=packX+pad,v=packY+pad;packX+=W+2*pad;rowHeight=Math.max(rowHeight,H+2*pad);
 const base=role==='stile'?[64,73,70]:role==='plate'?[98,106,101]:[86,96,92];
 // Low-contrast clustered wear, with opaque padding. No periodic bolt tile on bars.
 for(let y=-pad;y<H+pad;y++)for(let x=-pad;x<W+pad;x++){
  const a=Math.max(0,Math.min(W-1,x)),b=Math.max(0,Math.min(H-1,y));
  const cluster=((Math.floor(a/2)*17+Math.floor(b/3)*11+variant*23)%13);
  const d=cluster===0?-6:cluster===3?4:0;let color=base.map(c=>c+d);
  if(role!=='bar'){
   if(b===0&&H>1)color=base.map(c=>c+22);
   else if(b===H-1&&H>2)color=base.map(c=>c-19);
   else if(a===0&&W>2)color=base.map(c=>c+12);
   else if(a===W-1&&W>2)color=base.map(c=>c-14);
  }
  pixel(u+x,v+y,color);
 }
 const dot=(x,y,color)=>{if(x>=0&&y>=0&&x<W&&y<H)pixel(u+x,v+y,color);};
 if(role==='rail'&&W>=10&&H>=2){for(const x of [2,W-3]){dot(x,H-1,[46,55,52]);dot(x,0,[145,150,138]);}}
 if(role==='stile'&&H>=12){for(const y of [3,H-4]){dot(Math.floor(W/2),y,[34,42,39]);dot(Math.floor(W/2),y-1,[119,130,120]);}}
 if(role==='plate'&&W>=4&&H>=4){for(const x of [1,W-2])for(const y of [1,H-2]){dot(x,y,[47,55,51]);if(y>0)dot(x,y-1,[151,155,138]);}}
 if(role==='vent'&&W>=8&&H>=8)for(let y=2;y<H-1;y+=3)for(let x=2;x<W-2;x++)dot(x,y,[38,46,43]);
 // Sparse warm oxidation, kept away from the silhouettes and small bolts.
 if(role==='rail'&&W>12&&H>2){dot(7,H-2,[110,98,77]);dot(W-9,H-2,[91,84,67]);}
 const uv=[u,v,u+w,v+h];islands.set(key,uv);return uv;
}
for(const name of ['mob_harvester_assembled','barred_panel','mob_filter','mob_harvester_controller']){
 let p=ModelProject.all.findLast(p=>p.save_path===dir+'/'+name+'.bbmodel');
 if(p)p.select();else{Codecs.project.load(JSON.parse(fs.readFileSync(dir+'/'+name+'.bbmodel','utf8')),{path:dir+'/'+name+'.bbmodel'});p=Project;}
 const runtimeName=name==='mob_harvester_assembled'?'mob_harvester_body':name;
 if(!fs.existsSync(backup+'/'+name+'.bbmodel'))fs.writeFileSync(backup+'/'+name+'.bbmodel',JSON.stringify(Codecs.project.compile({raw:true}),null,2));
 if(!fs.existsSync(backup+'/'+runtimeName+'.json'))fs.copyFileSync(assets+'/models/block/'+runtimeName+'.json',backup+'/'+runtimeName+'.json');
 const scale=name==='mob_harvester_assembled'?3:1,tex=Texture.all[0];
 Undo.initEdit({elements:[...Cube.all],textures:[tex],bitmap:true});
 let painted=0,wood=0;
 for(const c of Cube.all){
  const n=c.name;let role=null;
  if(n.includes('inset_bar_'))role='bar';
  else if(n.includes('pillar_stile_'))role='stile';
  else if(n.startsWith('framed_cage_')||/^iron_(header|sill|waist_rail)$/.test(n))role='rail';
  else if(n==='roof_vent')role='vent';
  else if(/^(filter_selector_plate|attached_selector_plate|weathered_rim|hatch_iron_rim|hatch_lower_lip|lantern_cap_|lantern_foot_)/.test(n))role='plate';
  const plank=/^plank_(crossbeam|sidebeam)_/.test(n);
  if(!role&&!plank)continue;
  const dims=c.to.map((v,i)=>(v-c.from[i])*scale),a=c.from.map(v=>v*scale),b=c.to.map(v=>v*scale);
  for(const face of ['north','south','east','west','up','down']){
   const wh=['up','down'].includes(face)?[dims[0],dims[2]]:['east','west'].includes(face)?[dims[2],dims[1]]:[dims[0],dims[1]];
   if(role){
    const variant=face==='up'?1:face==='down'?2:0;c.faces[face].uv=[...island(role,wh[0],wh[1],variant)];
    audit.push({model:name,cube:n,face,role,size:wh,uv:c.faces[face].uv});painted++;
   }else{
    const mod=v=>((v%16)+16)%16;
    const u=['east','west'].includes(face)?mod(a[2]):mod(a[0]),v=['up','down'].includes(face)?mod(a[2]):mod(-b[1]);
    c.faces[face].uv=[u,v,u+wh[0],v+wh[1]];wood++;
   }
  }
 }
 projects.push({project:p,tex,runtimeName,painted,wood});
 // All material islands must be packed before distributing the same atlas to each project.
 Undo.finishEdit('Remap cage iron and align timber grain');
}
const atlas=canvas.toDataURL('image/png');fs.writeFileSync(dir+'/textures/harvester_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(assets+'/textures/block/harvester_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
for(const {project:p,tex,runtimeName}of projects){
 p.select();Undo.initEdit({textures:[tex],bitmap:true});tex.name='harvester_atlas.png';tex.folder='block';tex.namespace='revocultivation';tex.fromDataURL(atlas);tex.uv_width=tex.uv_height=512;await new Promise(r=>setTimeout(r,120));Canvas.updateAll();Undo.finishEdit('Paint weathered iron cage atlas');
 fs.writeFileSync(p.save_path,JSON.stringify(Codecs.project.compile({raw:true}),null,2));
 const exports=Cube.all.map(c=>({c,value:c.export}));for(const c of Cube.all)c.export=c.parent.name==='body';
 const json=Codecs.java_block.compile({raw:true});delete json.format_version;delete json.groups;json.parent='minecraft:block/block';json.render_type='minecraft:cutout';json.textures={'0':'revocultivation:block/harvester_atlas',particle:p.name==='mob_harvester_controller'?'minecraft:block/stone_bricks':'minecraft:block/iron_block'};
 fs.writeFileSync(assets+'/models/block/'+runtimeName+'.json',JSON.stringify(json,null,2));for(const {c,value}of exports)c.export=value;p.saved=true;
 if(p.name==='mob_harvester_assembled'){
  const preview=Preview.selected;preview.camera.position.set(18,17,-36);preview.controls.target.set(8,8,8);preview.controls.update();Canvas.updateVisibility();await new Promise(r=>setTimeout(r,180));preview.render();
  const img=await new Promise(r=>preview.screenshot({width:1000,height:800},r));fs.writeFileSync(dir+'/previews/harvester-textures.png',Buffer.from(img.split(',')[1],'base64'));
 }
}
ModelProject.all.findLast(p=>p.name==='mob_harvester_assembled').select();
const report={texture:'harvester_atlas.png',size:[512,512],islands:islands.size,usedBottom:packY+rowHeight,materialFaces:audit.length,geometryChanged:false,faces:audit};
fs.writeFileSync(dir+'/cage-texture-audit.json',JSON.stringify(report,null,2));
return {islands:report.islands,usedBottom:report.usedBottom,faces:report.materialFaces,projects:projects.map(p=>({name:p.project.name,ironFaces:p.painted,woodFaces:p.wood}))};
})()
