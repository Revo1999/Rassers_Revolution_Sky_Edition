(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition';
const dir=root+'/art/blockbench/revoextraction',assets=root+'/revoextraction/src/main/resources/assets/revoextraction';

const names=['support','casing','core','controller','input','output_hatch','unassembled','assembled'],result=[];
let steelData;
for(const suffix of names){
 const source=JSON.parse(fs.readFileSync(dir+'/wooden_drill_'+suffix+'.bbmodel','utf8'));
 const variant=dir+'/variants/modern-industrial/wooden_drill_'+suffix+'.bbmodel';
 const modern=JSON.parse(fs.readFileSync(fs.existsSync(variant)?variant:dir+'/variants/modern-industrial/wooden_drill_assembled.bbmodel','utf8'));
 const density=['assembled','unassembled'].includes(suffix)?3:1;
 // Restore the saved higher-tier panel's UVs, while preserving all the user's corrected geometry.
 for(const cube of source.elements){if(!cube.name.startsWith('industrial_controller_'))continue;const old=modern.elements.find(c=>c.name===cube.name);if(old)for(const side of Object.keys(cube.faces)){
  const uv=old.faces[side].uv.slice(),d=cube.to.map((v,i)=>v-cube.from[i]);
  const wh=['up','down'].includes(side)?[d[0],d[2]]:['east','west'].includes(side)?[d[2],d[1]]:[d[0],d[1]];
  uv[2]=uv[0]+Math.sign(uv[2]-uv[0])*wh[0]*density;uv[3]=uv[1]+Math.sign(uv[3]-uv[1])*wh[1]*density;cube.faces[side].uv=uv;
 }}
 newProject(Formats.java_block);Codecs.project.parse(source);Project.name='iron_drill_'+suffix;
 const elements=Cube.all.slice(),textures=Texture.all.slice();Undo.initEdit({elements,textures,outliner:true});
 if(!steelData){
  const canvas=document.createElement('canvas');canvas.width=canvas.height=512;const ctx=canvas.getContext('2d');
  const img=new Image();img.src=source.textures[0].source;await img.decode();ctx.drawImage(img,0,0);
  const pixels=ctx.getImageData(0,0,512,512),d=pixels.data;
  for(let y=0;y<512;y++)for(let x=0;x<512;x++){
   const i=(y*512+x)*4,r=d[i],g=d[i+1],b=d[i+2];
   // Wood islands become blued iron; preserve the atlas's stone, black recesses and panel indicators.
   if(r>g*1.12&&g>b*1.12&&y<380){
    const lum=.299*r+.587*g+.114*b,noise=((x*13+y*23)%7)-3;
    const v=Math.round(72+lum*.32+noise);d[i]=v-8;d[i+1]=v;d[i+2]=v+10;
   }
  }
  ctx.putImageData(pixels,0,0);steelData=canvas.toDataURL('image/png');
  fs.writeFileSync(assets+'/textures/block/iron_drill_atlas.png',Buffer.from(steelData.split(',')[1],'base64'));
  fs.writeFileSync(dir+'/iron_drill_atlas.png',Buffer.from(steelData.split(',')[1],'base64'));
 }
 for(const texture of textures){texture.name='iron_drill_atlas.png';texture.namespace='revoextraction';texture.folder='block';texture.fromDataURL(steelData);texture.uv_width=texture.uv_height=512;}
 await new Promise(ok=>setTimeout(ok,40));
 Undo.finishEdit('Iron tier: blued steel frame and preserved modern controller');Canvas.updateAll();
 function compile(name){const raw=Codecs.java_block.compile({raw:true});delete raw.groups;delete raw.format_version;raw.parent='minecraft:block/block';raw.textures.particle='revoextraction:block/iron_drill_atlas';fs.writeFileSync(assets+'/models/block/iron_drill_'+name+'.json',JSON.stringify(raw,null,2)+'\n');}
 if(suffix==='assembled'){
  for(const group of ['body','drill_bit','drive_gear']){for(const cube of elements)cube.export=cube.parent?.name===group;compile(group);}
  for(const cube of elements)cube.export=true;Canvas.updateAll();
 }else if(suffix!=='unassembled')compile(suffix);
 Project.save_path=dir+'/'+Project.name+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
 result.push({name:Project.name,cubes:elements.length,faces:elements.reduce((n,c)=>n+Object.values(c.faces).filter(f=>f.texture!==null).length,0)});
}
return result;
})()
