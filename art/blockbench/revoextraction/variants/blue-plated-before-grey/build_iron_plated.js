(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/revoextraction',assets=root+'/revoextraction/src/main/resources/assets/revoextraction',baseline=dir+'/variants/iron-before-plating';
const names=['support','casing','core','controller','input','output_hatch','unassembled','assembled'];
const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');ctx.fillStyle='#242c34';ctx.fillRect(0,0,512,512);
const palette={iron:[174,185,194],blue:[48,79,101],steel:[86,101,114],dark:[36,46,57],cutting:[154,171,184],copper:[148,99,68],vent:[40,64,81],screen:[21,32,41],recess:[21,28,34],button:[172,99,53]};
const tiles=new Map();let cursorX=2,cursorY=2,rowH=0;
function rgb(c,d=0){return 'rgb('+c.map(v=>Math.clamp(Math.round(v+d),0,255)).join(',')+')';}
function tile(style,width,height){
 const w=Math.max(1,Math.ceil(width-.0001)),h=Math.max(1,Math.ceil(height-.0001)),key=[style,w,h].join(':');if(tiles.has(key))return tiles.get(key);
 if(cursorX+w+2>512){cursorX=2;cursorY+=rowH+3;rowH=0;}if(cursorY+h+2>512)throw Error('Atlas overflow');
 const ox=cursorX,oy=cursorY;cursorX+=w+3;rowH=Math.max(rowH,h);const base=palette[style];
 const line=(x,y,W,H,color)=>{ctx.fillStyle=color;ctx.fillRect(ox+x,oy+y,W,H);};
 // Fresh sheet metal: discrete shades, sparse dither/wear, padded atlas islands. No inherited pixels.
 for(let y=-1;y<=h;y++)for(let x=-1;x<=w;x++){
  const xx=Math.clamp(x,0,w-1),yy=Math.clamp(y,0,h-1),hash=(xx*19+yy*37+xx*yy*7)%61;
  const wear=hash<3?-7:hash>58?5:0,dither=(xx+yy)%2===0&&hash%5===0?2:0;
  line(x,y,1,1,rgb(base,Math.floor((h-yy)/h*5)+wear+dither));
 }
 const rivet=(x,y)=>{line(x,y,2,2,'#303e49');line(x,y,1,1,'#d8e0e2');line(x+1,y+1,1,1,'#667b89');};
 if(w>=5&&h>=5){line(0,0,w,1,rgb(base,27));line(0,0,1,h,rgb(base,20));line(0,h-1,w,1,rgb(base,-32));line(w-1,0,1,h,rgb(base,-29));}
 if(['iron','blue','steel'].includes(style)){
  if(w>=10&&h>=9){for(let sx=0;sx<w;sx+=16)for(let sy=0;sy<h;sy+=16){
   const W=Math.min(16,w-sx),H=Math.min(16,h-sy);
   if(sx>0){line(sx,sy,1,H,rgb(base,-38));line(sx+1,sy,1,H,rgb(base,14));}
   if(sy>0){line(sx,sy,W,1,rgb(base,-38));line(sx,sy+1,W,1,rgb(base,14));}
   if(W>=8&&H>=8){rivet(sx+2,sy+2);rivet(sx+W-4,sy+2);rivet(sx+2,sy+H-4);rivet(sx+W-4,sy+H-4);}
  }}else if(w>=7&&h>=5){rivet(2,1);rivet(w-4,1);}
 }
 if(style==='blue'&&h>w*2&&w>=6){line(1,1,1,h-2,'#8297a1');line(w-2,1,1,h-2,'#1e3649');}
 if(style==='cutting'){line(0,0,w,1,'#e2e8e5');if(h>2)line(0,h-1,w,1,'#536572');}
 if(style==='copper'){line(0,0,w,1,'#cb9b70');if(h>3)line(1,h-2,w-2,1,'#644c3b');}
 if(style==='vent'&&w>=5&&h>=5)for(let y=2;y<h-2;y+=3){line(2,y,w-4,1,'#101a23');line(2,y+1,w-4,1,'#597184');}
 if(style==='screen'&&w>=9&&h>=10){line(0,0,w,h,'#253440');line(1,1,w-2,1,'#718590');line(2,2,w-4,5,'#0c1e28');line(3,3,w-6,1,'#9acac5');line(3,4,Math.max(2,w-8),2,'#4a9eae');line(2,8,w-4,1,'#647a88');line(2,h-3,2,1,'#cf9362');line(w-4,h-3,2,1,'#87b6a3');}
 if(style==='recess'&&w>=8&&h>=8){const cx=Math.floor(w/2);line(cx-1,2,2,h-5,'#7a949f');line(cx-2,h-5,4,1,'#b67f52');line(cx-1,h-4,2,1,'#b67f52');}
 const value=[ox,oy];tiles.set(key,value);return value;
}
function material(n,side){
 if(/display_panel/.test(n))return side==='north'?'screen':'dark';
 if(/confirm_button/.test(n))return 'button';
 if(/selector|bit_axle|shaft_socket|dark_hatch/.test(n))return /dark_hatch/.test(n)?'recess':'dark';
 if(/helical_cutting/.test(n))return ['up','down'].includes(side)?'cutting':'steel';
 if(/vent_|cooling_/.test(n))return ['up','east','west'].includes(side)?'vent':'blue';
 if(/stone_foot|stone_brick|footing_iron|iron_tie/.test(n))return 'steel';
 if(/gear_disc|bearing_oak|roof_iron/.test(n))return 'copper';
 if(/bearing|coupling|bit_mount|iron_band|rim_|frame_|output_lip/.test(n))return 'steel';
 if(/timber_leg|upright_oak|horizontal_|roof_oak|crossbeam/.test(n))return 'blue';
 if(/input_bracket|kinetic_housing|rear_mount|rear_shell|_wall/.test(n))return 'blue';
 if(/mechanism_wooden_body|planked_casing/.test(n))return side==='south'?'vent':'iron';
 return 'iron';
}
function rename(n){return n.replaceAll('upright_oak_post','riveted_iron_column').replaceAll('timber_leg','steel_column').replaceAll('stone_foot','steel_pedestal').replaceAll('stone_brick_collar','iron_clamp').replaceAll('planked_casing','riveted_plate_casing').replaceAll('mechanism_wooden_body','iron_gearbox_cabinet').replaceAll('top_bearing_oak','copper_bearing_ring').replaceAll('roof_oak_crossbeam','steel_roof_girder');}
const projects=[],result=[];
for(const suffix of names){
 const source=JSON.parse(fs.readFileSync(baseline+'/iron_drill_'+suffix+'.bbmodel','utf8'));
 newProject(Formats.java_block);Codecs.project.parse(source);Project.name='iron_drill_'+suffix;
 const elements=Cube.all.slice(),textures=Texture.all.slice();Undo.initEdit({elements,textures,outliner:true});const density=['assembled','unassembled'].includes(suffix)?3:1;
 if(suffix==='assembled'){
  const body=Group.all.find(g=>g.name==='body');
  for(const [name,from,to] of [['vent_cabinet_right',[39,28,12],[42,35,20]],['vent_cabinet_left',[6,28,28],[9,35,36]],['cooling_cartridge_right',[33,43,16],[38,46,32]],['cooling_cartridge_left',[10,43,16],[15,46,32]]]){
   const c=new Cube({name,from:from.map(v=>v/3),to:to.map(v=>v/3),box_uv:false}).addTo(body).init();for(const f of Object.values(c.faces))f.texture=textures[0].uuid;elements.push(c);
  }
 }
 for(const c of elements){const d=c.to.map((v,i)=>v-c.from[i]);for(const side of Object.keys(c.faces)){
  const f=c.faces[side];if(f.texture===null)continue;const wh=['up','down'].includes(side)?[d[0],d[2]]:['east','west'].includes(side)?[d[2],d[1]]:[d[0],d[1]];
  const w=wh[0]*density,h=wh[1]*density,[u,v]=tile(material(c.name,side),w,h);f.uv=[u,v,u+w,v+h];f.rotation=0;f.texture=textures[0].uuid;
 }c.name=rename(c.name);}
 Undo.finishEdit('Author riveted iron, navy steel and cooling details');projects.push({project:Project,elements,textures,suffix});
}
const atlas=canvas.toDataURL('image/png');fs.writeFileSync(dir+'/iron_drill_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));fs.writeFileSync(assets+'/textures/block/iron_drill_atlas.png',Buffer.from(atlas.split(',')[1],'base64'));
for(const {project,elements,textures,suffix} of projects){
 project.select();Undo.initEdit({elements,textures,outliner:true});for(const t of textures){t.name='iron_drill_atlas.png';t.namespace='revoextraction';t.folder='block';t.fromDataURL(atlas);t.uv_width=t.uv_height=512;}
 await new Promise(ok=>setTimeout(ok,35));Undo.finishEdit('Apply painted sheet-metal atlas');Canvas.updateAll();
 function compile(name){const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revoextraction:block/iron_drill_atlas';fs.writeFileSync(assets+'/models/block/iron_drill_'+name+'.json',JSON.stringify(raw,null,2)+'\n');}
 if(suffix==='assembled'){for(const g of ['body','drill_bit','drive_gear']){for(const c of elements)c.export=c.parent?.name===g;compile(g);}for(const c of elements)c.export=true;Canvas.updateAll();}else if(suffix!=='unassembled')compile(suffix);
 Project.save_path=dir+'/iron_drill_'+suffix+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
 result.push({name:Project.name,cubes:elements.length,faces:elements.reduce((a,c)=>a+Object.values(c.faces).filter(f=>f.texture!==null).length,0)});
}
fs.writeFileSync(dir+'/iron-material-manifest.json',JSON.stringify({palette,tiles:tiles.size,atlas:[512,512],paintedFromScratch:true,inheritedPixels:false,models:result},null,2)+'\n');
return {models:result,tiles:tiles.size,usedHeight:cursorY+rowH};
})()
