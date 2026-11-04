(async()=>{
const fs=require('fs'),root="C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition",dir=root+'/art/blockbench/small-cultivation',assets=root+'/revocultivation/src/main/resources/assets/revocultivation';
for(const d of [dir,dir+'/previews',assets+'/models/block',assets+'/textures/block',assets+'/textures/item'])fs.mkdirSync(d,{recursive:true});
const c=document.createElement('canvas');c.width=c.height=128;const ctx=c.getContext('2d');ctx.imageSmoothingEnabled=false;
const old=root+'/art/blockbench/revoextraction/textures',fresh=root+'/art/blockbench/first-tier-machines/textures/vanilla/blocks';
const mats={plank:[2,2],log:[22,2],end:[42,2],cobble:[62,2],stone:[82,2],leaf:[102,2],soil:[2,22],iron:[22,22],dark:[42,22],cream:[62,22],panel:[82,22]};
async function image(name,path){const img=new Image();await new Promise((ok,no)=>{img.onload=ok;img.onerror=no;img.src='data:image/png;base64,'+fs.readFileSync(path).toString('base64')});const[u,v]=mats[name];ctx.drawImage(img,u,v,16,16);}
await image('plank',old+'/vanilla/blocks/planks_oak.png');await image('log',old+'/vanilla/blocks/log_oak.png');await image('end',old+'/vanilla/blocks/log_oak_top.png');await image('cobble',old+'/vanilla/blocks/cobblestone.png');await image('stone',old+'/stone_bricks/blocks/stonebrick.png');await image('leaf',fresh+'/leaves_oak_carried.png');await image('soil',fresh+'/dirt.png');
for(const[name,base]of Object.entries({iron:[91,96,94],dark:[36,39,34],cream:[216,208,169],panel:[127,133,119]})){const[u,v]=mats[name];for(let y=0;y<16;y++)for(let x=0;x<16;x++){const n=(x*11+y*17+x*y)%13-6;ctx.fillStyle='rgb('+base.map(k=>k+n).join(',')+')';ctx.fillRect(u+x,v+y,1,1);}if(name==='iron'||name==='panel'){for(const[x,y]of[[1,1],[13,1],[1,13],[13,13]]){ctx.fillStyle='#303530';ctx.fillRect(u+x,v+y,2,2);ctx.fillStyle='#b9beb0';ctx.fillRect(u+x,v+y,1,1);}}}
const[pu,pv]=mats.panel;ctx.fillStyle='#ded5ac';ctx.fillRect(pu+4,pv+3,8,5);ctx.fillStyle='#333c32';ctx.fillRect(pu+8,pv+4,1,3);ctx.fillRect(pu+4,pv+11,3,2);ctx.fillStyle='#a76a34';ctx.fillRect(pu+10,pv+11,2,2);
// Extrude edge pixels into each tile's 2px gutter.
for(const[u,v]of Object.values(mats)){ctx.drawImage(c,u,v,16,1,u,v-2,16,2);ctx.drawImage(c,u,v+15,16,1,u,v+16,16,2);ctx.drawImage(c,u,v-2,1,20,u-2,v-2,2,20);ctx.drawImage(c,u+15,v-2,1,20,u+16,v-2,2,20);}
const url=c.toDataURL();const png=Buffer.from(url.split(',')[1],'base64');fs.writeFileSync(dir+'/small_cultivation_atlas.png',png);fs.writeFileSync(assets+'/textures/block/small_cultivation_atlas.png',png);
const results=[];
for(const silk of [false,true]){
newProject(Formats.java_block);Project.name=silk?'silkworm_cultivator':'composter';Project.texture_width=Project.texture_height=128;Project.box_uv=false;Project.credit='Revo1999. Native Blockbench MCP; vanilla materials copyright Mojang AB.';
const tex=new Texture({name:'small_cultivation_atlas.png',id:'0',namespace:'revocultivation',folder:'block'}).fromDataURL(url).add(false);tex.uv_width=tex.uv_height=128;
await new Promise(ok=>setTimeout(ok,40));const elements=[];Undo.initEdit({elements,textures:[tex],outliner:true});
const group=new Group({name:'single_block_body'}).init();
function box(name,from,to,material){const b=new Cube({name,from,to,box_uv:false,autouv:0}).addTo(group).init(),d=to.map((v,i)=>v-from[i]);for(const f of ['up','down','north','south','east','west']){const mat=material==='log'&&(f==='up'||f==='down')?'end':material;const[u,v]=mats[mat],wh=f==='up'||f==='down'?[d[0],d[2]]:f==='east'||f==='west'?[d[2],d[1]]:[d[0],d[1]];b.faces[f].texture=tex.uuid;b.faces[f].uv=[u,v,u+wh[0],v+wh[1]];if((f==='down'&&from[1]===0)||(f==='north'&&from[2]===0)||(f==='south'&&to[2]===16)||(f==='west'&&from[0]===0)||(f==='east'&&to[0]===16))b.faces[f].cullface=f;}elements.push(b);return b;}
box('cobble_plinth',[0,0,0],[16,2,16],'cobble');box('tray_floor',[2,2,2],[14,4,14],'plank');
for(const x of [0,13])for(const z of [0,13]){box('oak_post_'+x+'_'+z,[x,2,z],[x+3,14,z+3],'log');box('stone_corner_cap_'+x+'_'+z,[x,14,z],[x+3,16,z+3],'stone');}
for(const z of [0,14]){box('plank_wall_'+z,[3,2,z===0?1:13],[13,12,z===0?3:14],'plank');box('iron_rim_'+z,[3,12,z],[13,14,z+2],'iron');}
for(const x of [0,14]){box('plank_side_'+x,[x,2,3],[x+2,12,13],'plank');box('iron_side_rim_'+x,[x,12,3],[x+2,14,13],'iron');}
box('feed_bed',[2,4,2],[14,silk?9:10,14],silk?'leaf':'soil');
box('front_stone_panel',[5,3,.5],[11,10,1],'stone');box('analogue_controls',[6,6,0],[10,10,.5],'panel');box('output_recess',[6,3,0],[10,5,.5],'dark');
box('rear_drive_mount',[5,5,14],[11,11,15],'iron');box('rear_shaft_at_block_midheight',[6,6,15],[10,10,16],'dark');
if(silk){for(let i=0;i<3;i++){box('silkworm_'+i,[4+i*3,9,5+i%2*3],[6+i*3,10,6+i%2*3],'cream');}box('cocoon_rack_crossbar',[3,14,7],[13,16,9],'plank');box('hanging_cocoon_left',[4,11,7],[6,14,9],'cream');box('hanging_cocoon_right',[10,11,7],[12,14,9],'cream');}
else {box('tamping_bridge',[3,14,7],[13,16,9],'plank');box('tamping_spindle',[7,10,7],[9,14,9],'iron');}
Undo.finishEdit('Build '+Project.name+' balanced single-block model');
const raw=JSON.parse(Codecs.java_block.compile());raw.render_type='minecraft:cutout';raw.textures={'0':'revocultivation:block/small_cultivation_atlas',particle:'minecraft:block/oak_planks'};fs.writeFileSync(assets+'/models/block/'+Project.name+'.json',JSON.stringify(raw,null,2)+'\n');
Project.save_path=dir+'/'+Project.name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;Canvas.updateAll();
const prev=Preview.selected;prev.camera.position.set(30,27,-32);prev.controls.target.set(8,8,8);prev.controls.update();prev.render();const shot=await new Promise(ok=>prev.screenshot({width:900,height:800},ok));fs.writeFileSync(dir+'/previews/'+Project.name+'.png',Buffer.from(shot.split(',')[1],'base64'));
results.push({name:Project.name,cubes:elements.length,bounds:[0,16],density:'1px/model unit'});
}
// Original pixel-art item silhouettes, painted in the native canvas, no external raster processing.
for(const name of ['compost','silkworm','silk_cocoon','silk']){
const ic=document.createElement('canvas');ic.width=ic.height=16;const g=ic.getContext('2d');let seed=19;function pixel(x,y,col){g.fillStyle=col;g.fillRect(x,y,1,1);}
for(let y=1;y<15;y++)for(let x=1;x<15;x++){
let inside=name==='compost'?y>6&&y<14&&Math.abs(x-8)<(y-5):name==='silk_cocoon'?((x-8)**2/18+(y-8)**2/36<1):name==='silkworm'?x>=3&&x<=12&&Math.abs(y-(8+Math.round(Math.sin(x)*1.5)))<2:Math.abs(x-y)<3&&x>2&&x<13;
if(!inside)continue;seed=(seed*1664525+1013904223)>>>0;const palette=name==='compost'?['#4a2e1b','#674526','#80603a','#3d5229']:['#766d50','#b4aa82','#ded4ac','#eee7cd'];pixel(x,y,palette[(seed>>>28)%4]);
}if(name==='silkworm'){pixel(12,7,'#353526');pixel(12,8,'#353526');}
fs.writeFileSync(assets+'/textures/item/'+name+'.png',Buffer.from(ic.toDataURL().split(',')[1],'base64'));
}
return results;
})()
