(() => {
const fs=require('fs');
const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/revoextraction',assets=root+'/revoextraction/src/main/resources/assets/revoextraction';
const backup=root+'/.local/drill-textures-v1';fs.mkdirSync(backup,{recursive:true});
const files=fs.readdirSync(dir).filter(n=>n.endsWith('.bbmodel'));
for(const file of files)if(!fs.existsSync(backup+'/'+file))fs.copyFileSync(dir+'/'+file,backup+'/'+file);
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;
const ctx=canvas.getContext('2d');ctx.fillStyle='#161719';ctx.fillRect(0,0,512,512);
const tiles=new Map();let cursorX=3,cursorY=3,rowHeight=0;
function hash(x,y,s=0){let v=Math.imul(x+73,374761393)^Math.imul(y+91,668265263)^Math.imul(s+17,1274126177);v=Math.imul(v^(v>>>13),1274126177);return ((v^(v>>>16))>>>0)/4294967295;}
function rgb(r,g,b){return `rgb(${Math.round(Math.max(0,Math.min(255,r)))},${Math.round(Math.max(0,Math.min(255,g)))},${Math.round(Math.max(0,Math.min(255,b)))})`;}
function pixel(x,y,color){ctx.fillStyle=color;ctx.fillRect(x,y,1,1);}
function rect(x,y,w,h,color){ctx.fillStyle=color;ctx.fillRect(x,y,w,h);}
function paint(u,v,w,h,mat){
 const bases={0:[162,110,46],1:[112,76,33],2:[116,121,130],3:[106,105,106],4:[141,99,43],5:[174,125,47],6:[49,53,61],7:[13,16,20],8:[151,98,38],9:[99,105,119],10:[40,43,47],11:[101,108,115],12:[8,11,14]};
 let b=bases[mat];
 for(let y=0;y<h;y++)for(let x=0;x<w;x++){
  let grain=0,n=(hash(Math.floor(x/2),Math.floor(y/2),mat)-.5)*7;
  if([0,8].includes(mat)){
   let plank=Math.floor(y/12),yy=y%12;
   grain=(hash(plank,mat)-.5)*19+(hash(Math.floor(x/12),Math.floor(y/2),mat)-.5)*12;
   if(yy===0)grain-=43;else if(yy===1)grain+=24;else if(yy===11)grain-=15;
   if((x+plank*23)%64===0)grain-=29;
  }
  if(mat===1){
   let stripe=Math.floor(x/4),bend=Math.floor(hash(stripe,Math.floor(y/18),3)*2);
   grain=(hash(stripe,4)-.5)*35+(hash(Math.floor(x/2),Math.floor(y/11),9)-.5)*12;
   if((x+bend)%7===0)grain-=24;if((x+bend)%7===1)grain+=13;
  }
  if(mat===4){let dx=x-w/2,dy=y-h/2,r=Math.max(Math.abs(dx),Math.abs(dy));grain=(Math.floor(r/2)%3===0?-22:8)+(hash(Math.floor(x/4),Math.floor(y/4))-.5)*10;}
  if(mat===3){let yy=y%12,xx=(x+Math.floor(y/12)*9)%20;grain=(hash(Math.floor(x/7),Math.floor(y/5),8)-.5)*24;if(yy===0||xx===0)grain-=32;else if(yy===1||xx===1)grain+=20;}
  if([2,5,6,9,10,11].includes(mat)){grain=(hash(Math.floor(x/5),Math.floor(y/3),mat)-.5)*8;n*=.4;}
  if([7,12].includes(mat)){grain=-Math.min(10,(w-x)/3)+y*.08;n=0;}
  let edge=Math.min(x,y,w-1-x,h-1-y),shade=0;
  if(![7,12].includes(mat)){
   if(y===0||x===0)shade=mat===1?13:25;
   if(y===h-1||x===w-1)shade=-31;
   if(edge===1)shade+=(x<2||y<2?7:-11);
  }
  pixel(u+x,v+y,rgb(b[0]+grain+n+shade,b[1]+grain*.8+n+shade,b[2]+grain*.55+n+shade));
 }
 // Sparse directional grain streaks; not repeated noise on every texel.
 if([0,1,8].includes(mat)){
  for(let i=0;i<Math.max(3,w*h/70);i++){
   let x=2+Math.floor(hash(i,mat,11)*Math.max(1,w-5)),y=2+Math.floor(hash(i,mat,21)*Math.max(1,h-5));
   let len=3+Math.floor(hash(i,mat,31)*10),light=i%4===0;
   rect(u+x,v+y,mat===1?1:Math.min(len,w-x-2),mat===1?Math.min(len,h-y-2):1,light?'#b98744':mat===1?'#654324':'#845623');
  }
 }
 // Recessed panel edging, small fasteners and worn rim in the MI-inspired shell.
 if(mat===10&&w>=6&&h>=6){
  rect(u+2,v+2,w-4,1,'#171a1e');rect(u+2,v+3,1,h-5,'#191c20');
  rect(u+3,v+h-3,w-5,1,'#454b52');
  for(let x of [3,w-5])for(let y of [3,h-5]){rect(u+x,v+y,2,2,'#15171a');pixel(u+x,v+y,'#555c63');}
 }
 if([2,5,9,11].includes(mat)&&w>=8&&h>=8){
  const bolt=mat===5?'#6b4c23':'#3d424b';
  for(let x of [3,w-5])for(let y of [3,h-5]){rect(u+x,v+y,2,2,bolt);pixel(u+x,v+y,mat===5?'#d8ab5a':'#b3b8c0');}
 }
 if([2,5,6,9,11].includes(mat)&&w>12){for(let i=0;i<w/12;i++){let x=3+Math.floor(hash(i,mat,7)*(w-6));pixel(u+x,v+1,mat===5?'#e4b45b':'#c4c8cd');}}
}
function tile(mat,w,h){
 let W=Math.max(1,Math.round(w)),H=Math.max(1,Math.round(h));let key=[mat,W,H].join(':');if(tiles.has(key))return tiles.get(key);
 if(cursorX+W+3>512){cursorX=3;cursorY+=rowHeight+4;rowHeight=0;}
 if(cursorY+H+3>512)throw new Error('Atlas overflow');
 let r=[cursorX,cursorY,cursorX+W,cursorY+H];paint(cursorX,cursorY,W,H,mat);
 // Extend edge pixels into padding to avoid mip bleed.
 ctx.drawImage(canvas,cursorX,cursorY,W,1,cursorX,cursorY-1,W,1);
 ctx.drawImage(canvas,cursorX,cursorY+H-1,W,1,cursorX,cursorY+H,W,1);
 ctx.drawImage(canvas,cursorX,cursorY,1,H,cursorX-1,cursorY,1,H);
 ctx.drawImage(canvas,cursorX+W-1,cursorY,1,H,cursorX+W,cursorY,1,H);
 cursorX+=W+4;rowHeight=Math.max(rowHeight,H);tiles.set(key,r);return r;
}
const prepared={};
for(const file of files){
 const data=JSON.parse(fs.readFileSync(backup+'/'+file,'utf8'));
 const scale=file.includes('_assembled.')||file.includes('_unassembled.')?3:1;
 let hatchUUIDs=new Set();const groupNames=new Map((data.groups||[]).map(g=>[g.uuid,g.name]));
 function walk(nodes,inside=false){for(const n of nodes){if(typeof n==='string'){if(inside)hatchUUIDs.add(n);}else walk(n.children||[],inside||(n.name||groupNames.get(n.uuid)||'').startsWith('output_hatch_'));}}
 walk(data.outliner||[]);
 for(const e of data.elements){
  const d=e.to.map((v,i)=>v-e.from[i]);
  const hatch=file.includes('_output_hatch.')||hatchUUIDs.has(e.uuid)||file.includes('_assembled.')&&(e.name.startsWith('hatch_')||e.name==='output_chute_lip'||/^frame_.*_(56|57|58|59)$/.test(e.name));
  for(const [name,f] of Object.entries(e.faces)){
   if(f.texture===null)continue;
   let mat=Math.floor(f.uv[0]/64)+Math.floor(f.uv[1]/64)*4;
   if(hatch){mat=e.name.includes('interior')||e.name==='hatch_back'?12:e.name.startsWith('frame_')||e.name.includes('lip')?11:10;}
   else if(mat===1&&['up','down'].includes(name))mat=4;
   let wh=['up','down'].includes(name)?[d[0],d[2]]:['east','west'].includes(name)?[d[2],d[1]]:[d[0],d[1]];
   let box=tile(mat,wh[0]*scale*2,wh[1]*scale*2);
   // Preserve exact geometric density for fractional slivers within their padded patch.
   f.uv=[box[0],box[1],box[0]+wh[0]*scale*2,box[1]+wh[1]*scale*2];f.rotation=0;
  }
 }
 prepared[file]=data;
}
const imageData=canvas.toDataURL('image/png');
fs.writeFileSync(assets+'/textures/block/wooden_drill_atlas.png',Buffer.from(imageData.split(',')[1],'base64'));
globalThis.revoTexturePass={root,dir,assets,backup,prepared,imageData,files};
globalThis.revoApplyTexture=(file)=>{
 const p=revoTexturePass;const existing=ModelProject.all.find(x=>x.save_path===p.dir+'/'+file);if(existing)existing.select();else Codecs.project.load(JSON.parse(fs.readFileSync(p.backup+'/'+file,'utf8')),{path:p.dir+'/'+file});
 Undo.initEdit({elements:Cube.all,textures:Texture.all,uv_mode:true});
 const data=p.prepared[file];Project.texture_width=Project.texture_height=512;
 for(const t of Texture.all){t.fromDataURL(p.imageData);t.uv_width=t.uv_height=512;}
 const byId=new Map(data.elements.map(e=>[e.uuid,e]));
 for(const c of Cube.all){const e=byId.get(c.uuid);for(const [name,f] of Object.entries(e.faces)){c.faces[name].uv=f.uv.slice();c.faces[name].rotation=0;}}
 Canvas.updateAll();Undo.finishEdit('Repaint timber, steel and MI-style item hatch');
 return {project:Project.name,cubes:Cube.all.length};
};
globalThis.revoExportTexture=()=>{
 const p=revoTexturePass;Project.java_block_version='1.21.1';const file=Project.save_path;let raw=Codecs.project.compile({raw:true});raw.ai_used=true;raw.ai_agents=['Codex via Blockbench MCP'];fs.writeFileSync(file,JSON.stringify(raw,null,2));
 const assembled=Project.name.endsWith('_assembled'),guide=Project.name.endsWith('_unassembled');
 if(!guide){for(const group of assembled?['body','drill_bit','drive_gear']:['all']){
  for(const c of Cube.all)c.export=group==='all'||c.parent.name===group;
  const model=Codecs.java_block.compile({raw:true});delete model.format_version;model.render_type='minecraft:cutout';model.textures.particle='revoextraction:block/wooden_drill_atlas';
  const name=assembled?'wooden_drill_'+group:Project.name;
  const path=p.assets+'/models/block/'+name+'.json';const previous=JSON.parse(fs.readFileSync(path,'utf8'));model.display=previous.display;
  fs.writeFileSync(path,JSON.stringify(model,null,2));
 }for(const c of Cube.all)c.export=true;}
 Project.saved=true;return {saved:file,atlas:[Texture.all[0].width,Texture.all[0].height]};
};
return {prepared:files.length,uniqueFacePatches:tiles.size,atlas:[512,512],usedHeight:cursorY+rowHeight};
})()

