(async()=>{
const fs=require('fs');
const root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/first-tier-machines',models=root+'/revocultivation/src/main/resources/assets/revocultivation/models/block';
const backup=dir+'/history/pre-framed-cage';fs.mkdirSync(backup,{recursive:true});
const report=[];
for(const name of ['mob_harvester_assembled','barred_panel','mob_filter']){
 let p=ModelProject.all.findLast(p=>p.save_path===dir+'/'+name+'.bbmodel');
 if(p)p.select();else{Codecs.project.load(JSON.parse(fs.readFileSync(dir+'/'+name+'.bbmodel','utf8')),{path:dir+'/'+name+'.bbmodel'});p=Project;}
 if(!fs.existsSync(backup+'/'+name+'.bbmodel'))fs.writeFileSync(backup+'/'+name+'.bbmodel',JSON.stringify(Codecs.project.compile({raw:true}),null,2));
 const runtimeName=name==='mob_harvester_assembled'?'mob_harvester_body':name;
 if(!fs.existsSync(backup+'/'+runtimeName+'.json'))fs.copyFileSync(models+'/'+runtimeName+'.json',backup+'/'+runtimeName+'.json');
 const scale=name==='mob_harvester_assembled'?3:1,tex=Texture.all[0],body=Group.all.find(g=>g.name==='body');
 const affected=[...Cube.all];Undo.initEdit({elements:affected,outliner:true});
 function map(c,mat){
  const dims=c.to.map((v,i)=>(v-c.from[i])*scale),base=mat==='dark'?[64,64]:mat==='log'?[64,0]:mat==='planks'?[0,0]:[0,64];
  for(const face of ['north','south','east','west','up','down']){
   const wh=['up','down'].includes(face)?[dims[0],dims[2]]:['east','west'].includes(face)?[dims[2],dims[1]]:[dims[0],dims[1]];
   const uv=mat==='log'&&['up','down'].includes(face)?[128,0]:base;
   c.faces[face].texture=tex.uuid;c.faces[face].uv=[uv[0],uv[1],uv[0]+wh[0],uv[1]+wh[1]];
  }
 }
 function cube(n,a,b,mat='metal'){
  const c=new Cube({name:n,from:a.map(v=>v/scale),to:b.map(v=>v/scale),box_uv:false,autouv:0}).addTo(body).init();map(c,mat);affected.push(c);return c;
 }
 if(scale===3){
  // Replace only the detached grille. Existing timber, controls and owner edits survive.
  for(const c of [...Cube.all])if(c.parent===body&&(/^(cage_|framed_cage_)/.test(c.name)))c.remove();
  for(const c of Cube.all){
   if(/^plank_(crossbeam|sidebeam)_5_/.test(c.name)){c.to[1]=14/3;map(c,'planks');}
   if(/^plank_(crossbeam|sidebeam)_44_/.test(c.name)){c.to[1]=48/3;map(c,'planks');}
   if(c.name==='filter_selector_plate'){c.from=[22/3,23/3,3/3];c.to=[26/3,27/3,4/3];map(c,'metal');}
   if(c.name==='filter_selector_knob'){c.from=[23/3,24/3,2/3];c.to=[25/3,26/3,3/3];const d=[2,2,1];for(const [f,x]of Object.entries(c.faces)){const wh=['up','down'].includes(f)?[d[0],d[2]]:['east','west'].includes(f)?[d[2],d[1]]:[d[0],d[1]];x.uv=[128,64,128+wh[0],64+wh[1]];}}
  }
  function panel(face){
   const transform=v=>face==='front'?v:face==='back'?[48-v[0],v[1],48-v[2]]:face==='left'?[v[2],v[1],48-v[0]]:[48-v[2],v[1],v[0]];
   const add=(n,a,b,mat)=>{const A=transform(a),B=transform(b);return cube('framed_cage_'+face+'_'+n,A.map((v,i)=>Math.min(v,B[i])),A.map((v,i)=>Math.max(v,B[i])),mat);};
   add('lower_rail',[9,13,3.75],[39,15,7.25],'metal');
   add('waist_rail',[9,25,3.75],[39,27,7.25],'metal');
   add('upper_rail',[9,41,3.75],[39,44,7.25],'metal');
   for(const x of [9.75,36])add('pillar_stile_'+x,[x,14,3.5],[x+2.25,44,7.5],'dark');
   for(const x of [14.5,20.5,26.5,32.5])add('inset_bar_'+x,[x,14,4.75],[x+1.25,42,6.25],'metal');
  }
  for(const face of ['front','back','left','right'])panel(face);
 }else{
  // Matching placeable panels, including an attached filter selector.
  for(const c of [...Cube.all])c.remove();
  cube('oak_edge_left',[0,0,6],[2,16,10],'log');cube('oak_edge_right',[14,0,6],[16,16,10],'log');
  cube('iron_header',[1,14,6],[15,16,10]);cube('iron_sill',[1,0,6],[15,2,10]);cube('iron_waist_rail',[1,6,6.5],[15,8,9.5]);
  for(const x of [4,8,12])cube('inset_bar_'+x,[x,1,7],[x+1,15,9]);
  if(name==='mob_filter'){cube('attached_selector_plate',[6,5,5],[10,9,6.5]);const c=cube('amber_selector',[7,6,4],[9,8,5]);for(const [f,d]of Object.entries(c.faces)){const wh=['up','down'].includes(f)?[2,1]:['east','west'].includes(f)?[1,2]:[2,2];d.uv=[128,64,128+wh[0],64+wh[1]];}}
 }
 if(scale===3){const actor=Group.all.find(g=>g.name==='actor');if(actor){actor.visibility=false;for(const c of Cube.all)if(c.parent===actor)c.visibility=false;}}
 Canvas.updateAll();Canvas.updateVisibility();Undo.finishEdit('Frame Harvester cage into timber and stone joints');
 const source=Codecs.project.compile({raw:true});fs.writeFileSync(p.save_path,JSON.stringify(source,null,2));
 const exported=Cube.all.map(c=>({c,value:c.export}));
 for(const c of Cube.all)c.export=c.parent===body;
 const runtime=Codecs.java_block.compile({raw:true});delete runtime.format_version;delete runtime.groups;
 runtime.parent='minecraft:block/block';runtime.render_type='minecraft:cutout';runtime.textures={'0':'revocultivation:block/first_tier_atlas',particle:'revocultivation:block/first_tier_atlas'};
 fs.writeFileSync(models+'/'+runtimeName+'.json',JSON.stringify(runtime,null,2));for(const {c,value}of exported)c.export=value;p.saved=true;
 if(scale===3){
  // The legacy placeholder is hidden only in this cage-design preview. Gameplay uses vanilla mobs.
  const actor=Group.all.find(g=>g.name==='actor'),visible=actor?.visibility;if(actor)actor.visibility=false;Canvas.updateAll();
  const preview=Preview.selected;preview.camera.position.set(18,17,-36);preview.controls.target.set(8,8,8);preview.controls.update();await new Promise(r=>setTimeout(r,180));preview.render();
  const img=await new Promise(r=>preview.screenshot({width:1000,height:800},r));fs.writeFileSync(dir+'/previews/harvester-framed-cage.png',Buffer.from(img.split(',')[1],'base64'));
  if(actor)actor.visibility=visible;Canvas.updateAll();
 }
 report.push({name,sourceCubes:Cube.all.length,runtimeCubes:runtime.elements.length});
}
ModelProject.all.findLast(p=>p.name==='mob_harvester_assembled').select();
fs.writeFileSync(dir+'/cage-refinement.json',JSON.stringify({design:'Continuous framed iron rails seated into timber posts and stone collars; inset bars; clear centre view; texture bolts; 16 px/world block',models:report},null,2));
return report;
})()
