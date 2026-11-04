(async()=>{
const fs=require('fs'),root='C:/Users/Victor/Documents/Codex/2026-10-31/https-github-com-revo1999-rassers-revolution/outputs/Rassers_Revolution_Sky_Edition',dir=root+'/art/blockbench/revoextraction/steel';
ModelProject.all.filter(p=>p.name==='steel_drill_assembled').at(-1).select();
fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
const c=document.createElement('canvas');c.width=c.height=16;const q=c.getContext('2d');
const rect=(x,y,w,h,color)=>{q.fillStyle=color;q.fillRect(x,y,w,h);};
rect(4,4,8,5,'#253637');rect(5,3,6,1,'#8eaaa8');rect(5,4,1,2,'#b9cbca');rect(10,4,1,2,'#657f7d');
rect(1,7,14,8,'#27383b');rect(2,7,12,1,'#becbc6');rect(2,8,12,6,'#71938e');rect(2,12,12,2,'#466662');rect(3,8,10,2,'#91aaa2');
for(const [x,y] of [[3,11],[6,10],[12,12],[4,13],[10,8],[8,13]])rect(x,y,1,1,'#60807d');
rect(2,10,12,1,'#344e51');rect(7,9,2,4,'#bcc7b8');rect(7,10,1,2,'#f0edd4');
rect(2,8,1,1,'#e0e5d9');rect(13,8,1,1,'#e0e5d9');rect(2,13,1,1,'#a7b6aa');rect(13,13,1,1,'#a7b6aa');
const png=Buffer.from(c.toDataURL('image/png').split(',')[1],'base64');fs.writeFileSync(dir+'/drill_repair_kit.png',png);fs.writeFileSync(root+'/revoextraction/src/main/resources/assets/revoextraction/textures/item/drill_repair_kit.png',png);
return {source:Project.save_path,animations:Animation.all.map(a=>({name:a.name,length:a.length})),icon:[16,16]};
})()
