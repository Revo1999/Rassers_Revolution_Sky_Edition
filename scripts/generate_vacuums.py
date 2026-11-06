from pathlib import Path
import json,math
from PIL import Image,ImageDraw
root=Path('.');res=root/'revoindustry/src/main/resources';art=root/'art/blockbench/vacuums';art.mkdir(parents=True,exist_ok=True)
def put(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n')
lang=json.loads((res/'assets/revoindustry/lang/en_us.json').read_text());tag=res/'data/minecraft/tags/block/mineable/pickaxe.json';tags=json.loads(tag.read_text())
def geometry(tier):
 parts=[('mount',[2,0,9],[14,1.75,15],'dark'),('motor',[3,3,9],[13,13,15],'body'),('motor_cap',[2,12.125,8.75],[14,13.75,14.75],'metal'),('motor_foot',[2.75,1.75,8.5],[13.25,2.875,14.5],'metal'),('rear_plate',[2,2,15],[14,14,15.75],'metal'),('rear_output',[4,4,15.8],[12,12,16],'output'),('fan_shadow',[4,4,8.6],[12,12,8.7],'fan'),('status',[12.125,8,8.45],[13,10,8.6],'light')]
 rotations={};radius=[5,6,7][tier-1];start=[2,1,0][tier-1]
 for section,z0,z1,r,wall,mat in [('flange',start,start+2.5,radius,1.5,'metal'),('duct',start+2,10,radius-.65,1.35,'dark')]:
  half=r*math.tan(math.pi/8);outer=8+r;inner=outer-wall
  for side in range(8):
   name=f'{section}_{side}';front=z0+side*.02;back=z1+side*.02
   if side in [0,1,7]:a,b=[8-half,inner,front],[8+half,outer,back]
   elif side in [3,4,5]:a,b=[8-half,16-outer,front],[8+half,16-inner,back]
   elif side==2:a,b=[16-outer,8-half,front],[16-inner,8+half,back]
   else:a,b=[inner,8-half,front],[outer,8+half,back]
   parts.append((name,a,b,mat))
   if side in [1,5]:rotations[name]=45
   if side in [3,7]:rotations[name]=-45
 if tier>=2:
  parts.extend([('left_cooling',[2.625,6,11],[2.875,10,14.6],'grille'),('right_cooling',[13.125,6,11],[13.375,10,14.6],'grille')])
 if tier==3:
  parts.extend([('left_brace',[1.5,3,10],[2.5,11,14.5],'metal'),('right_brace',[13.5,3,10],[14.5,11,14.5],'metal'),('top_rail_left',[4,13.875,10],[5,14.875,14],'dark'),('top_rail_right',[10,13.875,10],[11,14.875,14],'dark'),('status_aux',[10,8,8.3],[11,10,8.4],'light')])
 return parts,rotations
for tier,color in enumerate([(155,54,43),(43,110,159),(123,71,172)],1):
 parts,rotations=geometry(tier)
 textures={};palette={'body':color,'metal':(151,159,160),'dark':(49,57,62),'grille':(28,36,40),'light':(192,238,236),'fan':(24,30,33),'output':(58,68,73)}
 for material,base in palette.items():
  im=Image.new('RGB',(16,16));pix=im.load()
  for y in range(16):
   for x in range(16):
    bayer=((0,8,2,10),(12,4,14,6),(3,11,1,9),(15,7,13,5))
    ramp=(x+y)/30
    threshold=int((.15+.7*ramp)*16)
    n=-13 if bayer[y%4][x%4]<threshold else 9
    if material=='dark':n=-6 if bayer[y%4][x%4]<threshold else 5
    elif material=='grille':n=-5 if x%3 else 21
    elif material=='fan':n=-2 if (x+y)%2 else 2
    elif material=='metal':n+=9 if y==0 or x==0 else -10 if x==15 or y==15 else 0
    pix[x,y]=tuple(max(0,min(255,v+n)) for v in base)
  if material=='metal':
   d=ImageDraw.Draw(im);d.point((2,1),fill=(68,79,84));d.point((3,1),fill=(210,218,218))
  if material=='body':
   d=ImageDraw.Draw(im);d.line((0,0,15,0),fill=tuple(min(255,v+26) for v in base));d.line((0,15,15,15),fill=tuple(v//2 for v in base));d.rectangle((3,4,12,5),fill=(199,203,193));d.rectangle((4,5,11,6),fill=(41,48,50));d.rectangle((6,9,9,10),fill=(204,210,205))
  if material=='output':
   d=ImageDraw.Draw(im);d.rectangle((0,0,7,7),fill=(118,133,141));d.rectangle((1,1,6,6),fill=tuple(min(255,v+16) for v in color));d.rectangle((2,2,5,5),fill=(29,38,43));d.line((2,3,5,3),fill=(206,225,225));d.point((4,2),fill=(206,225,225));d.point((4,4),fill=(206,225,225))
  if material=='fan':
   d=ImageDraw.Draw(im);d.ellipse((0,0,7,7),fill=(42,50,54));d.line((1,1,6,6),fill=(130,143,148),width=2);d.line((1,6,6,1),fill=(103,119,126),width=2);d.rectangle((3,3,4,4),fill=(173,184,188))
  path=res/f'assets/revoindustry/textures/block/vacuum_{tier}_{material}.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path);textures[material]=f'revoindustry:block/vacuum_{tier}_{material}'
 elements=[]
 for name,a,b,mat in parts:
  dx,dy,dz=[b[i]-a[i] for i in range(3)];faces={}
  for face,(w,h) in {'north':(dx,dy),'south':(dx,dy),'east':(dz,dy),'west':(dz,dy),'up':(dx,dz),'down':(dx,dz)}.items():faces[face]={'texture':'#'+mat,'uv':[0,0,w,h]}
  element={'name':name,'from':a,'to':b,'faces':faces}
  if name in rotations:element['rotation']={'origin':[8,8,0],'axis':'z','angle':rotations[name]}
  elements.append(element)
 model={'ambientocclusion':True,'textures':dict(textures,particle=textures['metal']),'elements':elements,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.65,.65,.65]},'fixed':{'rotation':[0,180,0],'scale':[.7,.7,.7]},'ground':{'translation':[0,3,0],'scale':[.4,.4,.4]}}}
 put(art/f'vacuum_{tier}.json',model)
 # Runtime copy is subsequently replaced with the native Blockbench Java codec export.
 put(res/f'assets/revoindustry/models/block/vacuum_{tier}.json',model)
 variants={}
 for face,rotation in {'south':{},'north':{'y':180},'east':{'y':270},'west':{'y':90},'up':{'x':90},'down':{'x':270}}.items():variants['facing='+face]={'model':f'revoindustry:block/vacuum_{tier}',**rotation}
 put(res/f'assets/revoindustry/blockstates/vacuum_{tier}.json',{'variants':variants})
 put(res/f'assets/revoindustry/models/item/vacuum_{tier}.json',{'parent':f'revoindustry:block/vacuum_{tier}'})
 put(res/f'data/revoindustry/loot_table/blocks/vacuum_{tier}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'revoindustry:vacuum_{tier}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 material=['minecraft:iron_ingot','modern_industrialization:steel_ingot','minecraft:diamond'][tier-1]
 centre='minecraft:hopper' if tier==1 else f'revoindustry:vacuum_{tier-1}'
 put(res/f'data/revoindustry/recipe/vacuum_{tier}.json',{'type':'minecraft:crafting_shaped','pattern':[' M ','MCM','RHR'],'key':{'M':{'item':material},'C':{'item':centre},'R':{'item':'minecraft:redstone'},'H':{'item':'minecraft:hopper'}},'result':{'id':f'revoindustry:vacuum_{tier}','count':1}})
 lang[f'block.revoindustry.vacuum_{tier}']=f'Vacuum Mk {tier}'
 if f'revoindustry:vacuum_{tier}' not in tags['values']:tags['values'].append(f'revoindustry:vacuum_{tier}')
lang.update({'tooltip.revoindustry.vacuum.stats':'Radius: %s blocks | %s slots | %s items/s','tooltip.revoindustry.vacuum.outlet':'Place on an inventory to aim the rear plate outlet into it.','tooltip.revoindustry.vacuum.wrench':'Wrench rotates the outlet; no power required.'})
put(res/'assets/revoindustry/lang/en_us.json',lang);put(tag,tags)
put(art/'plan.json',{'target':'Minecraft Java 1.21.1 NeoForge desktop','format':'java_block','shading':'Opaque vanilla albedo with deliberate pixel dithering','texel_density':'16 pixels per block; proportionate per-face UVs','budget':'24 / 26 / 31 cubes for distinct tier silhouettes','textures':'Seven shared 16px material tiles per tier; ordered Bayer dithering','animation':'Runtime item attraction and native end-rod suction stream','bounds':'All geometry within one 16x16x16 block','pipeDiameterPixels':[10,12,14],'seams':'Ring segments have unique front/back depths; cap and trim surfaces are separated','output':'Rear plate opposite the intake; local south, six-way facing'})
base=str(root.resolve()).replace('\\','/')
js="""(async()=>{
 const fs=require('fs'),root=ROOT,dir=root+'/art/blockbench/vacuums',report=[];
 for(let tier=1;tier<=3;tier++){
  const data=JSON.parse(fs.readFileSync(dir+'/vacuum_'+tier+'.json','utf8'));
  newProject(Formats.java_block);Project.name='vacuum_'+tier;Project.texture_width=Project.texture_height=16;
  Undo.initEdit({elements:[],textures:[],outliner:true});const textures={};
  for(const [name,path] of Object.entries(data.textures)){if(name==='particle')continue;const png=fs.readFileSync(root+'/revoindustry/src/main/resources/assets/'+path.replace(':','/textures/')+'.png');textures[name]=new Texture({name:path.split('/').pop()+'.png',folder:'block',namespace:'revoindustry',id:name,uv_width:16,uv_height:16}).fromDataURL('data:image/png;base64,'+png.toString('base64')).add(false);}
  const g=new Group({name:'single_block_vacuum'}).init();
  for(const part of data.elements){const c=new Cube({name:part.name,from:part.from,to:part.to,rotation:part.rotation?[0,0,part.rotation.angle]:[0,0,0],origin:part.rotation?part.rotation.origin:[8,8,8],box_uv:false}).addTo(g).init();for(const [face,f] of Object.entries(part.faces)){c.faces[face].texture=textures[f.texture.slice(1)].uuid;c.faces[face].uv=f.uv;}}
  Undo.finishEdit('Build industrial vacuum with large hollow intake pipe tier '+tier);Canvas.updateAll();
  Project.save_path=dir+'/vacuum_'+tier+'.bbmodel';fs.writeFileSync(Project.save_path,Codecs.project.compile());Project.saved=true;
  const output=JSON.parse(Codecs.java_block.compile());for(const [k,v] of Object.entries(output.textures||{}))if(typeof v==='object')output.textures[k]=v.sprite;output.textures.particle=data.textures.metal;output.display=data.display;
  fs.writeFileSync(root+'/revoindustry/src/main/resources/assets/revoindustry/models/block/vacuum_'+tier+'.json',JSON.stringify(output,null,2));
  const view=Preview.selected;view.controls.target.set(0,8,-.5);view.camera.position.set(25,28,-36);view.controls.update();Canvas.updateAll();await new Promise(ok=>setTimeout(ok,100));await new Promise(ok=>view.screenshot({width:700,height:700},url=>{fs.writeFileSync(dir+'/vacuum_'+tier+'-preview.png',Buffer.from(url.split(',')[1],'base64'));ok();}));
  report.push({tier,cubes:Cube.all.length,bounds:[0,16],textures:Texture.all.length});
 }fs.writeFileSync(dir+'/validation.json',JSON.stringify(report,null,2));return report;
})()
""".replace('ROOT',json.dumps(base))
(art/'build_native.js').write_text(js)
