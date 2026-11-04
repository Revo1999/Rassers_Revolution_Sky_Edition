(async()=>{
const fs=require('fs');
const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/first-tier-machines',assets=root+'/revocultivation/src/main/resources/assets/revocultivation';
for(const p of [dir,dir+'/previews',assets+'/models/block',assets+'/textures/block'])fs.mkdirSync(p,{recursive:true});
const old=root+'/art/blockbench/revoextraction/textures';const fresh=dir+'/textures/vanilla/blocks';
const swatches={planks:[0,0],log:[64,0],end:[128,0],cobble:[192,0],stone:[256,0],soil:[320,0],glass:[384,0],leaf:[448,0],metal:[0,64],dark:[64,64],amber:[128,64],panel:[192,64],zombie:[256,64],skeleton:[320,64],spider:[384,64],wheat:[448,64],meter:[64,128],dirt:[128,128]};
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const c=canvas.getContext('2d');c.imageSmoothingEnabled=false;
async function tile(mat,path){const img=new Image();await new Promise((ok,no)=>{img.onload=ok;img.onerror=no;img.src='data:image/png;base64,'+fs.readFileSync(path).toString('base64')});const [u,v]=swatches[mat];for(let y=0;y<64;y+=16)for(let x=0;x<64;x+=16)c.drawImage(img,u+x,v+y,16,16);}
await tile('planks',old+'/vanilla/blocks/planks_oak.png');await tile('log',old+'/vanilla/blocks/log_oak.png');await tile('end',old+'/vanilla/blocks/log_oak_top.png');await tile('cobble',old+'/vanilla/blocks/cobblestone.png');await tile('stone',old+'/stone_bricks/blocks/stonebrick.png');await tile('soil',fresh+'/farmland_wet.png');await tile('dirt',fresh+'/dirt.png');await tile('glass',fresh+'/glass.png');await tile('leaf',fresh+'/leaves_oak_carried.png');await tile('wheat',fresh+'/wheat_stage_7.png');
function colorTile(name,base){const [u,v]=swatches[name];for(let y=0;y<64;y++)for(let x=0;x<64;x++){let n=((x*13+y*19+x*y)%11)-5;c.fillStyle='rgb('+base.map(k=>Math.clamp(k+n,0,255)).join(',')+')';c.fillRect(u+x,v+y,1,1);}}
colorTile('metal',[106,111,115]);colorTile('dark',[40,44,44]);colorTile('amber',[232,151,36]);colorTile('panel',[133,132,123]);colorTile('zombie',[69,99,55]);colorTile('skeleton',[173,174,160]);colorTile('spider',[43,37,31]);colorTile('meter',[40,43,40]);
// Small bolts, abrasion and meter details are pixels, never extra geometry.
for(const name of ['metal','panel']){const[u,v]=swatches[name];for(let y=0;y<64;y+=16)for(let x=0;x<64;x+=16){c.fillStyle='#383d3c';for(const [a,b]of[[1,1],[13,1],[1,13],[13,13]])c.fillRect(u+x+a,v+y+b,2,2);c.fillStyle='#c3c5bc';c.fillRect(u+x+1,v+y+1,2,1);c.fillStyle='#797e7c';c.fillRect(u+x+4,v+y+15,8,1);}}
for(let y=0;y<64;y+=16)for(let x=0;x<64;x+=16){
 for(const name of ['zombie','skeleton','spider']){const[u,v]=swatches[name];c.fillStyle=name==='skeleton'?'#2b2924':'#ef512d';c.fillRect(u+x+2,v+y+3,2,1);c.fillRect(u+x+6,v+y+3,2,1);c.fillStyle='#242b23';c.fillRect(u+x+3,v+y+6,4,1);}
 const[u,v]=swatches.meter;c.fillStyle='#d4cfaa';c.fillRect(u+x+1,v+y+1,6,4);c.fillStyle='#4c4b40';c.fillRect(u+x+3,v+y+3,2,1);c.fillRect(u+x+2,v+y+2,1,1);c.fillStyle='#686b57';c.fillRect(u+x+5,v+y+1,1,1);
}
const atlas=canvas.toDataURL('image/png');fs.writeFileSync(assets+'/textures/block/first_tier_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(dir+'/textures/first_tier_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
const results=[];
async function build(name,assembled,kind,species="zombie"){
 if(Project)Project.saved=true;newProject(Formats.java_block);Project.name=name;Project.java_block_version='1.21.1';Project.texture_width=Project.texture_height=512;Project.box_uv=false;Project.credit='Revo1999; authored in Blockbench through MCP. Vanilla textures copyright Mojang AB.';
 const tex=new Texture({name:'first_tier_atlas.png',id:'0',folder:'block',namespace:'revocultivation'}).fromDataURL(atlas).add(false);tex.uv_width=tex.uv_height=512;
 await new Promise(ok=>setTimeout(ok,60));
 const cubes=[],groups={};const scale=assembled?3:1;Undo.initEdit({elements:cubes,textures:[tex],outliner:true});
 function group(name){return groups[name]||(groups[name]=new Group({name,origin:[0,0,0]}).init());}
 function cube(name,from,to,mat='planks',g='body',options={}){
  const dims=to.map((v,i)=>v-from[i]);const obj=new Cube({name,from:from.map(v=>v/scale),to:to.map(v=>v/scale),box_uv:false,autouv:0}).addTo(group(g)).init();
  for(const face of ['north','south','east','west','up','down']){
   let material=mat==='log'&&['up','down'].includes(face)?'end':mat;if(options.top&&face==='up')material=options.top;
   let [u,v]=swatches[material];let wh=['up','down'].includes(face)?[dims[0],dims[2]]:['east','west'].includes(face)?[dims[2],dims[1]]:[dims[0],dims[1]];
   let offsetU=material==='meter'||['zombie','skeleton','spider'].includes(material)?0:Math.min(((from[0]%16)+16)%16,63-wh[0]);let offsetV=material==='meter'||['zombie','skeleton','spider'].includes(material)?0:Math.min(((from[1]%16)+16)%16,63-wh[1]);
   obj.faces[face].texture=tex.uuid;obj.faces[face].uv=[u+offsetU,v+offsetV,u+offsetU+wh[0],v+offsetV+wh[1]];
   if(options.noFaces?.includes(face))obj.faces[face].texture=null;
  }
  cubes.push(obj);return obj;
 }
 function controller(shift=[0,0,0]){const b=(n,f,t,m)=>cube(n,f.map((v,i)=>v+shift[i]),t.map((v,i)=>v+shift[i]),m);
  b('stone_analogue_enclosure',[0,0,0],[16,16,3],'stone');b('weathered_rim',[2,2,-.6],[14,14,0],'metal');b('analogue_meter',[4,7,-.95],[12,13,-.6],'meter');b('brown_switch',[8,3,-1.8],[11,5,-.6],'dark');b('ceramic_fuse',[4,3,-1.2],[6,5,-.6],'amber');}
 function hatch(side=false){const b=(n,f,t,m)=>{if(side){const cv=p=>[48-p[2],p[1],16+p[0]],a=cv(f),z=cv(t);return cube(n,a.map((v,i)=>Math.min(v,z[i])),a.map((v,i)=>Math.max(v,z[i])),m);}return cube(n,f,t,m);};
  b('hatch_stone_enclosure',[0,0,0],[16,16,3],'stone');b('hatch_iron_rim',[2,2,-.6],[14,14,0],'metal');b('hatch_dark_opening',[4,4,-.9],[12,12,-.6],'dark');b('hatch_lower_lip',[3,2,-1.7],[13,4,-.6],'metal');}
 function lantern(x,z){cube('lantern_cap_'+x+'_'+z,[x,34,z],[x+4,35,z+4],'metal');cube('lantern_amber_'+x+'_'+z,[x+.5,28,z+.5],[x+3.5,34,z+3.5],'amber');cube('lantern_foot_'+x+'_'+z,[x,27,z],[x+4,28,z+4],'metal');}
 function frame(height){
  for(const x of [1,37])for(const z of [1,37]){cube('cobblestone_foot_'+x+'_'+z,[x-1,0,z-1],[x+11,4,z+11],'cobble');cube('oak_pillar_'+x+'_'+z,[x+1,4,z+1],[x+9,height-3,z+9],'log');cube('square_stone_cap_'+x+'_'+z,[x-1,height-4,z-1],[x+11,height,z+11],'stone');if(height>16)cube('square_stone_bracing_'+x+'_'+z,[x,14,z],[x+10,18,z+10],'stone');}
  for(const y of height>16?[5,height-4]:[5]){for(const z of [3,41])cube('plank_crossbeam_'+y+'_'+z,[10,y,z],[38,y+5,z+4]);for(const x of [3,41])cube('plank_sidebeam_'+y+'_'+x,[x,y,10],[x+4,y+5,38]);}
 }
 if(!assembled){
  if(name==='timber_support'){cube('oak_support',[0,3,0],[16,14,16],'log');cube('cobble_base',[0,0,0],[16,3,16],'cobble');cube('stone_square_brace',[0,14,0],[16,16,16],'stone');}
  else if(name==='wooden_casing'){cube('plank_casing',[0,0,0],[16,16,16]);cube('iron_front_strap',[0,12,-.06],[16,14,.2],'metal');}
  else if(name==='soil_bed')cube('moist_soil',[0,0,0],[16,16,16],'dirt','body',{top:'soil'});
  else if(name==='collection_grate'){cube('collection_floor',[0,0,0],[16,16,16],'dark');for(let x=1;x<16;x+=4)cube('grate_slat_'+x,[x,15.6,0],[x+1.5,16,16],'metal');}
  else if(name==='item_hatch')hatch();
  else if(name.endsWith('_controller'))controller();
  else {const glass=name==='glass_panel';cube('panel_edge_a',[0,0,7],[2,16,9],'log');cube('panel_edge_b',[14,0,7],[16,16,9],'log');cube('panel_top',[2,14,7],[14,16,9],'metal');cube('panel_bottom',[2,0,7],[14,2,9],'metal');if(glass)cube('clear_glass',[2,2,7.5],[14,14,8.5],'glass');else for(let x=3;x<14;x+=3)cube('iron_bar_'+x,[x,2,7],[x+1,14,9],'metal');}
 }else{
  const tall=kind!=='tree_grower';frame(tall?48:16);cube('bottom_plank_sill',[6,0,6],[42,2,42],'dark');
  if(kind==='mob_harvester'){
   cube('collection_tray',[10,2,10],[38,5,38],'dark');
   for(const z of [5,41])for(let x=16;x<=32;x+=8)cube('cage_front_back_bar_'+x+'_'+z,[x,18,z],[x+1,42,z+1],'metal');
   for(const x of [5,41])for(let z=16;z<=32;z+=8)cube('cage_side_bar_'+x+'_'+z,[x,18,z],[x+1,42,z+1],'metal');
   for(const z of [5,41])cube('cage_horizontal_'+z,[11,28,z],[38,29.5,z+1],'metal');for(const x of [5,41])cube('cage_side_horizontal_'+x,[x,28,11],[x+1,29.5,38],'metal');
   cube('wooden_roof',[10,43,10],[38,46,38]);cube('roof_vent',[19,46,19],[29,48,29],'metal');lantern(9,4);lantern(35,4);
   if(species==='spider'){
    cube('spider_body',[18,17,21],[30,24,31],'spider','actor');cube('spider_head',[19,18,15],[29,25,22],'spider','actor');
    for(const z of [17,21,25,29]){cube('spider_upper_left_'+z,[15,15,z],[20,18,z+1.5],'spider','actor');cube('spider_lower_left_'+z,[13,5,z],[15.5,16,z+1.5],'spider','actor');cube('spider_upper_right_'+z,[28,15,z],[33,18,z+1.5],'spider','actor');cube('spider_lower_right_'+z,[32.5,5,z],[35,16,z+1.5],'spider','actor');}
   }else{const flesh=species==='skeleton'?'skeleton':'zombie',body=species==='skeleton'?'skeleton':'dark';cube('mob_head',[20,28,20],[28,36,28],flesh,'actor');cube('mob_body',[21,18,22],[27,28,26],body,'actor');cube('mob_left_arm',[17,21,21],[20,29,24],flesh,'actor');cube('mob_right_arm',[28,21,21],[31,29,24],flesh,'actor');cube('mob_left_leg',[20,5,21],[23,18,25],body,'actor');cube('mob_right_leg',[25,5,21],[28,18,25],body,'actor');}
   cube('filter_selector_plate',[22,21,3],[26,25,5],'metal');cube('filter_selector_knob',[23,22,2],[25,24,3],'amber');
  }else{
   cube('soil_bed',[10,3,10],[38,14,38],'dirt','body',{top:'soil'});
   // Solid lower planter walls; plants and glass keep the upper silhouette open.
   for(const z of [5,39])cube('lower_planter_front_'+z,[10,6,z],[38,14,z+4]);for(const x of [5,39])cube('lower_planter_side_'+x,[x,6,10],[x+4,14,38]);
   if(kind==='greenhouse'){
    for(const z of [6,41])cube('glass_wall_front_'+z,[11,18,z],[37,43,z+1],'glass');for(const x of [6,41])cube('glass_wall_side_'+x,[x,18,11],[x+1,43,37],'glass');cube('glass_skylight',[11,46,11],[37,47,37],'glass');lantern(9,4);lantern(35,4);
    // Editable crop preview, replaced by the selected vanilla crop at runtime.
    for(let i=0;i<2;i++){let a=cube('wheat_preview_'+i,i===0?[18,14,23.875]:[23.875,14,18],i===0?[30,28,24.125]:[24.125,28,30],'wheat','plant');for(const [face,f]of Object.entries(a.faces)){f.uv=[448,64,464,80];if(['up','down'].includes(face)||(i===0?['east','west']:['north','south']).includes(face))f.texture=null;}}
   }else{
    cube('oak_trunk',[21,14,21],[27,39,27],'log','plant');cube('lower_oak_crown',[7,32,7],[41,48,41],'leaf','plant');cube('middle_oak_crown',[12,45,12],[36,57,36],'leaf','plant');cube('top_oak_crown',[17,55,17],[31,62,31],'leaf','plant');
    cube('amber_status_left',[10,10,4.8],[15,12,5.1],'amber');cube('amber_status_right',[33,10,4.8],[38,12,5.1],'amber');
   }
  }
  controller([16,0,0]);hatch(true);
 }
 Canvas.updateAll();Undo.finishEdit('Build first-tier '+name+' in Blockbench');
 Project.save_path=dir+'/'+name+'.bbmodel';Project.saved=true;
 const raw=Codecs.project.compile({raw:true});raw.ai_used=true;raw.ai_agents=['Codex via Blockbench MCP'];fs.writeFileSync(Project.save_path,JSON.stringify(raw,null,2));
 function runtime(file,predicate){for(const obj of cubes)obj.export=predicate(obj);const json=Codecs.java_block.compile({raw:true});delete json.format_version;delete json.groups;json.parent='minecraft:block/block';json.render_type='minecraft:cutout';json.textures={'0':'revocultivation:block/first_tier_atlas',particle:'revocultivation:block/first_tier_atlas'};fs.writeFileSync(assets+'/models/block/'+file+'.json',JSON.stringify(json,null,2));for(const obj of cubes)obj.export=true;}
 if(assembled){runtime(kind+'_body',obj=>obj.parent.name==='body');if(kind==='tree_grower')runtime('tree_grower_tree',obj=>obj.parent.name==='plant');if(kind==='mob_harvester'){
   runtime('mob_harvester_'+species,obj=>obj.parent.name==='actor');
  }}else runtime(name,()=>true);
 for(const obj of cubes)obj.export=true;Canvas.updateAll();Project.saved=true;
 const preview=Preview.selected;preview.camera.position.set(34,tallFor(name)?26:23,-34);preview.controls.target.set(8,8,8);preview.controls.update();preview.render();
 if(assembled){const image=await new Promise(ok=>preview.screenshot({width:1000,height:800},ok));fs.writeFileSync(dir+'/previews/'+kind+(kind==='mob_harvester'&&species!=='zombie'?'_'+species:'')+'.png',Buffer.from(image.split(',')[1],'base64'));}
 const record={name,cubes:cubes.length,triangles:cubes.length*12,scale,groups:Object.keys(groups)};results.push(record);return record;
}
function tallFor(name){return name.includes('greenhouse')||name.includes('harvester');}
globalThis.buildRevoFarm=build;
for(const name of ['timber_support','wooden_casing','soil_bed','collection_grate','glass_panel','barred_panel','mob_filter','item_hatch','tree_grower_controller','greenhouse_controller','mob_harvester_controller'])await build(name,false);
for(const kind of ['tree_grower','greenhouse','mob_harvester'])await build(kind+'_assembled',true,kind);
for(const species of ['skeleton','spider'])await build('mob_harvester_'+species+'_assembled',true,'mob_harvester',species);
fs.writeFileSync(dir+'/model-checks.json',JSON.stringify({preference:'balance',texture:[512,512],nativeDensity:'16 px/block after runtime x3',models:results},null,2));
return results;
})()
