"""Validate the chapter DAG, resource bootstrap, island and native primitive assets."""
from pathlib import Path
import sys,json,base64,math
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'.local/python-deps'))
import nbtlib
from PIL import Image
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def main():
 cfg=ROOT/'pack/overrides/config'
 chapter=read(cfg/'ftbquests/quests/chapters/5e10000000000000.snbt');tips=read(cfg/'ftbquests/quests/chapters/5e1f000000000000.snbt')
 assert len(chapter['quests'])==35 and len(tips['quests'])==6
 allids=[];questids={q['id'] for q in chapter['quests']};seen=set()
 for q in chapter['quests']:
  assert all(d in seen for d in q.get('dependencies',[])),q['title']
  seen.add(q['id']);allids.append(q['id'])
  for t in q['tasks']:
   allids.append(t['id'])
   if t['type']=='item':assert t['consume_items'] is False and t['count']>0
  for r in q.get('rewards',[]):allids.append(r['id'])
 for q in tips['quests']:
  assert not q.get('dependencies') and q['can_repeat']=='enabled'
  allids += [q['id']]+[t['id'] for t in q['tasks']]+[r['id'] for r in q['rewards']]
 assert len(allids)==len(set(allids)) and all(len(i)==16 and int(i,16)>0 for i in allids)
 storm=[r for q in chapter['quests'] for r in q.get('rewards',[]) if r['type']=='command']
 assert len(storm)==1 and storm[0]['command']=='/weather rain 300s' and storm[0]['permission_level']==2 and storm[0]['auto']=='disabled'
 inv=read(cfg/'skyblockbuilder/starter_inventory.json5')
 assert inv=={'items':[{'Slot':'mainhand','Item':{'id':'revoindustry:revolution_book','count':1}}]}
 assert read(cfg/'skyblockbuilder/inventory.json5')['clearInitialInventory'] is True
 island=nbtlib.load(cfg/'skyblockbuilder/templates/islands/revo_start.nbt')
 palette=island['palette'];blocks={tuple(int(v) for v in b['pos']):palette[int(b['state'])]['Name'] for b in island['blocks']}
 assert str(blocks[9,5,6])=='craftingstation:crafting_station' and (9,5,7) not in blocks
 assert not any('lava' in str(v) or str(v)=='minecraft:chest' for v in blocks.values())
 # Reachability uses actual pack crafting/smelting recipes plus explicit environmental processes.
 available={'minecraft:oak_log','minecraft:oak_planks','minecraft:stick','minecraft:oak_slab'}
 tags={'minecraft:logs':'minecraft:oak_log','minecraft:planks':'minecraft:oak_planks','minecraft:wooden_slabs':'minecraft:oak_slab'}
 recipes=[]
 for mod in ['revoextraction','revocultivation','revoindustry']:
  recipes += [read(p) for p in (ROOT/mod/'src/main/resources/data'/mod/'recipe').glob('*.json')]
 def ingredient(i):return i.get('item') in available or tags.get(i.get('tag')) in available
 changed=True
 while changed:
  before=set(available)
  if 'revocultivation:crook' in available:available.update(['minecraft:oak_leaves','minecraft:oak_sapling','minecraft:apple'])
  if {'revocultivation:composter','minecraft:oak_leaves'}<=available:available.add('revocultivation:compost')
  if any(h in available for h in ['revoextraction:wooden_hammer','revoextraction:stone_hammer']):
   for a,b in [('minecraft:dirt','revoextraction:stone_pebble'),('minecraft:cobblestone','minecraft:gravel'),('minecraft:gravel','minecraft:sand'),('minecraft:sand','revoextraction:dust')]:
    if a in available:available.add(b)
  if 'minecraft:cobblestone' in available:available.add('minecraft:furnace')
  if 'minecraft:furnace' in available:available.update(['minecraft:charcoal','minecraft:torch'])
  if 'revoindustry:rain_barrel' in available:available.add('water')
  if {'revoindustry:rain_barrel','revoextraction:dust','water'}<=available:available.add('minecraft:clay')
  if {'revoindustry:clay_crucible','minecraft:torch','minecraft:cobblestone'}<=available:available.add('lava')
  if 'revoindustry:clay_bucket' in available:
   if 'water' in available:available.add('revoindustry:clay_water_bucket')
   if 'lava' in available:available.add('revoindustry:clay_lava_bucket')
  for r in recipes:
   kind=r.get('type','');ok=False
   if kind=='minecraft:crafting_shaped':ok=all(ingredient(i) for i in r['key'].values())
   elif kind=='minecraft:crafting_shapeless':ok=all(ingredient(i) for i in r['ingredients'])
   elif kind=='minecraft:smelting':ok='minecraft:furnace' in available and ingredient(r['ingredient'])
   if ok:available.add(r['result']['id'])
  changed=before!=available
 required={'revocultivation:crook','revocultivation:composter','revoextraction:stone_hammer','revoextraction:dust','revoindustry:rain_barrel','revoindustry:clay_crucible','revoindustry:clay_bucket','revoindustry:clay_lava_bucket','create:hand_crank','create:shaft','revoextraction:wooden_drill_controller','revoextraction:wooden_drill_core','revoextraction:wooden_drill_support','revoextraction:wooden_drill_output_hatch','revoextraction:wooden_drill_input'}
 assert required<=available,required-available
 art=ROOT/'art/blockbench/chapter-one';res=ROOT/'revoindustry/src/main/resources/assets/revoindustry';atlas=art/'primitive_atlas.png'
 assert Image.open(atlas).size==(128,128) and atlas.read_bytes()==(res/'textures/block/primitive_atlas.png').read_bytes()
 for name,count in [('rain_barrel',5),('unfired_crucible',9),('clay_crucible',9)]:
  src=read(art/(name+'.bbmodel'));runtime=read(res/('models/block/'+name+'.json'));assert len(src['elements'])==len(runtime['elements'])==count
  assert base64.b64decode(src['textures'][0]['source'].split(',')[1])==atlas.read_bytes()
  for e in src['elements']:
   d=[b-a for a,b in zip(e['from'],e['to'])];assert all(0<=v<=16 and math.isfinite(v) for v in e['from']+e['to'])
   for face,f in e['faces'].items():
    u,v,U,V=f['uv'];wh=(d[0],d[2]) if face in ['up','down'] else (d[2],d[1]) if face in ['east','west'] else (d[0],d[1])
    assert abs(U-u-wh[0])<.001 and abs(V-v-wh[1])<.001
 report={'result':'passed','firstChapterQuests':35,'ungatedTips':6,'questIdsUnique':True,'dependencyGraphAcyclic':True,'renewableBootstrapReachable':sorted(required),'startingItems':['revoindustry:revolution_book'],'placedCraftingStation':True,'starterChestAndFreeLava':False,'nativeVesselCubes':[5,9,9]}
 (ROOT/'art/quests/content-validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print('PASS: 35-quest DAG, six ungated Tips entries, wood-to-drill/lava bootstrap, one-book start, station island and native vessel assets.')
if __name__=='__main__':main()

