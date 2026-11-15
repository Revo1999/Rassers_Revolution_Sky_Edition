"""Validate expansion quests, models and recipe assets without launching the game."""
from pathlib import Path
import json,math
ROOT=Path(__file__).resolve().parents[1]
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
ids=set();quest_ids=set();dependencies=[]
for p in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt'):
 chapter=read(p)
 for q in chapter.get('quests',[]):
  quest_ids.add(q['id']);dependencies.extend(q.get('dependencies',[]))
  for obj in [q]+q.get('tasks',[])+q.get('rewards',[]):
   ident=obj['id'];assert len(ident)==16 and int(ident,16)>0,ident;assert ident not in ids,ident;ids.add(ident)
assert set(dependencies)<=quest_ids,set(dependencies)-quest_ids
chapter=read(ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e50000000000000.snbt');assert len(chapter['quests'])==20
for mod,name in [('revostorage','chest_drive'),('revostorage','crafting_terminal'),('revoextraction','electric_drill'),('revocultivation','trading_colony')]:
 base=ROOT/mod/'src/main/resources';model=read(base/f'assets/{mod}/models/block/{name}.json');assert len(model['elements'])<=20
 for el in model['elements']:
  dims=[b-a for a,b in zip(el['from'],el['to'])]
  for side,face in el['faces'].items():
   u,v,U,V=face['uv'];w,h=(dims[0],dims[2]) if side in ['up','down'] else (dims[2],dims[1]) if side in ['east','west'] else (dims[0],dims[1]);assert abs((U-u)*4-w)<.001 and abs((V-v)*4-h)<.001,(name,el['name'],side);assert all(math.isfinite(n) and 0<=n<=16 for n in face['uv'])
 assert (base/f'assets/{mod}/textures/block/{name}.png').exists();assert (base/f'data/{mod}/recipe/{name}.json').exists();assert (base/f'data/{mod}/loot_table/blocks/{name}.json').exists()
print('PASS: unique quest/task/reward IDs, all quest dependencies, 20 expansion quests including salt geology, native model UV density, recipes, textures and loot.')
