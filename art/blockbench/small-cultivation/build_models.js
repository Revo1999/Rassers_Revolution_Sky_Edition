(async()=>{
const fs=require('fs'),root="C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition",dir=root+'/art/blockbench/small-cultivation',assets=root+'/revocultivation/src/main/resources/assets/revocultivation',archive=dir+'/archive/detailed-v1';
fs.mkdirSync(archive,{recursive:true});
for(const n of ['composter.bbmodel','silkworm_cultivator.bbmodel','small_cultivation_atlas.png','build_models.js'])if(!fs.existsSync(archive+'/'+n))fs.copyFileSync(dir+'/'+n,archive+'/'+n);
const canvas=document.createElement('canvas');canvas.width=canvas.height=128;const ctx=canvas.getContext('2d');ctx.imageSmoothingEnabled=false;
const img=new Image();await new Promise((ok,no)=>{img.onload=ok;img.onerror=no;img.src='data:image/png;base64,'+fs.readFileSync(dir+'/small_cultivation_atlas.png').toString('base64')});ctx.drawImage(img,0,0);
const tile=[2,42];const paintWood=eval(fs.readFileSync(dir+'/wood_paint.js','utf8'));paintWood(ctx,canvas);
const[u,v]=tile;ctx.drawImage(canvas,u,v,16,1,u,v-2,16,2);ctx.drawImage(canvas,u,v+15,16,1,u,v+16,16,2);ctx.drawImage(canvas,u,v-2,1,20,u-2,v-2,2,20);ctx.drawImage(canvas,u+15,v-2,1,20,u+16,v-2,2,20);
const atlas=canvas.toDataURL(),results=[];
for(const name of ['composter','silkworm_cultivator']){
let p=ModelProject.all.find(p=>p.save_path===dir+'/'+name+'.bbmodel');if(!p){const path=dir+'/'+name+'.bbmodel';Codecs.project.load(JSON.parse(fs.readFileSync(path,'utf8')),{path});p=Project;}p.select();
const tex=Texture.all[0],elements=Cube.all.slice(),silk=name==='silkworm_cultivator',group=Group.all[0];
Undo.initEdit({elements,textures:[tex],bitmap:true,outliner:true});
const keep=silk?['cocoon_rack_crossbar','hanging_cocoon_left','hanging_cocoon_right','feed_bed','silkworm_0']:[];
for(const e of Cube.all.slice())if(!keep.includes(e.name))e.remove();
function map(b,material='wood'){const d=b.to.map((v,i)=>v-b.from[i]),offset=material==='wood'?tile:material==='leaf'?[102,2]:[62,22];for(const f of Object.keys(b.faces)){const wh=f==='up'||f==='down'?[d[0],d[2]]:f==='east'||f==='west'?[d[2],d[1]]:[d[0],d[1]];b.faces[f].texture=tex.uuid;b.faces[f].uv=[offset[0],offset[1],offset[0]+wh[0],offset[1]+wh[1]];b.faces[f].cullface='';}return b;}
function box(name,from,to){const b=new Cube({name,from,to,box_uv:false,autouv:0}).addTo(group).init();elements.push(b);map(b);return b;}
box('wooden_bucket_floor',[1,0,1],[15,2,15]);const height=silk?10:16;
box('wooden_bucket_front',[1,2,1],[15,height,3]);box('wooden_bucket_back',[1,2,13],[15,height,15]);box('wooden_bucket_left',[1,2,3],[3,height,13]);box('wooden_bucket_right',[13,2,3],[15,height,13]);
if(silk){box('silk_rack_left_support',[2,8,7],[4,14,9]);box('silk_rack_right_support',[12,8,7],[14,14,9]);for(const b of Cube.all){if(b.name==='feed_bed'){b.from=[3,2,3];b.to=[13,7,13];map(b,'leaf');}else if(b.name==='silkworm_0'){b.from=[5,7,5];b.to=[7,8,6];map(b,'cream');}else if(b.name.startsWith('hanging_cocoon_'))map(b,'cream');else if(b.name==='cocoon_rack_crossbar')map(b);}}
tex.fromDataURL(atlas);await new Promise(ok=>setTimeout(ok,60));Undo.finishEdit('Simplify '+name+' to dithered wooden bucket and retained silk rack');
Canvas.updateAll();const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.render_type='minecraft:cutout';raw.textures={'0':'revocultivation:block/small_cultivation_atlas',particle:'revocultivation:block/small_cultivation_atlas'};
fs.writeFileSync(assets+'/models/block/'+name+'.json',JSON.stringify(raw,null,2)+'\n');fs.writeFileSync(p.save_path,Codecs.project.compile());p.saved=true;
const preview=Preview.selected;preview.camera.position.set(28,29,-32);preview.controls.target.set(8,8,8);preview.controls.update();preview.render();const shot=await new Promise(ok=>preview.screenshot({width:900,height:800},ok));fs.writeFileSync(dir+'/previews/'+name+'.png',Buffer.from(shot.split(',')[1],'base64'));
results.push({name,cubes:Cube.all.length,rackPreserved:silk?Cube.all.find(e=>e.name==='cocoon_rack_crossbar').uuid:null});}
const png=Buffer.from(atlas.split(',')[1],'base64');fs.writeFileSync(dir+'/small_cultivation_atlas.png',png);fs.writeFileSync(assets+'/textures/block/small_cultivation_atlas.png',png);
await eval(fs.readFileSync(dir+'/build_contents.js','utf8'));
return results;
})()
