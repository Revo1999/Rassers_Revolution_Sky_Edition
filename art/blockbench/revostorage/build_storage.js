(async()=>{
const fs=require('fs'),root="C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition";
const dir=root+'/art/blockbench/revostorage';
const palette={shell:'#a7abb9',edge:'#515368',recess:'#242737',screen:'#202c40',cyan:'#58c8db',copper:'#ad794e',dark:'#36394c',green:'#68ba8d'};
const results=[];
for(const [name,mod] of [['chest_drive','revostorage'],['crafting_terminal','revostorage'],['electric_drill','revoextraction'],['trading_colony','revocultivation']]){
 newProject(Formats.java_block);Project.name=name;Project.texture_width=Project.texture_height=64;
 const canvas=document.createElement('canvas');canvas.width=canvas.height=64;const ctx=canvas.getContext('2d');
 const styles=Object.keys(palette);styles.forEach((style,i)=>{const ox=(i%4)*16,oy=Math.floor(i/4)*16;ctx.fillStyle=palette[style];ctx.fillRect(ox,oy,16,16);for(let y=0;y<16;y++)for(let x=0;x<16;x++){if((x*13+y*19)%23<3){ctx.fillStyle='rgba(255,255,255,0.07)';ctx.fillRect(ox+x,oy+y,1,1);}}ctx.fillStyle='rgba(255,255,255,0.2)';ctx.fillRect(ox,oy,16,1);ctx.fillRect(ox,oy,1,16);ctx.fillStyle='rgba(0,0,0,0.25)';ctx.fillRect(ox,oy+15,16,1);});
 // Screen graphics are painted pixels; bay ridges stay geometry for parallax.
 ctx.fillStyle='#69bdcf';for(let y=2;y<14;y+=3){ctx.fillRect(49,y,11-(y%4),1);}ctx.fillStyle='#94d4e0';ctx.fillRect(50,12,3,2);
 const tex=new Texture({name:name+'.png'}).fromDataURL(canvas.toDataURL());tex.add(false);
 Undo.initEdit({elements:[],textures:[tex],outliner:true});const created=[];
 function cube(n,from,to,style){const c=new Cube({name:n,from,to,autouv:0}).init();const i=styles.indexOf(style),u=(i%4)*4,v=Math.floor(i/4)*4;const size=to.map((a,k)=>a-from[k]);for(const side of ['north','south','east','west','up','down']){const w=['north','south','up','down'].includes(side)?size[0]:size[2],h=['up','down'].includes(side)?size[2]:size[1];c.faces[side].texture=tex.uuid;c.faces[side].uv=[u*4,v*4,u*4+w,v*4+h];if((side==='south'&&to[2]===16)||(side==='west'&&from[0]===0)||(side==='east'&&to[0]===16)||(side==='down'&&from[1]===0)||(side==='up'&&to[1]===16))c.faces[side].cullface=side;}created.push(c);return c;}
 // UV logical grid is 16x16 for Java: 64px atlas => four pixels per logical UV unit.
 cube('body',[0,0,3],[16,16,16],'shell');cube('recess',[1,1,2],[15,15,3],'recess');
 cube('left_frame',[0,0,0],[1,16,3],'edge');cube('right_frame',[15,0,0],[16,16,3],'edge');cube('top_frame',[1,15,0],[15,16,3],'shell');cube('bottom_frame',[1,0,0],[15,1,3],'edge');
 if(name==='chest_drive'){
  for(let x=0;x<3;x++)for(let y=0;y<3;y++)cube('bay_'+x+'_'+y,[2+x*4.25,2+y*4.25,1],[5.5+x*4.25,5.5+y*4.25,2],'dark');
  for(let y=0;y<3;y++)cube('bay_status_'+y,[13.5,3+y*4.25,.75],[14,4+y*4.25,1.25],'green');
 }else if(name==='crafting_terminal'){
  cube('display',[2,5,1],[14,14,2],'screen');cube('screen_border',[1.5,4.5,.5],[14.5,5,2],'cyan');cube('keyboard',[2,2,0],[14,4,2],'dark');cube('spring_winder',[12,1,0],[14,2,1],'copper');
 }else if(name==='electric_drill'){
  cube('motor',[3,4,0],[13,13,2],'dark');cube('bit_mount',[6,1,0],[10,5,2],'copper');cube('sensor',[2,13,0],[6,15,2],'cyan');for(let y=5;y<12;y+=2)cube('motor_rib_'+y,[3,y,-.5],[13,y+.5,.5],'edge');
 }else{cube('trade_window',[2,6,0],[14,13,2],'copper');cube('counter',[1,4,-1],[15,6,3],'dark');cube('emerald_sign',[6,13,0],[10,15,1],'green');}
 Undo.finishEdit('Build '+name,{elements:created,textures:[tex],outliner:true});Canvas.updateAll();
 const assets=root+'/'+mod+'/src/main/resources/assets/'+mod;fs.mkdirSync(assets+'/textures/block',{recursive:true});fs.mkdirSync(assets+'/models/block',{recursive:true});
 fs.writeFileSync(assets+'/textures/block/'+name+'.png',Buffer.from(canvas.toDataURL().split(',')[1],'base64'));
 const compiled=JSON.parse(Codecs.java_block.compile());compiled.textures={'0':mod+':block/'+name,particle:mod+':block/'+name};compiled.display={gui:{rotation:[30,225,0],translation:[0,0,0],scale:[.625,.625,.625]},ground:{translation:[0,3,0],scale:[.25,.25,.25]},fixed:{scale:[.5,.5,.5]}};
 fs.writeFileSync(assets+'/models/block/'+name+'.json',JSON.stringify(compiled,null,2));fs.writeFileSync(dir+'/'+name+'.bbmodel',Codecs.project.compile());
 Project.save_path=dir+'/'+name+'.bbmodel';Project.saved=true;
 const preview=Preview.selected;preview.camera.position.set(32,25,-36);preview.controls.target.set(8,8,8);preview.controls.update();await new Promise(resolve=>preview.screenshot({width:800,height:700},data=>{fs.writeFileSync(dir+'/'+name+'.png',Buffer.from(data.split(',')[1],'base64'));resolve();}));results.push({name,elements:created.length,faces:created.length*6});
}
return results;
})()