// Native Blockbench geometry pass. Coordinates are physical Minecraft pixels before source scaling.
globalThis.revoIronStyles=new Map();
globalThis.revoIronStructure=(suffix,elements,texture)=>{
 const def=(name,from,to,style='blue',rotation=[0,0,0],origin=[0,0,0])=>({name,from,to,style,rotation,origin});
 const support=[
  def('column_central_web',[6,1,3],[10,15,13]),
  def('column_front_flange',[2,1,1],[14,15,3]),def('column_back_flange',[2,1,13],[14,15,15]),
  def('column_foot_plate',[1,0,0],[15,2,16],'steel'),def('column_head_plate',[1,14,0],[15,16,16],'steel'),
  def('column_front_web_rib_left',[4,2,3],[6,14,5],'steel'),def('column_front_web_rib_right',[10,2,3],[12,14,5],'steel'),
  def('column_rear_web_rib_left',[4,2,11],[6,14,13],'steel'),def('column_rear_web_rib_right',[10,2,11],[12,14,13],'steel')
 ];
 const casing=[
  def('fabricated_service_casing',[1,1,3],[15,15,16],'iron'),
  def('service_casing_left_rail',[0,0,1],[2,16,3],'steel'),def('service_casing_right_rail',[14,0,1],[16,16,3],'steel'),
  def('service_casing_top_rail',[2,14,1],[14,16,3],'steel'),def('service_casing_bottom_rail',[2,0,1],[14,2,3],'steel'),
  def('service_casing_vent_cassette',[4,5,0],[12,11,3],'vent')
 ];
 const core=[
  def('spindle_core_left_cheek',[1,2,3],[5,15,13]),def('spindle_core_right_cheek',[11,2,3],[15,15,13]),
  def('spindle_core_head_bridge',[3,12,2],[13,16,14],'iron'),def('spindle_core_chuck_mount',[4,1,4],[12,4,12],'steel')
 ];
 function apply(nodes,defs,parent,scale=1,shift=[0,0,0]){
  if(nodes.length>defs.length)throw Error('Unused inherited geometry in '+suffix);
  defs.forEach((d,i)=>{
   let c=nodes[i];
   if(!c){c=new Cube({name:d.name,box_uv:false}).addTo(parent).init();elements.push(c);}
   c.name=d.name;c.from=d.from.map((v,j)=>v/scale+shift[j]);c.to=d.to.map((v,j)=>v/scale+shift[j]);
   c.origin=d.origin.map((v,j)=>v/scale+shift[j]);c.rotation=d.rotation.slice();c.rescale=false;c.box_uv=false;
   for(const f of Object.values(c.faces)){f.texture=texture.uuid;f.cullface='';f.rotation=0;}
   revoIronStyles.set(d.name,d.style);
  });
 }
 if(suffix==='support'||suffix==='casing'||suffix==='core'){
  const nodes=elements.filter(c=>suffix!=='core'||!/bit_axle|helical_cutting/.test(c.name));
  apply(nodes,{support,casing,core}[suffix],nodes[0].parent);return;
 }
 if(suffix==='unassembled'){
  for(const group of Group.all.slice()){
   const kind=group.name.split('_')[0];if(!['support','casing','core'].includes(kind))continue;
   const nodes=elements.filter(c=>c.parent===group&&(kind!=='core'||!/bit_axle|helical_cutting/.test(c.name)));
   apply(nodes,{support,casing,core}[kind],group,3,group.origin.map(v=>v-8/3));
  }
  return;
 }
 if(suffix!=='assembled')return;
 const body=Group.all.find(g=>g.name==='body');
 const keep=c=>/^industrial_controller_|^hatchblock_|^input_box_rim_/.test(c.name)||['input_bracket','shaft_bearing'].includes(c.name);
 const nodes=elements.filter(c=>c.parent===body&&!keep(c));
 const design=[];const add=(...a)=>design.push(def(...a));
 // Long skid base, with an opening behind the output hatch and a clear central drilling aperture.
 for(const x of [1,35])for(const z of [1,35])add('skid_anchor_'+x+'_'+z,[x,0,z],[x+12,4,z+12],'steel');
 add('right_long_skid',[37,1,5],[45,4,43]);
 add('left_front_skid',[3,1,5],[11,4,15]);add('left_rear_skid',[3,1,33],[11,4,43]);
 add('left_hatch_bypass_skid',[9,1,13],[12,4,35],'dark');
 add('front_base_crossmember',[11,1,3],[37,4,8]);add('rear_base_crossmember',[11,1,40],[37,4,45]);
 // Lower legs splay at 22.5 degrees, while upper load-bearing columns have a real I section.
 for(const z of [7,41])for(const [x,sign,topX] of [[6.5,-1,13],[41.5,1,35]]){
  const key=x+'_'+z;
  add('splayed_leg_'+key,[x-3,4,z-3],[x+3,21.3,z+3],'blue',[0,0,sign*22.5],[x,4,z]);
  add('upper_column_web_'+key,[topX-1,19,z-3],[topX+1,36,z+3]);
  add('upper_column_front_flange_'+key,[topX-3,19,z-4],[topX+3,36,z-2],'steel');
  add('upper_column_back_flange_'+key,[topX-3,19,z+2],[topX+3,36,z+4],'steel');
 }
 add('front_gantry_beam',[8,36,3],[40,41,11],'steel');add('rear_gantry_beam',[8,36,37],[40,41,45],'steel');
 add('left_gantry_beam',[8,36,11],[15,41,37]);add('right_gantry_beam',[33,36,11],[40,41,37]);
 for(const z of [7.5,40.5])for(const [x,sign] of [[13.5,-1],[34.5,1]])
  add('gantry_knee_brace_'+x+'_'+z,[x-1.5,29,z-1.5],[x+1.5,39,z+1.5],'steel',[0,0,sign*45],[x,29,z]);
 // A stepped central drive and heavy spindle carriage replace the old wooden box proportions.
 add('spindle_bearing_cradle',[15,19,15],[33,25,33],'steel');add('spindle_lower_bearing_ring',[18,18,18],[30,20,30],'copper');
 add('central_transmission_case',[11,25,11],[37,34,37],'iron');
 add('motor_lower_deck',[14,34,14],[34,39,34]);add('motor_upper_deck',[17,39,17],[31,44,31],'steel');
 add('motor_crown',[19,44,19],[29,47,29],'iron');
 add('input_transmission_web',[32,22,16],[38,34,32]);
 add('rear_lower_cooling_cassette',[16,29,37],[32,34,40],'vent');add('rear_upper_cooling_cassette',[18,34,34],[30,39,38],'vent');
 // Exposed collection duct stays outside the cutting sweep and connects to the lower hatch.
 add('output_riser_duct',[6,16,19],[10,27,29],'dark');add('output_elbow_duct',[8,23,19],[15,27,29],'steel');
 add('input_gearbox_seat',[37,15,16],[46,17,32],'steel');
 add('spindle_guide_left',[13,19,20],[15,27,22],'cutting');add('spindle_guide_right',[33,19,26],[35,27,28],'cutting');
 apply(nodes,design,body,3);
 const input=elements.find(c=>c.name==='input_bracket');input.from[0]=37/3;
 const mount=elements.find(c=>c.name==='industrial_controller_rear_mount');mount.from=[17/3,23/3,6/3];mount.to=[31/3,33/3,12/3];
};
