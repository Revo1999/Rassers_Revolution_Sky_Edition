(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition',dir=root+'/art/blockbench/metal-farms',assets=root+'/revocultivation/src/main/resources/assets/revocultivation';
const D=(name,from,to,style='iron',group='body')=>({name,from,to,style,group});
const snapshot=dir+'/history/before-connected-glass';fs.mkdirSync(snapshot,{recursive:true});
for(const name of ['iron_greenhouse_assembled','steel_tree_grower_assembled','iron_glass_panel']){
 const p=ModelProject.all.filter(p=>p.name===name).at(-1);if(p){p.select();const file=snapshot+'/'+name+'-live-native.bbmodel';if(!fs.existsSync(file))fs.writeFileSync(file,Codecs.project.compile());}
}
const sources={},glass='connected_glass';sources.iron_glass_panel=[D('seamless_glass_block',[0,0,0],[16,16,16],glass)];
function tank(x,z,tier,height,group='body'){
 const a=[],add=(n,f,t,s)=>a.push(D(n,f,t,s,group)),tag='tank_'+tier+'_'+x+'_'+z;
 add(tag+'_floor',[x+1,0,z+1],[x+14,3,z+15],'silver');add(tag+'_lid',[x+1,height-2,z+1],[x+14,height,z+15],tier);
 for(const X of [x+1,x+13])for(const Z of [z+1,z+14])add(tag+'_corner_'+X+'_'+Z,[X,3,Z],[X+1,height-2,Z+1],'silver');
 add(tag+'_front_glass',[x+2,3,z+1.25],[x+13,height-2,z+1.4],glass);add(tag+'_rear_glass',[x+2,3,z+14.6],[x+13,height-2,z+14.75],glass);
 add(tag+'_outer_glass',[x+1.25,3,z+2],[x+1.4,height-2,z+14],glass);add(tag+'_inner_glass',[x+13.6,3,z+2],[x+13.75,height-2,z+14],glass);
 add(tag+'_fill_cap',[x+5,height,z+5],[x+10,height+1,z+11],'cast');add(tag+'_water_port',[x,6,z+6],[x+1.25,10,z+10],'water_port');
 return a;
}
sources.iron_water_tank=tank(0,0,'iron',15);sources.steel_water_tank=tank(0,0,'steel',15);
const greenhouse=[],add=(...args)=>greenhouse.push(D(...args));
for(const x of [1,67])for(const z of [1,67]){
 if(!(x===67&&z===67))add('foundation_'+x+'_'+z,[x,0,z],[x+12,5,z+12],'cast');
 add('glass_support_column_'+x+'_'+z,[x+2,x===67&&z===67?16:5,z+2],[x+10,40,z+10],'iron');
 add('upper_corner_bracket_'+x+'_'+z,[x,36,z],[x+12,40,z+12],'silver');
}
for(const z of [3,71])add('bed_crossmember_'+z,[13,2,z],[67,10,z+6],'iron');
for(const x of [3,71])for(const [a,b] of [[13,32],[48,67]])add('bed_side_sill_'+x+'_'+a,[x,2,a],[x+6,10,b],'iron');
for(const x of [16,32,48])for(const z of [16,32,48]){
 add('crop_tray_'+x+'_'+z,[x,1,z],[x+16,4,z+16],'cast');
 add('crop_soil_'+x+'_'+z,[x,4,z],[x+16,15,z+16],'dirt');
 add('moist_crop_surface_'+x+'_'+z,[x,15,z],[x+16,16,z+16],'soil');
}
for(const z of [14,64])add('continuous_bed_ribbon_'+z,[14,11,z],[66,18,z+2],'iron');
for(const x of [14,64])add('continuous_bed_side_ribbon_'+x,[x,11,16],[x+2,18,64],'iron');
add('controller_enclosure',[32,0,0],[48,16,8],'iron');add('controller_screen',[34,6,-.2],[46,14,0],'screen');add('controller_switches',[35,2,-.2],[45,5,0],'switch');
add('output_cabinet',[72,0,48],[80,16,64],'silver');add('output_socket',[80,3,52],[80.2,13,60],'output');
add('collection_duct',[64,5,52],[72,11,60],'cast');add('collection_duct_cover',[64,11,52],[72,12,60],'silver');
add('continuous_front_glazing',[3.5,16,3.5],[76.5,43,3.65],glass);add('continuous_rear_glazing',[3.5,16,76.35],[76.5,43,76.5],glass);
add('continuous_right_glazing',[76.35,16,3.5],[76.5,43,76.5],glass);
add('left_glazing_front_of_tank',[3.5,16,3.5],[3.65,43,33],glass);add('left_glazing_rear_of_tank',[3.5,16,47],[3.65,43,76.5],glass);add('left_glazing_above_tank',[3.5,25,33],[3.65,43,47],glass);
add('continuous_roof_glazing',[3.5,44,3.5],[76.5,44.15,76.5],glass);
for(const z of [3,69])add('glass_top_border_'+z,[11,40,z],[69,44,z+8],'iron');
for(const x of [3,69])add('glass_side_top_border_'+x,[x,40,11],[x+8,44,69],'iron');
for(const z of [3,74])add('glass_lower_border_'+z,[11,14,z],[69,17,z+3],'silver');
add('glass_right_lower_border',[74,14,11],[77,17,69],'silver');
for(const [a,b] of [[11,33],[47,69]])add('glass_left_lower_border_'+a,[3,14,a],[6,17,b],'silver');
add('tank_glass_header',[3,25,33],[6,27,47],'silver');
for(const x of [11,67])for(const z of [3,74])add('glass_edge_trim_'+x+'_'+z,[x,17,z],[x+2,40,z+3],'silver');
for(const z of [19,39,59]){
 add('ceiling_rail_'+z,[12,44.5,z],[68,46.5,z+2],'silver');add('ceiling_diffuser_off_'+z,[14,44.3,z+.25],[66,44.5,z+1.75],'light_off');
 add('ceiling_diffuser_on_'+z,[14,44.2,z+.25],[66,44.3,z+1.75],'light_on','lights');
}
add('ceiling_left_bus_rail',[11,43.5,13],[14,45.5,67],'iron');add('ceiling_right_bus_rail',[66,43.5,13],[69,45.5,67],'iron');
add('integrated_converter_plinth',[64,0,64],[80,2,80],'cast');add('integrated_converter_shell',[65,2,65],[79,14,79],'iron');add('integrated_converter_top_mount',[64,14,64],[80,16,80],'silver');
add('rear_fe_terminal',[67,3.5,79],[77,12.5,80],'fe_terminal');add('converter_side_louvres',[79,4,67],[79.25,12,77],'vent');
add('converter_hatch_bus',[68,4,63.8],[76,12,65],'copper');add('converter_roof_conduit',[66,16,65],[68,44,67],'copper');
greenhouse.push(...tank(0,32,'iron',25));add('greenhouse_irrigation_pipe',[11,23,38],[17,25,42],'copper');
add('preview_water',[2.2,3,35.2],[10.8,22,44.8],'water','water_preview');
for(const type of ['wheat','carrot','potato','beetroot'])for(const x of [16,32,48])for(const z of [16,32,48]){
 add('preview_'+type+'_x_'+x+'_'+z,[x,16,z+7.95],[x+16,32,z+8.05],type,'crop_'+type);
 add('preview_'+type+'_z_'+x+'_'+z,[x+7.95,16,z],[x+8.05,32,z+16],type,'crop_'+type);
}
sources.iron_greenhouse_assembled=greenhouse;
const tree=[],T=(...args)=>tree.push(D(...args));
for(const x of [1,67])for(const z of [1,67]){
 T('steel_planter_foot_'+x+'_'+z,[x,0,z],[x+12,4,z+12],'cast');T('steel_planter_post_'+x+'_'+z,[x+2,4,z+2],[x+10,14,z+10],'steel');T('corner_cap_'+x+'_'+z,[x,14,z],[x+12,16,z+12],'silver');
}
for(const z of [3,71])T('five_block_crossmember_'+z,[13,2,z],[67,9,z+6],'steel');
for(const x of [3,71])for(const [a,b] of [[13,32],[48,67]])T('split_side_sill_'+x+'_'+a,[x,2,a],[x+6,9,b],'steel');
for(const x of [16,32,48])for(const z of [16,32,48]){
 if(x===32&&z===32){T('preview_actual_dirt_block',[32,0,32],[48,16,48],'dirt','soil_preview');continue;}
 T('soil_tray_'+x+'_'+z,[x,1,z],[x+16,4,z+16],'cast');T('soil_bed_'+x+'_'+z,[x,4,z],[x+16,16,z+16],'dirt');
}
for(const z of [14,64])T('continuous_steel_soil_ribbon_'+z,[14,11,z],[66,18,z+2],'steel');
for(const x of [14,64])T('continuous_steel_soil_side_ribbon_'+x,[x,11,16],[x+2,18,64],'steel');
T('collection_intake_collar',[62,5,35],[66,12,45],'silver');
T('collection_duct_trough',[64,5,36],[73,10,44],'cast');
for(const z of [35,44])T('collection_duct_wall_'+z,[64,5,z],[73,11,z+1],'steel');
T('collection_duct_cover',[64,11,36],[73,12,44],'silver');
T('controller_enclosure',[32,0,0],[48,16,8],'steel');T('controller_screen',[34,6,-.2],[46,14,0],'screen');T('controller_switches',[35,2,-.2],[45,5,0],'switch');
T('output_cabinet',[72,0,32],[80,16,48],'silver');T('output_socket',[80,3,36],[80.2,13,44],'output');
tree.push(...tank(0,32,'steel',26));T('irrigation_main',[13,13,37],[32,15,39],'copper');T('irrigation_rear_branch',[31,13,39],[33,15,64],'copper');
T('preview_water',[2.2,3,35.2],[12.8,23,44.8],'water','water_preview');
const blocks=[];for(let y=1;y<=6;y++)blocks.push({x:0,y,z:0,log:true});
for(let y=4;y<=7;y++){const r=y<=5?2:1;for(let x=-r;x<=r;x++)for(let z=-r;z<=r;z++){
 if(Math.abs(x)===r&&Math.abs(z)===r)continue;if(x===0&&z===0&&y<=6)continue;blocks.push({x,y,z,log:false});
}}
for(const b of blocks)T('preview_vanilla_'+(b.log?'log':'leaves')+'_'+b.x+'_'+b.y+'_'+b.z,[32+b.x*16,b.y*16,32+b.z*16],[48+b.x*16,(b.y+1)*16,48+b.z*16],b.log?'oak_log':'oak_leaves','real_tree_preview');
sources.steel_tree_grower_assembled=tree;
const cv=document.createElement('canvas');cv.width=cv.height=512;const ctx=cv.getContext('2d');ctx.imageSmoothingEnabled=false;ctx.fillStyle='#263134';ctx.fillRect(0,0,512,512);
const samples={};const aliases={connected_glass:'glass',soil:'farmland_moist',dirt:'dirt',oak_log:'oak_log',oak_log_top:'oak_log_top',oak_leaves:'oak_leaves',water:'water_still',wheat:'wheat_stage7',carrot:'carrots_stage3',potato:'potatoes_stage3',beetroot:'beetroots_stage3'};
for(const [style,file] of Object.entries(aliases)){
 const img=new Image();img.src='data:image/png;base64,'+fs.readFileSync(dir+'/vanilla/'+file+'.png').toString('base64');await img.decode();const c=document.createElement('canvas');c.width=c.height=16;const q=c.getContext('2d');q.drawImage(img,0,0,16,16,0,0,16,16);samples[style]=q.getImageData(0,0,16,16).data;
}
const palette={steel:[65,91,86],iron:[97,117,131],silver:[156,168,170],cast:[66,76,80],dark:[25,35,39],copper:[149,111,76],screen:[20,43,45],switch:[105,125,118],vent:[61,77,83],output:[27,37,41],light_off:[82,98,102],light_on:[199,230,215],water_port:[35,104,123],fe_terminal:[30,62,72]};
const tiles=new Map();let px=2,py=2,row=0;
function tile(style,w,h){
 const W=Math.max(1,Math.ceil(w)),H=Math.max(1,Math.ceil(h)),key=[style,W,H].join(':');if(tiles.has(key))return tiles.get(key);
 if(px+W+2>512){px=2;py+=row+3;row=0;}if(py+H+2>512)throw Error('Refinement atlas overflow');const ox=px,oy=py;px+=W+3;row=Math.max(row,H);
 const fill=(x,y,w,h,c)=>{ctx.fillStyle=c;ctx.fillRect(ox+x,oy+y,w,h);};
 if(samples[style]){
  ctx.clearRect(ox-1,oy-1,W+2,H+2);const data=samples[style];
  for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){
   const X=Math.clamp(x,0,W-1)%16,Y=Math.clamp(y,0,H-1)%16,i=(Y*16+X)*4;
   if(style==='connected_glass'&&(X===0||X===15||Y===0||Y===15))continue;
   const tint=style==='water'?[63,118,228]:style==='oak_leaves'?[103,151,69]:[255,255,255];
   fill(x,y,1,1,'rgba('+[0,1,2].map(k=>Math.round(data[i+k]*tint[k]/255)).join(',')+','+(style==='water'?.78:data[i+3]/255)+')');
  }
 }else{
  const base=palette[style],rgb=d=>'rgb('+base.map(v=>Math.clamp(v+d,0,255)).join(',')+')';
  for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){const X=Math.clamp(x,0,W-1),Y=Math.clamp(y,0,H-1),patch=((X>>2)*13+(Y>>2)*7)%5-2,hash=(X*29+Y*19+X*Y*11)%67;fill(x,y,1,1,rgb(patch*3+((X+Y)%2?4:-4)+(hash<5?-11:hash>62?8:0)));}
  if(W>=4&&H>=4){fill(0,0,W,1,rgb(24));fill(0,0,1,H,rgb(16));fill(0,H-1,W,1,rgb(-26));fill(W-1,0,1,H,rgb(-24));}
  const rivet=(x,y)=>{fill(x,y,2,2,'#35484c');fill(x,y,1,1,'#ccd7d0');};
  if(['steel','iron','silver','cast'].includes(style)&&W>=9&&H>=8){rivet(2,2);rivet(W-4,2);rivet(2,H-4);rivet(W-4,H-4);}
  if(style==='screen'&&W>=8&&H>=7){fill(1,1,W-2,H-2,'#142f34');fill(2,2,W-4,1,'#99c8b7');fill(2,3,Math.max(2,W-7),1,'#5d9f9a');fill(2,H-3,2,1,'#ce9c66');fill(W-4,H-3,2,1,'#afc7b5');}
  if(style==='switch'&&W>=7)for(let x=1;x<W-1;x+=3)fill(x,1,2,Math.min(2,H-1),'#d3d8c9');
  if(style==='vent'&&W>=6&&H>=5)for(let y=2;y<H-1;y+=3){fill(1,y,W-2,1,'#17272d');fill(1,y+1,W-2,1,'#84979d');}
  if(style==='light_on')for(let x=1;x<W-1;x+=3)fill(x,0,1,H,'#edf9df');
  if(style==='output'&&W>=7&&H>=7){const x=Math.floor(W/2);fill(x-1,2,2,H-4,'#b2c0b5');fill(x-2,H-4,4,1,'#b2c0b5');}
  if((style==='fe_terminal'||style==='water_port')&&W>=4&&H>=4){fill(1,1,W-2,H-2,'#14292f');fill(1,1,W-2,1,'#85bbbf');fill(1,1,1,H-2,'#72a8ae');if(W>=8&&H>=8){fill(3,3,W-6,H-6,'#6da6b1');fill(4,4,W-8,H-8,'#1d3d48');}}
 }
 tiles.set(key,[ox,oy]);return [ox,oy];
}
const projects=[];
for(const [name,defs] of Object.entries(sources)){
 const scale=name.endsWith('_assembled')?5:1;
 newProject(Formats.free);Project.name=name;Project.texture_width=Project.texture_height=512;
 const texture=new Texture({name:'greenhouse_tree_atlas.png',namespace:'revocultivation',folder:'block',uv_width:512,uv_height:512}).fromDataURL(cv.toDataURL()).add(false);
 Undo.initEdit({elements:[],textures:[texture],outliner:true});const groups={};
 for(const d of defs){
  if(!groups[d.group])groups[d.group]=new Group({name:d.group,visibility:!/^crop_/.test(d.group)||d.group==='crop_wheat'}).init();
  const c=new Cube({name:d.name,from:d.from.map(v=>v/scale),to:d.to.map(v=>v/scale),box_uv:false}).addTo(groups[d.group]).init();const dims=d.to.map((v,i)=>v-d.from[i]);
  c.visibility=!/^crop_/.test(d.group)||d.group==='crop_wheat';
  for(const [face,f] of Object.entries(c.faces)){
   const wh=['up','down'].includes(face)?[dims[0],dims[2]]:['east','west'].includes(face)?[dims[2],dims[1]]:[dims[0],dims[1]];
   const crop=['wheat','carrot','potato','beetroot'].includes(d.style),mat=d.style==='oak_log'&&['up','down'].includes(face)?'oak_log_top':d.style;
   const [u,v]=tile(mat,crop?16:wh[0],crop?16:wh[1]);f.uv=[u,v,u+(crop?16:wh[0]),v+(crop?16:wh[1])];f.texture=texture.uuid;f.rotation=0;f.cullface=name==='iron_glass_panel'?face:'';
   if(crop){const alongX=d.name.includes('_x_');if(!((alongX?['north','south']:['east','west']).includes(face)))f.texture=null;}
  }
 }
 Undo.finishEdit('Connect greenhouse glazing, fit the converter, add ceiling light rails and build a 5x5 real-tree base');projects.push({project:Project,texture,name,scale});
}
const atlas=cv.toDataURL();fs.writeFileSync(dir+'/greenhouse_tree_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(assets+'/textures/block/greenhouse_tree_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
const report=[];
for(const {project,texture,name,scale} of projects){
 project.select();Undo.initEdit({textures:[texture],elements:Cube.all.slice()});texture.fromDataURL(atlas);texture.uv_width=texture.uv_height=512;await new Promise(ok=>setTimeout(ok,35));Undo.finishEdit('Apply seamless glass and vanilla plant/tree textures with dithered metal');Canvas.updateAll();
 const compile=output=>{const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.render_type='minecraft:cutout';raw.textures.particle='revocultivation:block/greenhouse_tree_atlas';fs.writeFileSync(assets+'/models/block/'+output+'.json',JSON.stringify(raw,null,2)+'\n');};
 if(name.endsWith('_assembled')){for(const c of Cube.all)c.export=c.parent.name==='body';compile(name.replace('_assembled','_body'));if(name.startsWith('iron_greenhouse')){for(const c of Cube.all)c.export=c.parent.name==='lights';compile('iron_greenhouse_lights');}for(const c of Cube.all)c.export=['body','lights'].includes(c.parent.name);}else compile(name);
 Project.save_path=dir+'/'+name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
 report.push({name,scale,cubes:Cube.all.length,runtimeCubes:Cube.all.filter(c=>c.export).length});
}
fs.writeFileSync(dir+'/greenhouse-tree-manifest.json',JSON.stringify({nativeBlockbench:true,models:report,atlas:[512,512],pixelsPerPhysicalUnit:1,realTreeBlocks:blocks,glassBorderPixelsRemoved:true,cropPreviewUsesNativeModelScale:1,vanillaAttribution:'vanilla/manifest.json'},null,2));
return {models:report,tiles:tiles.size,atlasHeight:py+row};
})()
