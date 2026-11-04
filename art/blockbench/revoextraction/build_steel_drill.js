(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/revoextraction/steel',assets=root+'/revoextraction/src/main/resources/assets/revoextraction';fs.mkdirSync(dir,{recursive:true});
const D=(name,from,to,style='paint',rotation=[0,0,0],origin=[0,0,0],group='body')=>({name,from,to,style,rotation,origin,group});
const body=[],add=(...args)=>body.push(D(...args));
// A new closed portal gantry on four cast pedestals, rather than the iron tier's splayed skid legs.
for(const x of [1,35])for(const z of [1,35]){
 const k=x+'_'+z;
 add('cast_steel_foot_'+k,[x,0,z],[x+12,5,z+12],'cast');
 add('column_base_shoe_'+k,[x+1,5,z+1],[x+11,8,z+11],'steel');
 add('box_section_column_'+k,[x+2,8,z+2],[x+10,39,z+10]);
 add('recessed_column_web_'+k,[x+4,10,z+1.8],[x+8,34,z+2],'dark');
 add('upper_gantry_shoe_'+k,[x,35,z],[x+12,40,z+12],'steel');
}
add('front_foundation_tie',[13,1,3],[35,5,10],'cast');add('rear_foundation_tie',[13,1,38],[35,5,45],'cast');
add('right_foundation_tie',[38,1,13],[45,5,35],'cast');
add('front_box_girder',[5,38,3],[43,44,12]);add('rear_box_girder',[5,38,36],[43,44,45]);
add('left_roof_rail',[3,39,12],[12,44,36],'steel');add('right_roof_rail',[36,39,12],[45,44,36],'steel');
// The two silver slide rails carry a stepped gearbox and keep the entire bit sweep unobstructed.
for(const x of [14,31]){
 add('vertical_spindle_slide_'+x,[x,20,17],[x+3,39,21],'polished');
 add('slide_bearing_shoe_'+x,[x-1,24,16],[x+4,31,22],'cast');
}
add('transmission_lower_saddle',[15,20,21],[33,25,33],'steel');
add('heavy_transmission_case',[14,25,14],[34,36,35]);
add('front_transmission_lid',[17,28,12],[31,34,14],'steel');
add('raised_motor_deck',[16,36,15],[32,42,33],'cast');
add('motor_armoured_crown',[18,42,17],[30,47,31],'steel');
add('rear_finned_heat_exchanger',[17,29,35],[31,36,40],'vent');
add('spindle_thrust_bearing',[18,18.8,18],[30,20.5,30],'copper');
// Full middle-layer electronic panel, shallow enough to stay outside the drill sweep.
add('controller_box',[16,16,0],[32,32,5],'steel');add('controller_display',[18,21, -.25],[30,30,0],'screen');
add('controller_switch_bank',[19,17,-.25],[29,20,0],'switch');
add('controller_mount',[17,29,5],[31,35,12],'cast');
// Ground-height output cabinet and a collection duct joining the upper transmission.
add('output_hatch_housing',[0,0,17],[11,16,31],'steel');
add('output_hatch_rim',[0,1,18],[1,15,30],'steel');add('output_hatch_socket',[-.2,3,20],[0,13,28],'output');
add('collection_riser',[9,16,19],[10.5,27,29],'cast');add('collection_elbow',[10,24,22],[15,27,28],'steel');
// Dedicated left-side coolant reservoir is a visible structural replacement, not an extra crate.
for(const x of [1,11])for(const z of [18,29])add('reservoir_corner_post_'+x+'_'+z,[x,19.5,z],[x+1,33,z+1],'tank');
add('reservoir_front_glass',[2,19.5,18],[11,33,18.15],'glass');
add('reservoir_rear_glass',[2,19.5,29.85],[11,33,30],'glass');
add('reservoir_outer_glass',[1,19.5,19],[1.15,33,29],'glass');
add('reservoir_inner_glass',[11.85,19.5,19],[12,33,29],'glass');
add('reservoir_lower_band',[.5,17.5,17.5],[12.5,19.5,30.5],'steel');add('reservoir_upper_band',[.5,33,17.5],[12.5,35,30.5],'steel');
add('tank_filler_neck',[4,35,21],[9,38,26],'cast');add('tank_filler_cap',[3.5,38,20.5],[9.5,39,26.5],'steel');
add('coolant_external_port',[0,22,22],[1,26,26],'inlet');
add('reservoir_outlet_pipe',[11,30,23],[14,37,25],'copper');
add('overhead_coolant_header',[12,36,23],[35,38,25],'copper');
for(const x of [13,33]){
 add('coolant_downpipe_'+x,[x,20,23],[x+2,36,25],'copper');
 add('spray_nozzle_'+x,[x-1,19.5,22],[x+3,21,26],'polished');
}
// Encased input presents a clear, flush Create shaft at Y=24, Z=24.
add('shaft_gearbox',[37,17,17],[44,31,31],'cast');
add('shaft_socket_recess',[44,19,19],[44.5,29,29],'dark');
for(const [name,a,b] of [['north',[44,17,17],[47,31,19]],['south',[44,17,29],[47,31,31]],['top',[44,29,19],[47,31,29]],['bottom',[44,17,19],[47,19,29]]])add('input_box_rim_'+name,a,b,'steel');
add('input_upper_link',[34,28,20],[38,34,28]);
const gear=[D('internal_drive_wheel',[40,20,20],[42,28,28],'copper',[0,0,0],[46,24,24],'drive_gear'),D('create_shaft_coupling',[44.25,22,22],[48,26,26],'polished',[0,0,0],[46,24,24],'drive_gear')];
const source=JSON.parse(fs.readFileSync(root+'/art/blockbench/revoextraction/iron_drill_assembled.bbmodel','utf8'));
const bitGroup=source.groups.find(g=>g.name==='drill_bit');const bitIds=source.outliner.find(g=>g.uuid===bitGroup.uuid).children;
const bit=source.elements.filter(c=>bitIds.includes(c.uuid)).map(c=>D('steel_'+c.name,c.from.map(v=>v*3),c.to.map(v=>v*3),c.name==='bit_axle'?'graphite':'bit',c.rotation||[0,0,0],(c.origin||[8,6,8]).map(v=>v*3),'drill_bit'));
const water=[D('preview_vanilla_water',[2.2,19.6,19.2],[10.8,32.8,28.8],'water',[0,0,0],[0,0,0],'preview_water')];
const component={
 support:[D('structural_box_column',[3,1,3],[13,15,13]),D('bottom_column_flange',[1,0,1],[15,2,15],'steel'),D('top_column_flange',[1,14,1],[15,16,15],'steel'),D('column_web',[6,3,2.8],[10,13,3],'dark')],
 casing:[D('steel_service_enclosure',[0,0,1],[16,16,16]),D('reinforced_service_lid',[1,1,0],[15,15,1],'steel'),D('recessed_cooling_grille',[4,5,-.1],[12,11,0],'vent')],
 core:[D('steel_core_body',[1,7,1],[15,16,15]),D('steel_core_mount',[3,5,3],[13,7,13],'steel'),D('core_spindle',[6,1,6],[10,5,10],'graphite'),D('core_cutting_disc',[4,1,4],[12,3,12],'bit')],
 controller:[D('controller_enclosure',[0,0,1],[16,16,16],'steel'),D('controller_display',[2,5,0],[14,14,1],'screen'),D('controller_switches',[3,1,0],[13,4,1],'switch')],
 input:[D('encased_input_box',[0,0,4],[16,16,16],'cast'),D('input_recess',[3,3,3],[13,13,4],'dark'),D('input_shaft',[6,6,0],[10,10,3],'polished'),D('input_rim_left',[1,1,1],[3,15,4],'steel'),D('input_rim_right',[13,1,1],[15,15,4],'steel'),D('input_rim_top',[3,13,1],[13,15,4],'steel'),D('input_rim_bottom',[3,1,1],[13,3,4],'steel')],
 output_hatch:[D('output_cabinet',[0,0,1],[16,16,16],'steel'),D('output_aperture',[3,3,0],[13,13,1],'output')],
 water_tank:[D('water_tank_top_band',[0,14,0],[16,16,16],'steel'),D('water_tank_bottom_band',[0,0,0],[16,2,16],'steel'),D('water_fitting',[6,6,0],[10,10,1],'inlet')]
};
for(const x of [0,14])for(const z of [0,14])component.water_tank.push(D('tank_frame_'+x+'_'+z,[x,2,z],[x+2,14,z+2],'tank'));
for(const [n,a,b] of [['front',[2,2,1],[14,14,1.15]],['back',[2,2,14.85],[14,14,15]],['left',[1,2,2],[1.15,14,14]],['right',[14.85,2,2],[15,14,14]]])component.water_tank.push(D('tank_glass_'+n,a,b,'glass'));
const cv=document.createElement('canvas');cv.width=cv.height=512;const ctx=cv.getContext('2d');ctx.fillStyle='#20292b';ctx.fillRect(0,0,512,512);
const load=async name=>{const img=new Image();img.src='data:image/png;base64,'+fs.readFileSync(dir+'/vanilla/'+name+'.png').toString('base64');await img.decode();const c=document.createElement('canvas');c.width=c.height=16;const q=c.getContext('2d');q.drawImage(img,0,0,16,16,0,0,16,16);return q.getImageData(0,0,16,16).data;};
const glassPixels=await load('glass'),waterPixels=await load('water_still');
const palette={paint:[66,94,91],steel:[145,155,156],cast:[71,79,82],polished:[166,180,184],dark:[24,32,35],copper:[141,105,73],tank:[157,169,159],screen:[24,44,45],switch:[76,89,87],inlet:[45,107,125],water:[42,132,165],graphite:[35,43,49],bit:[57,68,76],vent:[46,58,60],output:[23,29,31]};
const tiles=new Map();let px=2,py=2,row=0;
function tile(style,w,h){
 const W=Math.max(1,Math.ceil(w)),H=Math.max(1,Math.ceil(h)),key=[style,W,H].join(':');if(tiles.has(key))return tiles.get(key);
 if(px+W+2>512){px=2;py+=row+3;row=0;}if(py+H+2>512)throw Error('Steel atlas full');const x0=px,y0=py;px+=W+3;row=Math.max(row,H);
 const base=palette[style]||[190,220,225],fill=(x,y,w,h,c)=>{ctx.fillStyle=Array.isArray(c)?'rgb('+c.map(v=>Math.clamp(v,0,255)).join(',')+')':c;ctx.fillRect(x0+x,y0+y,w,h);};
 if(style==='glass'||style==='water'){
  ctx.clearRect(x0-1,y0-1,W+2,H+2);const data=style==='glass'?glassPixels:waterPixels;
  for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){
   const X=Math.clamp(x,0,W-1)%16,Y=Math.clamp(y,0,H-1)%16,i=(Y*16+X)*4,t=style==='water'?[63,118,228]:[255,255,255];
   fill(x,y,1,1,'rgba('+[0,1,2].map(k=>Math.round(data[i+k]*t[k]/255)).join(',')+','+(style==='water'?.78:data[i+3]/255)+')');
  }
  tiles.set(key,[x0,y0]);return [x0,y0];
 }
 const shade=d=>base.map(v=>Math.clamp(v+d,0,255));
 for(let y=-1;y<=H;y++)for(let x=-1;x<=W;x++){
  const X=Math.clamp(x,0,W-1),Y=Math.clamp(y,0,H-1),patch=((X>>2)*13+(Y>>2)*7)%5-2,hash=(X*29+Y*17+X*Y*11)%67;
  const d=(X+Y)%2===0?4:-4;fill(x,y,1,1,shade(d+patch*3+(hash<5?-11:hash>62?8:0)));
 }
 if(W>=4&&H>=4){fill(0,0,W,1,shade(22));fill(0,0,1,H,shade(13));fill(0,H-1,W,1,shade(-24));fill(W-1,0,1,H,shade(-26));}
 const rivet=(x,y)=>{fill(x,y,2,2,'#354548');fill(x,y,1,1,'#c2ccca');};
 if(['paint','steel','tank','cast'].includes(style)&&W>=9&&H>=8){rivet(2,2);rivet(W-4,2);rivet(2,H-4);rivet(W-4,H-4);}
 if(style==='vent'&&W>=6)for(let y=2;y<H-1;y+=3){fill(1,y,W-2,1,'#142125');fill(1,y+1,W-2,1,'#687f80');}
 if(style==='screen'&&W>=9&&H>=7){fill(1,1,W-2,H-2,'#172e32');fill(2,2,W-4,1,'#8cc5bc');fill(2,3,Math.max(1,W-7),1,'#3c8c8a');fill(2,H-3,2,1,'#bd895a');fill(W-4,H-3,2,1,'#97b69c');}
 if(style==='switch'&&W>=7){for(let x=1;x<W-1;x+=3)fill(x,1,2,Math.min(2,H-1),'#bcc3b6');}
 if(style==='water'){for(let y=2;y<H-1;y+=4)fill(0,y,Math.max(1,W-1),1,'#6db7c7');if(W>=3)fill(1,0,1,H,'#90d0dc');}
 if(style==='output'&&W>=7&&H>=7){fill(2,1,W-4,1,'#8d9b9d');const c=Math.floor(W/2);fill(c-1,2,2,H-4,'#91a0a1');fill(c-2,H-4,4,1,'#91a0a1');}
 if(style==='inlet'&&W>=4&&H>=4){fill(1,1,W-2,H-2,'#203c48');fill(1,1,W-2,1,'#88bcc5');}
 if(style==='bit'){fill(0,0,W,1,'#71858e');}
 tiles.set(key,[x0,y0]);return [x0,y0];
}
const projects=[];
async function build(name,defs,scale=1){
 newProject(Formats.free);Project.name='steel_drill_'+name;Project.texture_width=Project.texture_height=512;
 const texture=new Texture({name:'steel_drill_atlas.png',namespace:'revoextraction',folder:'block',uv_width:512,uv_height:512}).fromDataURL(cv.toDataURL()).add(false);
 Undo.initEdit({elements:[],textures:[texture],outliner:true});
 const groups={};for(const d of defs){if(!groups[d.group])groups[d.group]=new Group({name:d.group,origin:(d.group==='drill_bit'?[24,18,24]:d.group==='drive_gear'?[46,24,24]:[0,0,0]).map(v=>v/scale)}).init();
  const c=new Cube({name:d.name,from:d.from.map(v=>v/scale),to:d.to.map(v=>v/scale),rotation:d.rotation,origin:d.origin.map(v=>v/scale),box_uv:false}).addTo(groups[d.group]).init();
  const size=d.to.map((v,i)=>v-d.from[i]);for(const [side,f] of Object.entries(c.faces)){
   const wh=['up','down'].includes(side)?[size[0],size[2]]:['east','west'].includes(side)?[size[2],size[1]]:[size[0],size[1]];
   const [u,v]=tile(d.style,...wh);f.texture=texture.uuid;f.uv=[u,v,u+wh[0],v+wh[1]];f.rotation=0;f.cullface='';
  }
 }
 Undo.finishEdit('Build a water-cooled steel portal drill with an integrated side tank');
 if(name==='assembled'){
  Undo.initEdit({animations:[]});const animators={};
  for(const name of ['drill_bit','drive_gear'])animators[groups[name].uuid]={name,type:'bone',rotation_global:false,quaternion_interpolation:false,keyframes:[0,1,2,3,4].map(time=>({channel:'rotation',time,interpolation:'linear',data_points:[name==='drill_bit'?{x:'0',y:String(time*90),z:'0'}:{x:String(-time*90),y:'0',z:'0'}]}))};
  new Animation({name:'working',length:4,loop:'loop',snapping:24,animators}).add();Undo.finishEdit('Add a continuous four-second working preview');
 }
 projects.push({project:Project,texture,name,scale});
}
for(const [name,defs] of Object.entries(component))await build(name,defs);
const unassembled=[];
for(let x=-1;x<=1;x++)for(let y=0;y<3;y++)for(let z=-1;z<=1;z++){
 let kind=Math.abs(x)===1&&Math.abs(z)===1?'support':x===0&&z===0&&y===0?'air':x===0&&z===0&&y===1?'core':x===0&&z===1&&y===1?'controller':x===-1&&z===0&&y===0?'output_hatch':x===1&&z===0&&y===1?'input':x===-1&&z===0&&y===1?'water_tank':'casing';
 if(kind==='air')continue;for(const d of component[kind]){
  const shift=[(x+1)*16,y*16,(1-z)*16];let from=d.from.slice(),to=d.to.slice();
  // Standalone ports face north. In the guide the shaft faces right and output faces left.
  if(kind==='input'||kind==='output_hatch'){
   const sign=kind==='input'?1:-1;const rotate=p=>[8+sign*(8-p[2]),p[1],8+sign*(p[0]-8)];
   const a=rotate(from),b=rotate(to);from=a.map((v,i)=>Math.min(v,b[i]));to=a.map((v,i)=>Math.max(v,b[i]));
  }
  unassembled.push({...d,name:d.name+'_'+x+'_'+y+'_'+z,from:from.map((v,i)=>v+shift[i]),to:to.map((v,i)=>v+shift[i])});
 }
}
await build('unassembled',unassembled,3);await build('assembled',[...body,...bit,...gear,...water],3);
const atlas=cv.toDataURL('image/png');fs.writeFileSync(dir+'/steel_drill_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(assets+'/textures/block/steel_drill_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
const result=[];
for(const item of projects){item.project.select();Undo.initEdit({textures:[item.texture],elements:Cube.all.slice()});item.texture.fromDataURL(atlas);item.texture.uv_width=item.texture.uv_height=512;await new Promise(ok=>setTimeout(ok,30));Undo.finishEdit('Apply dithered painted steel, cast metal and coolant atlas');Canvas.updateAll();
 const compile=name=>{const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revoextraction:block/steel_drill_atlas';fs.writeFileSync(assets+'/models/block/steel_drill_'+name+'.json',JSON.stringify(raw,null,2)+'\n');};
 if(item.name==='assembled'){
  for(const group of ['body','drill_bit','drive_gear']){for(const c of Cube.all)c.export=c.parent?.name===group;compile(group);}for(const c of Cube.all)c.export=c.parent?.name!=='preview_water';
 }else if(item.name!=='unassembled')compile(item.name);
 Project.save_path=dir+'/steel_drill_'+item.name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
 result.push({name:Project.name,cubes:Cube.all.length,faces:Cube.all.length*6});
}
Canvas.updateAll();fs.writeFileSync(dir+'/manifest.json',JSON.stringify({models:result,palette,atlas:[512,512],physicalEnvelope:[48,48,48],sourceScale:3,pixelsPerBlock:16,shaftCenter:[48,24,24],fluidInterior:{from:[2.2,19.6,19.2],to:[10.8,32.8,28.8],runtimeTexture:'minecraft:block/water_still',previewUsesFirstVanillaFrame:true},nativeBlockbench:true,tiles:tiles.size},null,2));
return {models:result,tiles:tiles.size,atlasHeight:py+row};
})()
