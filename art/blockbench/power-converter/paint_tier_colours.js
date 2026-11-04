(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/power-converter',assets=root+'/revoindustry/src/main/resources/assets/revoindustry',history=dir+'/history/pre-tier-colours';
const tiers=[['power_converter',[45,86,132],'Blue',1],['advanced_power_converter',[54,109,82],'Green',2],['elite_power_converter',[136,57,50],'Red',3]];
const digits={1:['010','110','010','010','111'],2:['111','001','111','100','111'],3:['111','001','111','001','111']};
const result=[];
for(const [name,base,color,tier] of tiers){
 const outputs={};fs.mkdirSync(dir+'/textures/'+name,{recursive:true});
 for(const part of ['side','top','front','back']){
  const cv=document.createElement('canvas');cv.width=cv.height=16;const c=cv.getContext('2d');
  const rgb=d=>'rgb('+base.map(v=>Math.clamp(v+d,0,255)).join(',')+')';
  const rect=(x,y,w,h,col)=>{c.fillStyle=col;c.fillRect(x,y,w,h);};
  for(let y=0;y<16;y++)for(let x=0;x<16;x++){
   const hash=(x*13+y*29+x*y)%37;rect(x,y,1,1,rgb(hash<2?-8:hash>34?5:(x+y)%7===0?2:0));
  }
  rect(0,0,16,1,'#93a4ae');rect(0,0,1,16,'#718390');rect(15,0,1,16,'#26343e');rect(0,15,16,1,'#26343e');
  if(part==='side'){
   for(const y of [3,5]){rect(3,y,10,1,'#23323b');rect(3,y+1,10,1,rgb(16));}
   rect(3,10,10,3,rgb(-8));rect(3,10,10,1,rgb(15));
   for(const [x,y] of [[2,2],[13,2],[2,13],[13,13]])rect(x,y,1,1,'#b4bec1');
  }else if(part==='top'){
   // The model samples rows 0..14 on its top. A bold 6 x 10 numeral fits entirely inside that area.
   rect(1,1,14,12,rgb(5));rect(1,13,14,1,'#273740');
   const glyph=digits[tier];
   for(let y=0;y<5;y++)for(let x=0;x<3;x++)if(glyph[y][x]==='1')rect(6+x*2,3+y*2,2,2,'#1c2b34');
   for(let y=0;y<5;y++)for(let x=0;x<3;x++)if(glyph[y][x]==='1')rect(5+x*2,2+y*2,2,2,'#e1e4d8');
   for(const [x,y] of [[2,2],[13,2],[2,11],[13,11]])rect(x,y,1,1,'#b4bec1');
  }else{
   // Keep the shared teal FE and copper EU connector artwork intact inside the terminal.
   const img=new Image();img.src='data:image/png;base64,'+fs.readFileSync(history+'/'+name+'/'+part+'.png').toString('base64');await img.decode();c.drawImage(img,2,2,12,12,2,2,12,12);
  }
  const data=cv.toDataURL('image/png');outputs[part]=data;
  fs.writeFileSync(assets+'/textures/block/'+name+'/'+part+'.png',Buffer.from(data.split(',')[1],'base64'));
  fs.writeFileSync(dir+'/textures/'+name+'/'+part+'.png',Buffer.from(data.split(',')[1],'base64'));
 }
 const source=JSON.parse(fs.readFileSync(history+'/'+name+'.bbmodel','utf8'));
 newProject(Formats.java_block);Codecs.project.parse(source);Project.name=name;const elements=Cube.all.slice(),textures=Texture.all.slice();Undo.initEdit({elements,textures,outliner:true});
 for(const tex of textures){const part=tex.name.replace('.png','');tex.namespace='revoindustry';tex.folder='block/'+name;tex.fromDataURL(outputs[part]);tex.uv_width=tex.uv_height=16;}
 elements.find(e=>e.name==='steel_enclosure').faces.up.rotation=180;
 await new Promise(ok=>setTimeout(ok,35));Undo.finishEdit('Paint '+color.toLowerCase()+' tier '+tier+' with large top numeral');Canvas.updateAll();
 const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revoindustry:block/'+name+'/side';fs.writeFileSync(assets+'/models/block/'+name+'.json',JSON.stringify(raw,null,2)+'\n');
 Project.save_path=dir+'/'+name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;result.push({name,tier,color,base,cubes:elements.length});
}
fs.writeFileSync(dir+'/tier-colours.json',JSON.stringify(result,null,2)+'\n');return result;
})()
