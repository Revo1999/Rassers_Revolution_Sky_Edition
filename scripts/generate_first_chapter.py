"""First chapter resources, recipes, stable FTB quests and ungated guide access."""
from pathlib import Path
import json,shutil
from quest_presentation import first_chapter
ROOT=Path(__file__).resolve().parents[1]
def put(path,obj):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
def res(mod,path,obj):put(f'{mod}/src/main/resources/{path}',obj)
def ingredient(s):return {'tag':s[1:]} if s.startswith('#') else {'item':s}
def shaped(mod,name,pattern,key,out=None,count=1):
 res(mod,f'data/{mod}/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:ingredient(v) for k,v in key.items()},'result':{'id':out or f'{mod}:{name}','count':count}})
def shapeless(mod,name,inputs,out,count=1):
 res(mod,f'data/{mod}/recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[ingredient(i) for i in inputs],'result':{'id':out,'count':count}})
def smelt(name,source,out):
 res('revoindustry',f'data/revoindustry/recipe/{name}.json',{'type':'minecraft:smelting','category':'misc','ingredient':{'item':source},'result':{'id':out},'experience':.1,'cookingtime':200})
shaped('revocultivation','crook',['SS ',' S ',' S '],{'S':'minecraft:stick'})
shaped('revocultivation','composter',['L L','P P','PPP'],{'L':'#minecraft:logs','P':'#minecraft:planks'})
for tier,mat in [('wooden','#minecraft:planks'),('stone','minecraft:cobblestone')]:
 shaped('revoextraction',tier+'_hammer',['MMM',' S ',' S '],{'M':mat,'S':'minecraft:stick'})
shaped('revoextraction','cobblestone_from_pebbles',['PP','PP'],{'P':'revoextraction:stone_pebble'},'minecraft:cobblestone')
shaped('revoextraction','starter_shaft',[' S ',' C ',' S '],{'S':'minecraft:stick','C':'minecraft:cobblestone'},'create:shaft',2)
shaped('revoextraction','starter_hand_crank',['PPP','  S'],{'P':'#minecraft:planks','S':'create:shaft'},'create:hand_crank')
shaped('revoindustry','rain_barrel',['P P','P P','SSS'],{'P':'#minecraft:planks','S':'#minecraft:wooden_slabs'})
shaped('revoindustry','unfired_crucible',['C C','C C','CCC'],{'C':'minecraft:clay_ball'})
shaped('revoindustry','unfired_clay_bucket',['C C',' C '],{'C':'minecraft:clay_ball'})
smelt('fire_crucible','revoindustry:unfired_crucible','revoindustry:clay_crucible')
smelt('fire_bucket','revoindustry:unfired_clay_bucket','revoindustry:clay_bucket')
shapeless('revoindustry','clay_balls_from_clay',['minecraft:clay'],'minecraft:clay_ball',4)
shapeless('revoindustry','revolution_book',['#minecraft:planks','minecraft:stick'],'revoindustry:revolution_book')
for mod,names in [('revoextraction',['wooden_hammer','stone_hammer','stone_pebble']),('revocultivation',['crook']),('revoindustry',['unfired_clay_bucket','clay_bucket','clay_water_bucket','clay_lava_bucket'])]:
 for name in names:res(mod,f'assets/{mod}/models/item/{name}.json',{'parent':'minecraft:item/handheld' if 'hammer' in name or name=='crook' else 'minecraft:item/generated','textures':{'layer0':f'{mod}:item/{name}'}})
res('revoindustry','assets/revoindustry/models/item/revolution_book.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/book'}})
res('revoextraction','assets/revoextraction/blockstates/dust.json',{'variants':{'':{'model':'revoextraction:block/dust'}}})
res('revoextraction','assets/revoextraction/models/block/dust.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/light_gray_concrete_powder'}})
res('revoextraction','assets/revoextraction/models/item/dust.json',{'parent':'revoextraction:block/dust'})
for mod,name in [('revoextraction','dust'),('revoindustry','rain_barrel'),('revoindustry','clay_crucible'),('revoindustry','unfired_crucible')]:
 if mod=='revoindustry':
  res(mod,f'assets/{mod}/models/item/{name}.json',{'parent':f'{mod}:block/{name}'})
  res(mod,f'assets/{mod}/blockstates/{name}.json',{'variants':{'':{'model':f'{mod}:block/{name}'}} if name=='unfired_crucible' else {'variants':{}}})
  if name!='unfired_crucible':res(mod,f'assets/{mod}/blockstates/{name}.json',{'variants':{'facing='+f:{'model':f'{mod}:block/{name}','y':r} for f,r in [('north',0),('east',90),('south',180),('west',270)]}})
 res(mod,f'data/{mod}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{mod}:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
res('revoextraction','data/c/tags/item/dusts/stone.json',{'replace':False,'values':['revoextraction:dust']})
res('revoextraction','data/c/tags/item/dusts.json',{'replace':False,'values':['revoextraction:dust']})
res('revocultivation','data/c/tags/item/crooks.json',{'replace':False,'values':['revocultivation:crook']})
for tag,names in [('empty',['clay_bucket']),('water',['clay_water_bucket']),('lava',['clay_lava_bucket'])]:res('revoindustry',f'data/c/tags/item/buckets/{tag}.json',{'replace':False,'values':['revoindustry:'+n for n in names]})
res('revoindustry','data/minecraft/tags/block/mineable/axe.json',{'replace':False,'values':['revoindustry:rain_barrel']})
res('revoindustry','data/minecraft/tags/block/mineable/pickaxe.json',{'replace':False,'values':['revoindustry:clay_crucible','revoindustry:unfired_crucible']})
res('revoextraction','data/minecraft/tags/block/mineable/shovel.json',{'replace':False,'values':['revoextraction:dust']})
lang={
'revoindustry':{'block.revoindustry.rain_barrel':'Rain Barrel','block.revoindustry.clay_crucible':'Clay Crucible','block.revoindustry.unfired_crucible':'Unfired Clay Crucible','item.revoindustry.unfired_clay_bucket':'Unfired Clay Bucket','item.revoindustry.clay_bucket':'Clay Bucket','item.revoindustry.clay_water_bucket':'Clay Water Bucket','item.revoindustry.clay_lava_bucket':'Clay Lava Bucket','item.revoindustry.revolution_book':'Revolution Book','state.revoindustry.0':'Working','state.revoindustry.1':'Waiting for rain','state.revoindustry.2':'Needs open sky','state.revoindustry.3':'Needs cobblestone','state.revoindustry.4':'Needs heat below','state.revoindustry.5':'Fluid ready / tank full','emi.category.revoindustry.rain_barrel':'Rain and Clay','emi.category.revoindustry.lava_crucible':'Lava Crucible','recipe.revoindustry.0':'Rain: 1000 mB in 10s','recipe.revoindustry.1':'Dust + water = clay','recipe.revoindustry.2':'4 cobble = 1000 mB lava','recipe.revoindustry.heat':'40s torch / 20s campfire','recipe.revoindustry.sky':'Barrel needs open sky','recipe.revoindustry.bucket':'Use clay or iron buckets'},
'revoextraction':{'item.revoextraction.wooden_hammer':'Wooden Hammer','item.revoextraction.stone_hammer':'Stone Hammer','item.revoextraction.stone_pebble':'Stone Pebble','block.revoextraction.dust':'Stone Dust','emi.category.revoextraction.hammering':'Hammering','recipe.revoextraction.hammer':'Wood or stone hammer','recipe.revoextraction.hammer_use':'Mine or right-click block'},
'revocultivation':{'item.revocultivation.crook':'Wooden Crook'}}
for mod,entries in lang.items():
 p=ROOT/mod/'src/main/resources/assets'/mod/'lang/en_us.json';data=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {};data.update(entries);res(mod,f'assets/{mod}/lang/en_us.json',data)

# Stable filenames prevent FTB's editor from importing another duplicate on restart.
chapter_dir=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
old=chapter_dir/'revo_cosmetics.snbt'
if old.exists():
 data=json.loads(old.read_text(encoding='utf-8-sig'));data['filename']='5e70000000000000';put('pack/overrides/config/ftbquests/quests/chapters/5e70000000000000.snbt',data)
 archive=ROOT/'art/quests/history';archive.mkdir(parents=True,exist_ok=True);shutil.copy2(old,archive/old.name);old.unlink()
BASE=0x5E10000000000000
def hid(n):return f'{BASE+n:016X}'
quests=[];ids={}
def quest(key,title,item=None,count=1,deps=(),x=0,y=0,text=(),rewards=()):
 i=len(quests)+1;ids[key]=hid(i)
 task={'id':hid(0x100+i),'type':'item','item':{'id':item,'count':1},'count':count,'consume_items':False} if item else {'id':hid(0x100+i),'type':'checkmark'}
 q={'id':ids[key],'title':title,'x':float(x),'y':float(y),'description':list(text),'tasks':[task],'rewards':list(rewards)}
 if deps:q['dependencies']=[ids[d] for d in deps]
 quests.append(q);return q
quest('welcome','From Nothing',x=0,y=0,text=['One oak, a crafting station and the Revolution Book. Build everything else yourself.','Right-click this book to open FTB Quests. Your other mod manuals are freely available in Tips & Guides.','The island has no free ice or lava. Preserve the island and grow a renewable supply of trees.'])
quest('station','Your First Workshop',deps=['welcome'],x=0,y=-2,text=['A Crafting Station is already placed on the island. It keeps its grid when you close it.','Use it for the recipes below. This checkmark does not require breaking your station.'])
quest('logs','A Handful of Wood','minecraft:oak_log',4,['welcome'],2,0,['Punch the starting oak logs. Save the leaves until you have a crook.','Plant replacement saplings on dirt and give them space and daylight.'])
quest('sticks','Stick With It','minecraft:stick',8,['logs'],4,0,['Turn logs into planks, then make sticks.','These are the handles for your crook and first wooden hammer.'])
quest('crook','A Hook in the Sky','revocultivation:crook',1,['sticks'],6,0,['Craft the Wooden Crook using four sticks. Break leaves with it.','It collects leaf blocks, supplies replacement saplings regularly and helps oak trees provide apples.'])
quest('leaves','A Renewable Pile','minecraft:oak_leaves',16,['crook'],8,0,['Harvest leaves using the crook, and keep replanting saplings.','Leaf blocks are your early source of compost, dirt and silkworm feed.'])
quest('sapling','Never Lose Your Last Tree','minecraft:oak_sapling',1,['crook'],6,2,['Keep a spare sapling. Plant before harvesting the next tree.','The crook supplies replacement saplings while collecting leaf blocks.'])
quest('composter','A Wooden Composter','revocultivation:composter',1,['leaves'],10,0,['The Composter recipe is entirely wood: logs and planks.','Insert four matching leaves or other compostable plants. A full batch automatically runs for 8 seconds. No crank, power or tamping.','Right-click held inputs to fill it, or use its interface. Watch the contents rise.'])
quest('compost','Let Nature Work','revocultivation:compost',1,['composter'],12,0,['Four compostable inputs produce one Compost after 8 seconds.','The result stays visible until taken or auto-ejected into an inventory in front.'])
quest('dirt','Grow Your Own Ground','minecraft:dirt',4,['compost'],14,0,['Craft each Compost into one Dirt. Keep making this renewably from leaves.','You can also use Compost as bone meal, but save enough for the stone loop.'])
quest('woodhammer','The Bootstrap Hammer','revoextraction:wooden_hammer',1,['sticks'],6,4,['Make a Wooden Hammer from planks and sticks. It is the stone-free starting tool.','The Stone Hammer does the same conversions and lasts longer once you can afford cobblestone.'])
quest('pebbles','Stone From Soil','revoextraction:stone_pebble',4,['dirt','woodhammer'],16,0,['Place Dirt and hammer it with either hammer. One dirt gives four Stone Pebbles.','Mining with the hammer or right-clicking the block both work. You never need to destroy your island to bootstrap stone.'])
quest('cobble','Four Pebbles, One Block','minecraft:cobblestone',1,['pebbles'],18,0,['Craft four Stone Pebbles in a 2 by 2 square into one Cobblestone.','Repeat leaves -> compost -> dirt -> pebbles -> cobblestone whenever you need more.'])
quest('stonehammer','A Better Hammer','revoextraction:stone_hammer',1,['cobble'],20,0,['Craft a Stone Hammer with three cobblestone and two sticks.','It works on dirt too. Your wooden hammer stays useful as a backup.'])
quest('gravel','Break It Down','minecraft:gravel',1,['stonehammer'],22,0,['Hammer a placed cobblestone block into gravel.','The hammer replaces its normal drop rather than giving both.'])
quest('sand','Finer Still','minecraft:sand',1,['gravel'],24,0,['Hammer gravel into sand.'])
quest('dust','The Finest Stone','revoextraction:dust',3,['sand'],26,0,['Hammer sand into Stone Dust. This is the last step: cobblestone -> gravel -> sand -> dust.','Make at least three dust for your first clay crucible and bucket.'])
rain_reward={'id':hid(0x500),'type':'command','command':'/weather rain 300','permission_level':2,'silent':True,'auto':'enabled'}
quest('barrel','Catch a Cloud','revoindustry:rain_barrel',1,['leaves'],10,-3,['Make a Rain Barrel from planks and wooden slabs, then place it with clear sky above.','Finishing this quest starts a five-minute shower, so your opening clay loop does not depend on waiting for random weather.','During rain it collects one bucket of water in 10 seconds. Keep the top uncovered.'],[rain_reward])
quest('clay','Dust Meets Rain','minecraft:clay',1,['dust','barrel'],28,-3,['Right-click one Stone Dust into a full Rain Barrel. 1000 mB water and one dust make one Clay block.','Take the clay from the barrel interface. Item/fluid automation and front item auto-ejection also work.'])
quest('clayballs','Soft Foundations','minecraft:clay_ball',10,['clay'],30,-3,['Break clay blocks or craft them back into four clay balls.','You need seven balls for the crucible and three for a clay bucket.'])
quest('furnace','A Small Kiln','minecraft:furnace',1,['cobble'],20,3,['Eight cobblestone make a furnace. Logs or charcoal can fire your clay items.','Smelt logs into charcoal if you need renewable fuel or more torches.'])
quest('rawcrucible','Shape the Crucible','revoindustry:unfired_crucible',1,['clayballs'],32,-3,['Arrange seven clay balls in a U shape. The unfired block cannot make lava yet.'])
quest('crucible','Fire It First','revoindustry:clay_crucible',1,['rawcrucible','furnace'],34,-3,['Smelt the Unfired Clay Crucible in a furnace.','Place the fired crucible above a heat source and insert cobblestone.'])
quest('bucket','A Bucket Before Iron','revoindustry:clay_bucket',1,['clayballs','furnace'],32,-5,['Craft three clay balls into an Unfired Clay Bucket and smelt it.','Clay buckets collect water or lava from vessels and world sources. They never turn into free iron buckets.','Water pouring returns the empty clay bucket. Pouring lava breaks the clay bucket, so place your water first.'])
quest('heat','A Little Warmth',deps=['crucible'],x=36,y=-3,text=['Put a torch beneath the fired crucible. There is already one on the starting island, or craft more with charcoal.','Four cobblestone melt into 1000 mB lava: 40 seconds above a torch, 20 above a lit campfire. Unlit campfires do not heat it.','The machine works while loaded and pauses if heat or input is missing.'])
quest('water','Save Some Water','revoindustry:clay_water_bucket',1,['bucket','barrel'],34,-5,['Use an empty Clay Bucket on a full Rain Barrel to collect water.','Reserve water for your generator before spending every refill on clay.'])
quest('lava','Your First Lava','revoindustry:clay_lava_bucket',1,['heat','bucket'],38,-3,['Insert four cobblestone into the heated crucible and let the tank reach 1000 mB.','Collect it with an empty Clay Bucket. Lava is earned through your clay and heat chain, not supplied in the starter chest.'])
quest('generator','Stone on Demand',deps=['water','lava'],x=40,y=-3,text=['Build a normal water-and-lava cobblestone generator if you want a backup to composted dirt.','Place water first, reuse the empty clay bucket to collect lava, then place lava. Keep the lava source safe; flowing lava should meet flowing water in the cobble gap.','The lava bucket breaks when poured. These fluids otherwise use normal Minecraft behavior.'])
quest('shaft','A Primitive Drive','create:shaft',2,['cobble'],22,5,['Use the pack starter recipe: a cobblestone and two sticks make two Create shafts.','This avoids needing metal before your first drill. Later Create recipes remain available.'])
quest('crank','Turn It Yourself','create:hand_crank',1,['shaft'],24,5,['The starter crank recipe uses planks and a shaft.','This crank powers the wooden drill. The Composter and Silk Worm Cultivator remain passive.'])
quest('cores','The Heart of the Drill','revoextraction:wooden_drill_core',2,['cobble'],26,5,['Make two drill cores. One is used to craft the controller, and one is placed inside the multiblock.'])
quest('supports','Twelve Timber Supports','revoextraction:wooden_drill_support',12,['cobble'],28,7,['The four corners each stand three blocks tall: twelve supports total.','Use renewable cobblestone for their feet and braces.'])
quest('controller','The First Controller','revoextraction:wooden_drill_controller',1,['cores'],28,5,['Craft the Wooden Drill Controller using one core and wooden casings.','Open the drill in EMI and use its interactive multiblock construction guide.'])
quest('parts','Finish the Shell',deps=['controller'],x=30,y=5,text=['For the assembled 3 x 3 x 3 drill you need 12 timber supports, 10 casings, 1 placed core, 1 controller, 1 kinetic input and 1 output hatch.','Some casings are also consumed when crafting the controller, input and hatch. EMI shows each recipe.'])
# Actual inventory tasks on the parts node, not just a self-report.
quests[-1]['tasks']=[{'id':hid(0x300+i),'type':'item','item':{'id':item,'count':1},'count':count,'consume_items':False} for i,(item,count) in enumerate([('revoextraction:wooden_drill_casing',10),('revoextraction:wooden_drill_input',1),('revoextraction:wooden_drill_output_hatch',1),('revoextraction:wooden_drill_core',1)])]
quest('drill','From Dirt to Industry',deps=['supports','parts','crank'],x=32,y=5,text=['Follow EMI to assemble the drill, attach the hand crank to its kinetic input, and turn it.','Check the controller for formation, RPM and cycle status. Open the output hatch for its inventory, or put a chest against its output face.','Confirm this checkmark once your first drill works. The rest of the pack starts with the resources you can now renew.'])
images=first_chapter(quests,ids)
put('pack/overrides/config/ftbquests/quests/chapters/5e10000000000000.snbt',{'id':hid(0),'filename':'5e10000000000000','title':'Chapter 1: From Nothing','icon':{'id':'revoindustry:revolution_book','count':1},'order_index':0,'default_quest_shape':'circle','images':images,'quests':quests})
tips=[];T=0x5E1F000000000000
for i,(title,item,text) in enumerate([
 ('The Revolution Book','revoindustry:revolution_book',['Your pack quest menu. Claim a replacement here or craft one from a plank and a stick.']),
 ('Modern Industrialization','modern_industrialization:guidebook',['The official MI guidebook. No unlock guard is applied yet.']),
 ('Applied Energistics 2 and Addons','ae2:guide',['The AE2 guide also contains installed guide pages for AdvancedAE, ExtendedAE, Wireless Terminals, AE2 Helpers, MEGA Cells and Applied Create.','These addons share the AE2 guide instead of shipping separate physical manuals. No unlock guard is applied yet.']),
 ('FTB Quests Handbook','ftbquests:book',['The standard FTB quest book opens the same quests as your Revolution Book.']),
 ('Create Ponder',None,['Create uses Ponder rather than a separate physical manual. Hold its Ponder key on supported Create items in EMI or your inventory.','For Revo multiblocks, open their assembly recipe in EMI and use the 3D build guide.']),
 ('Building and Utility Mods',None,['Macaw blocks, Create Deco, BetterBlockz, Simply Light and the other utility mods have their recipes in EMI and information in item tooltips.','No fake book items are supplied for mods that do not ship manuals.'])
 ]):
 n=i+1
 if item:text=list(text)+['Free quest manuals are knowledge-only: recipes that consume them for crafting or recycling are removed. Normal books must still be obtained through progression.']
 tips.append({'id':f'{T+n:016X}','title':title,'x':float(i%3*3),'y':float(i//3*3),'description':text,'dependencies':[],'can_repeat':'enabled','repeat_cooldown':0,'tasks':[{'id':f'{T+0x100+n:016X}','type':'checkmark'}],'rewards':[{'id':f'{T+0x200+n:016X}','type':'item','item':{'id':item,'count':1},'auto':'disabled'}] if item else []})
put('pack/overrides/config/ftbquests/quests/chapters/5e1f000000000000.snbt',{'id':f'{T:016X}','filename':'5e1f000000000000','title':'Tips & Guides','icon':{'id':'minecraft:knowledge_book','count':1},'order_index':90,'quests':tips})
put('revolib/src/main/resources/data/revolib/tags/item/free_quest_manuals.json',{'replace':False,'values':[{'id':item,'required':False} for item in sorted({r['item']['id'] for q in tips for r in q.get('rewards',[]) if r['type']=='item'})]})
put('pack/overrides/config/ftbquests/quests/data.snbt',{'version':13,'default_consume_items':False,'default_reward_team':False,'default_autoclaim_rewards':'disabled','pause_game':False,'detection_delay':20,'progression_mode':'linear','fallback_locale':'en_us','grid_scale':.5})
put('pack/overrides/config/ftbquests/quests/chapter_groups.snbt',{'chapter_groups':[]})
put('art/quests/chapter-one-map.json',{'chapter':'5E10000000000000','quests':ids,'questCount':len(quests),'tips':len(tips),'guideItems':['revoindustry:revolution_book','modern_industrialization:guidebook','ae2:guide','ftbquests:book']})
print(f'Generated {len(quests)} first-chapter quests, {len(tips)} ungated Tips entries and progression recipes.')

