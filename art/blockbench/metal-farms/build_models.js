(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/metal-farms',assets=root+'/revocultivation/src/main/resources/assets/revocultivation';fs.mkdirSync(dir+'/previews',{recursive:true});
const D=(name,from,to,style='steel',group='body')=>({name,from,to,style,group});
const components={},assemblies={};
for(const tier of ['steel','iron']){
 const paint=tier==='steel'?'steel':'iron';
 components[tier+'_support']=[D('closed_box_column',[3,2,3],[13,14,13],paint),D('column_base_flange',[1,0,1],[15,2,15],'cast'),D('column_top_flange',[1,14,1],[15,16,15],'silver'),D('recessed_web',[6,3,2.75],[10,13,3],'dark')];
 components[tier+'_casing']=[D('service_casing',[0,0,1],[16,16,16],paint),D('raised_service_lid',[1,1,0],[15,15,1],'silver'),D('recessed_louvres',[4,5,-.1],[12,11,0],'vent')];
 components[tier+'_soil_bed']=[D('planting_tray',[0,0,0],[16,3,16],'cast'),D('cultivation_soil',[2,3,2],[14,13,14],'dirt'),D('cultivation_surface',[2,13,2],[14,14,14],'soil')];
 components[tier+'_item_hatch']=[D('hatch_cabinet',[0,0,1],[16,16,16],'silver'),D('hatch_aperture',[3,3,0],[13,13,1],'output')];
}
components.steel_collection_grate=[D('collection_tray',[0,0,0],[16,3,16],'cast')];for(const x of [2,5,8,11,14])components.steel_collection_grate.push(D('grate_bar_'+x,[x,3,1],[x+1,4,15],'silver'));
components.steel_barred_panel=[D('bottom_frame',[0,0,6],[16,2,10],'steel'),D('top_frame',[0,14,6],[16,16,10],'steel'),D('left_frame',[0,2,6],[2,14,10],'silver'),D('right_frame',[14,2,6],[16,14,10],'silver')];
for(const x of [4,8,12])components.steel_barred_panel.push(D('cage_bar_'+x,[x,2,7],[x+1.25,14,9],'silver'));
components.steel_mob_filter=components.steel_barred_panel.map(d=>({...d,name:'filter_'+d.name}));components.steel_mob_filter.push(D('selector_plate',[5,6,4],[11,11,7],'silver'),D('selector_knob',[7,7,3],[9,9,4],'amber'));
components.iron_glass_panel=[D('glass_window',[1,1,7.5],[15,15,8.5],'glass'),D('lower_glazing_rail',[0,0,6.5],[16,1,9.5],'iron'),D('upper_glazing_rail',[0,15,6.5],[16,16,9.5],'iron'),D('left_glazing_rail',[0,1,6.5],[1,15,9.5],'silver'),D('right_glazing_rail',[15,1,6.5],[16,15,9.5],'silver')];
components.iron_grow_light=[D('lamp_housing',[0,10,0],[16,16,16],'iron'),D('recessed_diffuser',[2,9,2],[14,10,14],'light_off'),D('lamp_conduit',[7,0,7],[9,10,9],'copper')];
function controller(paint){return [D('controller_cabinet',[0,0,1],[16,16,16],paint),D('display_panel',[2,5,0],[14,14,1],'screen'),D('switch_bank',[3,1,0],[13,4,1],'switch')];}
components.steel_tree_grower_controller=controller('steel');components.steel_mob_harvester_controller=controller('steel');components.iron_greenhouse_controller=controller('iron');
function machine(kind){
 const tree=kind==='steel_tree_grower',green=kind==='iron_greenhouse',paint=green?'iron':'steel',a=[],add=(...args)=>a.push(D(...args));
 for(const x of [1,35])for(const z of [1,35]){
  const converter=green&&x===35&&z===35;
  if(!converter){add('cast_foot_'+x+'_'+z,[x,0,z],[x+12,4,z+12],'cast');add('foot_cap_'+x+'_'+z,[x+1,4,z+1],[x+11,5,z+11],'silver');}
  if(tree){add('planter_corner_'+x+'_'+z,[x+2,5,z+2],[x+10,14,z+10],paint);add('corner_top_clamp_'+x+'_'+z,[x,14,z],[x+12,16,z+12],'silver');}
  else{
   add('structural_column_'+x+'_'+z,[x+2,converter?16:5,z+2],[x+10,42,z+10],paint);
   add('column_inner_web_'+x+'_'+z,[x+5,converter?17:7,z+1.75],[x+7,34,z+2],'dark');
   add('upper_column_clamp_'+x+'_'+z,[x,36,z],[x+12,40,z+12],'silver');
  }
 }
 for(const z of [3,40])add('lower_front_sill_'+z,[13,3,z],[35,8,z+5],paint);
 add('lower_left_sill',[3,3,13],[8,8,35],paint);
 if(!green)add('rear_right_base_link',[40,3,32],[45,8,35],paint);
 add('controller_enclosure',[16,0,0],[32,16,6],paint);add('controller_screen',[18,6,-.25],[30,14,0],'screen');add('controller_switches',[19,2,-.25],[29,5,0],'switch');
 add('output_cabinet',[40,0,16],[48,16,32],'silver');add('output_socket',[48,3,20],[48.2,13,28],'output');
 if(tree||green){
  add('raised_soil_tray',[10,2,10],[38,5,38],'cast');add('cultivation_bed',[11,5,11],[37,13.5,37],'dirt');add('cultivation_surface',[11,13.5,11],[37,14,37],'soil');
  for(const z of [8,37])add('planter_front_wall_'+z,[10,7,z],[38,14,z+3],paint);
  for(const x of [8,37])add('planter_side_wall_'+x,[x,7,10],[x+3,14,38],paint);
  if(tree){
   for(const z of [8,39])add('stainless_tray_lip_'+z,[12,14,z],[36,15,z+1],'silver');
   add('root_service_drawer',[8,5,17],[10,12,31],'vent');add('rear_service_drawer',[17,5,38],[31,12,40],'vent');
   for(const x of [12,33])add('planter_status_light_'+x,[x,9,7.75],[x+3,11,8],'amber');
  }else{
   for(const z of [5,39]){add('upper_glazing_beam_'+z,[11,39,z],[37,44,z+4],paint);add('window_front_'+z,[11,16,z+1.5],[37,39,z+2.5],'glass');add('window_bottom_front_'+z,[11,15,z+1],[37,17,z+3],'silver');}
   for(const x of [5,39]){add('upper_side_beam_'+x,[x,39,11],[x+4,44,37],paint);add('window_side_'+x,[x+1.5,16,11],[x+2.5,39,37],'glass');add('window_bottom_side_'+x,[x+1,15,11],[x+3,17,37],'silver');}
   add('glass_roof',[11,44,11],[37,44.5,37],'glass');
   for(const z of [16,29]){add('grow_light_fixture_'+z,[14,38,z],[34,40,z+3],'silver');add('unlit_grow_diffuser_'+z,[15,37.9,z+.5],[33,38,z+2.5],'light_off');add('active_grow_diffuser_'+z,[15,37.8,z+.5],[33,37.9,z+2.5],'light_on','lights');}
   add('ceiling_power_conduit',[34,38,17],[36,41,34],'copper');add('rear_power_downpipe',[36,17,34],[38,39,36],'copper');
   add('rear_converter_link',[37,16,35],[41,18,39],'cast');
  }
 }else{
  add('collection_floor',[10,1,10],[38,4,38],'cast');for(const x of [12,18,24,30,36])add('floor_grate_'+x,[x,4,11],[x+1.5,5,37],'silver');
  for(const z of [4,40]){
   add('top_cage_rail_'+z,[11,36,z],[37,41,z+4],paint);add('lower_cage_rail_'+z,[11,12,z],[37,15,z+4],'silver');
   for(const x of [14,21,28,35])add('front_cage_bar_'+x+'_'+z,[x,15,z+1],[x+1.5,36,z+2.5],'silver');
  }
  for(const x of [4,40]){
   add('top_side_cage_rail_'+x,[x,36,11],[x+4,41,37],paint);add('lower_side_cage_rail_'+x,[x,12,11],[x+4,15,37],'silver');
   for(const z of [14,21,28,35])add('side_cage_bar_'+x+'_'+z,[x+1,15,z],[x+2.5,36,z+1.5],'silver');
  }
  add('armoured_processing_roof',[9,41,9],[39,45,39],paint);add('roof_filter_unit',[17,45,17],[31,48,31],'vent');
  add('species_filter_plate',[18,21,2.5],[30,30,5],'silver');add('species_filter_knob',[22,24,1.5],[26,27,2.5],'amber');
  for(const x of [9,36]){add('status_lamp_housing_'+x,[x,24,2],[x+3,32,4],'cast');add('status_lamp_'+x,[x+.5,25,1.8],[x+2.5,31,2],'amber');}
 }
 if(green){
  const clipped=[];
  for(const d of a){
   if(d.from[0]>=48||d.from[1]>=16||d.from[2]>=48||d.to[0]<=32||d.to[2]<=32){clipped.push(d);continue;}
   if(d.from[0]<32)clipped.push({...d,name:d.name+'_left_of_converter',to:[32,d.to[1],d.to[2]]});
   if(d.from[2]<32)clipped.push({...d,name:d.name+'_front_of_converter',from:[Math.max(32,d.from[0]),d.from[1],d.from[2]],to:[d.to[0],d.to[1],32]});
  }
  return clipped;
 }
 return a;
}
for(const kind of ['steel_tree_grower','steel_mob_harvester','iron_greenhouse'])assemblies[kind]=machine(kind);
const cv=document.createElement('canvas');cv.width=cv.height=512;const ctx=cv.getContext('2d');ctx.fillStyle='#242b30';ctx.fillRect(0,0,512,512);
const pixels={};for(const name of ['glass','farmland_wet','dirt']){
 const image=new Image();image.src='data:image/png;base64,'+fs.readFileSync(root+'/art/blockbench/first-tier-machines/textures/vanilla/blocks/'+name+'.png').toString('base64');await image.decode();
 const c=document.createElement('canvas');c.width=c.height=16;c.getContext('2d').drawImage(image,0,0);pixels[name]=c.getContext('2d').getImageData(0,0,16,16).data;
}
const palette={steel:[65,91,86],iron:[97,117,131],silver:[156,168,170],cast:[66,76,80],dark:[25,35,39],copper:[149,111,76],amber:[217,153,57],screen:[20,43,45],switch:[105,125,118],vent:[61,77,83],output:[27,37,41],light_off:[82,98,102],light_on:[194,226,215]};
const tiles=new Map();let px=2,py=2,row=0;
function tile(style,w,h){
 const W=Math.max(1,Math.ceil(w)),H=Math.max(1,Math.ceil(h)),key=[style,W,H].join(':');if(tiles.has(key))return tiles.get(key);
 if(px+W+2>512){px=2;py+=row+3;row=0;}if(py+H+2>512)throw Error('Atlas overflow');const ox=px,oy=py;px+=W+3;row=Math.max(row,H);
 const fill=(x,y,w,h,color)=>{ctx.fillStyle=color;ctx.fillRect(ox+x,oy+y,w,h);};
 if(['glass','soil','dirt'].includes(style)){
  ctx.clearRect(ox-1,oy-1,W+2,H+2);const data=pixels[style==='soil'?'farmland_wet':style];
  for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){const X=Math.clamp(x,0,W-1)%16,Y=Math.clamp(y,0,H-1)%16,i=(Y*16+X)*4;fill(x,y,1,1,'rgba('+[data[i],data[i+1],data[i+2]].join(',')+','+data[i+3]/255+')');}
 }else{
  const base=palette[style],rgb=n=>'rgb('+base.map(v=>Math.clamp(v+n,0,255)).join(',')+')';
  for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){const X=Math.clamp(x,0,W-1),Y=Math.clamp(y,0,H-1),patch=((X>>2)*13+(Y>>2)*7)%5-2,hash=(X*29+Y*19+X*Y*11)%67;fill(x,y,1,1,rgb(patch*3+((X+Y)%2?4:-4)+(hash<5?-11:hash>62?8:0)));}
  if(W>=4&&H>=4){fill(0,0,W,1,rgb(24));fill(0,0,1,H,rgb(16));fill(0,H-1,W,1,rgb(-26));fill(W-1,0,1,H,rgb(-24));}
  const rivet=(x,y)=>{fill(x,y,2,2,'#34494b');fill(x,y,1,1,'#cbd4cb');};
  if(['steel','iron','silver','cast'].includes(style)&&W>=9&&H>=8){rivet(2,2);rivet(W-4,2);rivet(2,H-4);rivet(W-4,H-4);}
  if(style==='screen'&&W>=8&&H>=7){fill(1,1,W-2,H-2,'#142f34');fill(2,2,W-4,1,'#99c8b7');fill(2,3,Math.max(2,W-7),1,'#5d9f9a');fill(2,H-3,2,1,'#ce9c66');fill(W-4,H-3,2,1,'#afc7b5');}
  if(style==='switch'&&W>=7)for(let x=1;x<W-1;x+=3)fill(x,1,2,Math.min(2,H-1),'#d3d8c9');
  if(style==='vent'&&W>=6&&H>=5)for(let y=2;y<H-1;y+=3){fill(1,y,W-2,1,'#17272d');fill(1,y+1,W-2,1,'#84979d');}
  if(style==='light_on'){for(let x=1;x<W-1;x+=3)fill(x,0,1,H,'#edf9df');}
  if(style==='output'&&W>=7&&H>=7){const x=Math.floor(W/2);fill(x-1,2,2,H-4,'#b2c0b5');fill(x-2,H-4,4,1,'#b2c0b5');}
 }
 tiles.set(key,[ox,oy]);return [ox,oy];
}
const projects=[];
async function build(name,defs,scale){
 newProject(Formats.free);Project.name=name;Project.texture_width=Project.texture_height=512;
 const texture=new Texture({name:'metal_farms_atlas.png',namespace:'revocultivation',folder:'block',uv_width:512,uv_height:512}).fromDataURL(cv.toDataURL()).add(false);
 Undo.initEdit({elements:[],textures:[texture],outliner:true});const groups={};
 for(const d of defs){if(!groups[d.group])groups[d.group]=new Group({name:d.group}).init();const c=new Cube({name:d.name,from:d.from.map(v=>v/scale),to:d.to.map(v=>v/scale),box_uv:false}).addTo(groups[d.group]).init();
  const dims=d.to.map((v,i)=>v-d.from[i]);for(const [side,f] of Object.entries(c.faces)){const wh=['up','down'].includes(side)?[dims[0],dims[2]]:['east','west'].includes(side)?[dims[2],dims[1]]:[dims[0],dims[1]],[u,v]=tile(d.style,...wh);f.uv=[u,v,u+wh[0],v+wh[1]];f.texture=texture.uuid;f.rotation=0;f.cullface='';}
 }
 Undo.finishEdit('Build a reinforced metal farm with native-density dithered materials');
 if(name==='steel_tree_grower_assembled'||name==='iron_greenhouse_assembled'){
  const src=JSON.parse(fs.readFileSync(root+'/art/blockbench/first-tier-machines/'+(name.startsWith('steel_tree')?'tree_grower':'greenhouse')+'_assembled.bbmodel','utf8'));
  const plant=src.groups.find(g=>g.name==='plant'),ids=src.outliner.find(g=>g.uuid===plant.uuid).children;
  const t=new Texture({...src.textures[0],uuid:undefined}).fromDataURL(src.textures[0].source).add(false);const g=new Group({name:'plant_preview'}).init();Undo.initEdit({elements:[],textures:[t],outliner:true});
  for(const d of src.elements.filter(c=>ids.includes(c.uuid))){const c=new Cube({...d,uuid:undefined,name:'preview_'+d.name}).addTo(g).init();for(const f of Object.values(c.faces))if(f.texture!==null)f.texture=t.uuid;c.export=false;}
  Undo.finishEdit('Retain an editable crop/tree preview; runtime renders live growth');
 }
 if(name==='iron_greenhouse_assembled'){
  const src=JSON.parse(fs.readFileSync(root+'/art/blockbench/power-converter/power_converter.bbmodel','utf8'));
  const ts=src.textures.map(d=>new Texture({...d,uuid:undefined}).fromDataURL(d.source).add(false)),g=new Group({name:'converter_preview'}).init();Undo.initEdit({elements:[],textures:ts,outliner:true});
  for(const d of src.elements){const rotate=p=>[(48-p[0])/3,p[1]/3,(48-p[2])/3];const a=rotate(d.from),b=rotate(d.to);const c=new Cube({...d,uuid:undefined,name:'converter_'+d.name,from:a.map((v,i)=>Math.min(v,b[i])),to:a.map((v,i)=>Math.max(v,b[i])),rotation:[0,0,0],origin:[0,0,0]}).addTo(g).init();
   const oldFaces=JSON.parse(JSON.stringify(d.faces));for(const side of ['north','south','east','west','up','down']){const old=oldFaces[({north:'south',south:'north',east:'west',west:'east',up:'up',down:'down'})[side]];c.faces[side].extend(old);if(old.texture!==null)c.faces[side].texture=ts[Number(old.texture)].uuid;}
   c.export=false;
  }
  Undo.finishEdit('Preview the actual separate Power Converter at the rear-right corner');
 }
 projects.push({project:Project,texture,name,scale});
}
for(const [name,defs] of Object.entries(components))await build(name,defs,1);
for(const [name,defs] of Object.entries(assemblies))await build(name+'_assembled',defs,3);
const atlas=cv.toDataURL();fs.writeFileSync(dir+'/metal_farms_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(assets+'/textures/block/metal_farms_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
const result=[];
for(const {project,texture,name,scale} of projects){project.select();Undo.initEdit({textures:[texture],elements:Cube.all.slice()});texture.fromDataURL(atlas);texture.uv_width=texture.uv_height=512;await new Promise(ok=>setTimeout(ok,30));Undo.finishEdit('Apply the dithered metal, original glass and farmland atlas');Canvas.updateAll();
 const compile=output=>{const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revocultivation:block/metal_farms_atlas';fs.writeFileSync(assets+'/models/block/'+output+'.json',JSON.stringify(raw,null,2)+'\n');};
 if(name.endsWith('_assembled')){
  for(const c of Cube.all)c.export=c.parent.name==='body';compile(name.replace('_assembled','_body'));
  if(name.startsWith('iron_greenhouse')){for(const c of Cube.all)c.export=c.parent.name==='lights';compile('iron_greenhouse_lights');}
  for(const c of Cube.all)c.export=['body','lights'].includes(c.parent.name);
 }else compile(name);
 Project.save_path=dir+'/'+name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
 result.push({name,cubes:Cube.all.length,runtimeCubes:Cube.all.filter(c=>c.export).length});
}
fs.writeFileSync(dir+'/manifest.json',JSON.stringify({nativeBlockbench:true,palette,atlas:[512,512],pixelsPerPhysicalUnit:1,assemblySourceScale:3,models:result,glass:'Mojang Minecraft glass',converterPlacement:[32,0,32],converterRemainsSeparateBlock:true},null,2));
return {models:result,tiles:tiles.size,atlasHeight:py+row};
})()
