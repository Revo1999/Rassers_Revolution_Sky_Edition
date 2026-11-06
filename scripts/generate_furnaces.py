from pathlib import Path
import json,colorsys,zipfile,io
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'revoindustry/src/main/resources';ART=ROOT/'art/blockbench/furnaces'
ART.mkdir(parents=True,exist_ok=True)
def put(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
def asset(path,obj):put(RES/'assets/revoindustry'/path,obj)
def recipe(name,pattern,key,result,count=1):put(RES/f'data/revoindustry/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:({'tag':v[1:]} if v.startswith('#') else {'item':v}) for k,v in key.items()},'result':{'id':result,'count':count}})
def block(name,model):
 asset(f'blockstates/{name}.json',{'variants':{'':{'model':f'revoindustry:block/{name}'}}});asset(f'models/block/{name}.json',model);asset(f'models/item/{name}.json',{'parent':f'revoindustry:block/{name}'})
 put(RES/f'data/revoindustry/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'revoindustry:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
langpath=RES/'assets/revoindustry/lang/en_us.json';lang=json.loads(langpath.read_text(encoding='utf-8-sig'))
# Material-appropriate native MI casings; other tiers use riveted plates drawn from the supplied reference.
native={'copper':'bronze_machine_casing','iron':'steel_machine_casing','netherite':'blastproof_casing'}
materials={'silver':'#c:ingots/silver','gold':'minecraft:gold_ingot','diamond':'minecraft:diamond','emerald':'minecraft:emerald','obsidian':'minecraft:obsidian','crystal':'minecraft:packed_ice','rainbow':'ironfurnaces:rainbow_plating'}
colors={'silver':(164,171,176),'gold':(178,137,52),'diamond':(60,153,171),'emerald':(59,112,79),'obsidian':(56,48,76),'crystal':(117,171,193),'rainbow':(120,107,168)}
texturepath=RES/'assets/revoindustry/textures/block/furnaces';texturepath.mkdir(parents=True,exist_ok=True)
mi=zipfile.ZipFile(ROOT/'.local/integration-mods/Modern-Industrialization-2.5.11.jar')
iron=zipfile.ZipFile(ROOT/'.local/integration-mods/ironfurnaces-neoforge-1.21.1-4.3.2.jar')
def dependency_sprite(jar,path):
 im=Image.open(io.BytesIO(jar.read(path))).convert('RGBA');return im.crop((0,0,16,16))
def mi_sprite(name):return dependency_sprite(mi,'assets/modern_industrialization/textures/block/overlays/'+name+'.png')
# Retain Iron Furnaces' door/opening pixels, while the underlying MI model supplies all casing material.
for active in [False,True]:
 name='controller_front'+('_on' if active else '')
 original=dependency_sprite(iron,'assets/ironfurnaces/textures/block/iron_furnace_front'+('_on' if active else '')+'.png')
 overlay=Image.new('RGBA',(16,16))
 for y in range(3,15):
  for x in range(2,14):
   r,g,b,a=original.getpixel((x,y));value=max(r,g,b)
   if value<115 or (r>g*1.25 and r>b*1.4):overlay.putpixel((x,y),(r,g,b,a))
 overlay.save(texturepath/(name+'.png'))
def plate(rgb):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 def shade(delta):return tuple(max(0,min(255,v+delta)) for v in rgb)+(255,)
 for y in range(16):
  for x in range(16):d.point((x,y),fill=shade(((x//3*3+y//3*7)%5-2)*2+((x*7+y*11)%5-2)))
 d.rectangle((0,0,15,15),outline=shade(-35));d.line((1,1,14,1),fill=shade(26));d.line((1,1,1,14),fill=shade(17));d.line((2,14,14,14),fill=shade(-20));d.line((14,2,14,14),fill=shade(-20))
 for x in [3,12]:
  for y in [3,12]:d.rectangle((x,y,x+1,y+1),fill=shade(-26));d.point((x,y),fill=shade(21))
 return im
manifest=[];registered=[]
for i,tier in enumerate(['copper','iron','silver','gold','diamond','emerald','obsidian','crystal','netherite','rainbow']):
 casing='modern_industrialization:'+native[tier] if tier in native else 'revoindustry:'+tier+'_furnace_casing'
 if tier not in native:
  name=tier+'_furnace_casing';registered.append(name)
  if tier=='rainbow':
   atlas=Image.new('RGBA',(16,16*24))
   for frame in range(24):
    im=plate((103,112,118));d=ImageDraw.Draw(im)
    for y in range(2,14):
     for x in range(2,14):
      rgb=tuple(round(v*210+15) for v in colorsys.hsv_to_rgb(((x+y)/24+frame/24)%1,.72,.76));d.point((x,y),fill=rgb+(255,))
    for x in [3,12]:
     for y in [3,12]:d.point((x,y),fill=(181,190,188,255));d.point((x+1,y+1),fill=(56,63,65,255))
    atlas.paste(im,(0,frame*16))
   atlas.save(texturepath/(tier+'_casing.png'));put(texturepath/(tier+'_casing.png.mcmeta'),{'animation':{'frametime':3,'interpolate':True}})
  else:plate(colors[tier]).save(texturepath/(tier+'_casing.png'))
  block(name,{'parent':'minecraft:block/cube_all','textures':{'all':f'revoindustry:block/furnaces/{tier}_casing'}})
  recipe(name,['MMM','MCM','MMM'],{'M':materials[tier],'C':'modern_industrialization:steel_machine_casing'},casing,4)
  lang['block.revoindustry.'+name]=tier.title()+' Furnace Casing'
 asset(f'models/machine_casing/{tier}_furnace.json',{'loader':'modern_industrialization:use_block_model','block':casing})
 furnace='million_furnace' if tier=='rainbow' else tier+'_furnace';name=tier+'_multiblock_furnace';registered.append(name)
 block(name,{'loader':'modern_industrialization:machine','casing':'revoindustry:'+tier+'_furnace','default_overlays':{'front':'revoindustry:block/furnaces/controller_front','front_active':'revoindustry:block/furnaces/controller_front_on'}})
 recipe(name,['CCC','CFC','CCC'],{'C':casing,'F':'ironfurnaces:'+furnace},'revoindustry:'+name)
 lang['block.revoindustry.'+name]=tier.title()+' Multiblock Furnace'
 manifest.append({'tier':tier,'controller':'revoindustry:'+name,'furnace':'ironfurnaces:'+furnace,'casing':casing,'casingKey':'revoindustry:'+tier+'_furnace','nativeCasing':tier in native,'hatchGrade':1 if tier in ['copper','iron'] else 2 if tier in ['silver','gold'] else 3,'lanes':2 if tier in ['copper','iron'] else 4 if tier in ['silver','gold'] else 6,'batch':64 if tier=='rainbow' else 1})
for grade in range(3):
 for direction,color in [('input',(188,151,60)),('output',(80,162,191))]:
  name=f'furnace_item_{direction}_hatch_{grade+1}';registered.append(name)
  # MI's own recess and extraction-frame sprites, composited at their original pixel positions.
  overlay=mi_sprite('output' if direction=='input' else 'output_item');overlay.alpha_composite(mi_sprite('item_auto'))
  overlay.save(texturepath/(name+'.png'))
  block(name,{'loader':'modern_industrialization:machine','casing':['modern_industrialization:bronze','modern_industrialization:steel','modern_industrialization:mv'][grade],'default_overlays':{'side':'revoindustry:block/furnaces/'+name,'output':'modern_industrialization:block/overlays/output_item','item_auto':'modern_industrialization:block/overlays/item_auto'}})
  if grade==0:recipe(name,['PPP','PHP','PPP'],{'P':'#c:plates/copper','H':f'modern_industrialization:bronze_item_{direction}_hatch'},'revoindustry:'+name)
  else:recipe(name,['PPP','PHP','PPP'],{'P':'#c:plates/'+['copper','steel','stainless_steel'][grade],'H':f'revoindustry:furnace_item_{direction}_hatch_{grade}'},'revoindustry:'+name)
  lang['block.revoindustry.'+name]=f'Furnace Item {direction.title()} Hatch {grade+1}'
for grade in range(3):
 for kind,color in [('solid',(211,134,55)),('liquid',(62,153,211)),('gas',(117,196,122))]:
  name=f'{kind}_fuel_hatch_{grade+1}';registered.append(name)
  overlay=mi_sprite('output_energy' if kind=='solid' else 'output_fluid');overlay.alpha_composite(mi_sprite('item_auto' if kind=='solid' else 'fluid_auto'))
  overlay.save(texturepath/(name+'.png'))
  default=['modern_industrialization:bronze','modern_industrialization:steel','modern_industrialization:mv'][grade]
  block(name,{'loader':'modern_industrialization:machine','casing':default,'default_overlays':{'side':'revoindustry:block/furnaces/'+name}})
  if grade==0:
   base='modern_industrialization:bronze_item_input_hatch' if kind=='solid' else 'modern_industrialization:bronze_fluid_input_hatch'
   recipe(name,['PPP','PHP','PBP'],{'P':'#c:plates/copper','H':base,'B':{'solid':'minecraft:furnace','liquid':'minecraft:bucket','gas':'minecraft:glass_bottle'}[kind]},'revoindustry:'+name)
  else:recipe(name,['PPP','PHP','PPP'],{'P':'#c:plates/'+['copper','steel','stainless_steel'][grade],'H':f'revoindustry:{kind}_fuel_hatch_{grade}'},'revoindustry:'+name)
  lang['block.revoindustry.'+name]=f'{kind.title()} Fuel Input Hatch {grade+1}'
name='furnace_vent';registered.append(name);overlay=Image.new('RGBA',(16,16));d=ImageDraw.Draw(overlay);d.rectangle((3,3,12,12),fill=(29,34,33,255),outline=(108,121,116,255))
for y in range(5,12,2):d.line((5,y,10,y),fill=(97,106,101,255))
overlay.save(texturepath/(name+'.png'))
block(name,{'loader':'modern_industrialization:machine','casing':'modern_industrialization:steel','default_overlays':{'top':'revoindustry:block/furnaces/furnace_vent'}})
recipe(name,['PPP','PBP','PPP'],{'P':'#c:plates/iron','B':'minecraft:iron_bars'},'revoindustry:'+name)
lang.update({'block.revoindustry.furnace_vent':'Furnace Exhaust Vent','hatch.revoindustry.furnace_fuel':'Solid, Liquid or Gas Fuel Input','hatch.revoindustry.furnace_vent':'Roof-centre Exhaust Vent','emi.category.revoindustry.furnace_assembly':'RevoIndustry Furnace Assembly'})
put(langpath,lang)
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';j=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {'replace':False,'values':[]};j['values']=list(dict.fromkeys(j['values']+['revoindustry:'+n for n in registered]));put(p,j)
# Invalidates the crafting route in all pack worlds; the mixin also disables pre-existing installed Factory Augments.
put(RES/'data/ironfurnaces/recipe/augments/augment_factory.json',{'neoforge:conditions':[{'type':'neoforge:false'}],'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:bedrock'}],'result':{'id':'ironfurnaces:augment_factory'}})
put(ART/'manifest.json',{'tiers':manifest,'shape':[3,3,3],'minimumCasings':21,'minimumPorts':{'itemInput':1,'itemOutput':1,'fuelInput':1},'roofVent':[0,2,1],'hollowCentre':[0,1,1],'fuelRates':[64,512,4096],'fuelTankCapacities':[4000,16000,64000],'factoryAugmentDisabled':True,'registeredBlocks':registered})
print('Generated',len(manifest),'furnace tiers,',len(registered),'blocks and recipes, native casing aliases and hatch overlays.')
