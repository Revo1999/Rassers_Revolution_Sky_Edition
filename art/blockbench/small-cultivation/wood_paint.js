(ctx,canvas)=>{
const palette=['#735331','#8c653b','#a77c47','#be975b','#d1b078'];
const bayer=[[0,8,2,10],[12,4,14,6],[3,11,1,9],[15,7,13,5]];
const hash=(x,y)=>((x*73+y*151+x*y*19+47)%101)/100;
for(let y=0;y<16;y++)for(let x=0;x<16;x++){
const stave=Math.floor(x/4),grain=x%4;
let value=[2.1,2.55,1.85,2.35][stave];
value+=(hash(stave,Math.floor(y/3))-.5)*1.4;
value+=grain===0?-1.45:grain===1?.35:grain===2?-.05:-.3;
value+=(hash(Math.floor(x/2),Math.floor(y/2))-.5)*.65;
for(const[kx,ky]of[[2,10],[10,5]]){const d=Math.abs(x-kx)+Math.abs(y-ky)*.5;if(d<2.2)value-=1.2*(1-d/2.2);}
for(const[wx,wy]of[[6,4],[13,11]]){const d=Math.abs(x-wx)+Math.abs(y-wy)*.7;if(d<3)value+=.9*(1-d/3);}
value=Math.max(0,Math.min(4,value));const low=Math.floor(value),fraction=value-low;
const threshold=(bayer[y%4][x%4]+.5)/16;
const index=Math.min(4,low+(fraction>threshold?1:0));
ctx.fillStyle=palette[index];ctx.fillRect(2+x,42+y,1,1);
}
}
