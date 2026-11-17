"""Audit actual Revo block registrations against loaded and packaged recipe outputs.

Run the Nexus native game tests first to refresh the registry/recipe-manager dump.
This writes a report only; it never creates or modifies recipes.
"""
from pathlib import Path
from collections import defaultdict
import json, zipfile

ROOT=Path(__file__).resolve().parents[1]

def main():
 runtime=json.loads((ROOT/'.local/nexus-runtime-registry-audit.json').read_text())
 outputs=defaultdict(list)
 for r in runtime['recipes']:outputs[r['result']].append({'id':r['id'],'type':r['type'],'source':'native recipe manager'})
 # HammerItem.result is a real Java interaction, so it has no RecipeManager entry.
 outputs['revoextraction:dust'].append({'id':'sand_to_dust','type':'revoextraction:manual_hammering','source':'revoextraction/src/main/java/dk/revo/revoextraction/HammerItem.java'})
 def recipe(source,key,data):
  found=[]
  for field in ['result','results','item_outputs']:
   values=data.get(field,[]);values=values if isinstance(values,list) else [values]
   for value in values:
    if isinstance(value,str):found.append(value)
    elif isinstance(value,dict):
     item=value.get('id',value.get('item'))
     if isinstance(item,str):found.append(item)
  for item in found:outputs[item].append({'id':key,'type':data.get('type','unknown'),'source':source,'conditions':data.get('neoforge:conditions',[])})
 for p in ROOT.glob('revo*/src/main/resources/data/*/recipe/**/*.json'):
  root=p.parts.index('data');key=p.parts[root+1]+':'+Path(*p.parts[root+3:]).with_suffix('').as_posix()
  recipe(p.relative_to(ROOT).as_posix(),key,json.loads(p.read_text()))
 for jar in (ROOT/'revolib/run/mods').glob('*.jar'):
  with zipfile.ZipFile(jar) as z:
   for name in z.namelist():
    parts=name.split('/')
    if len(parts)>3 and parts[0]=='data' and parts[2]=='recipe' and name.endswith('.json'):
     recipe(jar.name,parts[1]+':'+'/'.join(parts[3:])[:-5],json.loads(z.read(name)))
 manifest=json.loads((ROOT/'revoindustry/src/main/resources/nexus/structures.json').read_text())
 uses=defaultdict(lambda:defaultdict(int))
 for kind,bp in manifest['structures'].items():
  for cell in bp['cells']:uses[cell['block']][kind]+=1
 rows=[]
 for block in sorted(runtime['blocks'],key=lambda b:b['id']):
  item=block.get('item',block['id']);routes=outputs.get(item,[]) if block['hasItem'] else []
  rows.append({**block,'routes':routes,'machines':dict(uses[block['id']]),'status':'no item by design' if not block['hasItem'] else 'recipe present' if routes else 'no recipe'})
 missing=[r for r in rows if r['status']=='no recipe' and r['id'] in manifest['blocks']]
 native=[]
 for block,used in sorted(uses.items()):
  if block.startswith('modern_industrialization:'):native.append({'id':block,'machines':dict(used),'recipes':outputs.get(block,[])})
 report={'scope':'All runtime-registered Revo blocks; native recipe manager supplemented by packaged recipes for optional integration conditions. Loot tables are not crafting routes.',
         'registeredBlocks':len(rows),'missingNexusBlockRecipes':len(missing),'blocks':rows,'nativeNexusHatches':native}
 (ROOT/'docs/machine-recipe-audit.json').write_text(json.dumps(report,indent=2)+'\n')
 names={'pressure_casting_plant':'Casting Plant','electroplating_line':'Plating Line','particle_collider':'Collider','nexus_portal':'Portal','nexus_builder':'Builder'}
 lines=['# Missing machine-block recipes','',
  'Audit of all '+str(len(rows))+' registered Revo blocks, using the native recipe manager and packaged recipe JSON. Block drops and loot tables do not count as recipes. Optional-integration recipes are checked in the source even when their dependency is absent from the test server. The complete inventory is in [machine-recipe-audit.json](docs/machine-recipe-audit.json).','',
  '**No missing block recipes were added.** The explicitly requested eleven mob-egg recipes and eleven typed vanilla-spawner recipes are the exception. Eggs and placed spawners cannot spawn mobs.','',
  '## Nexus recipes to define','',
  'These blocks are now registered with items, models, loot, mining tags and executable structure definitions. Their survival crafting recipes are deliberately pending. Counts below are physical blocks per blueprint, not additional recipe ingredients.','',
  '| Missing output | Used by (quantity) | Status |','|---|---|---|']
 for r in missing:
  used=', '.join(f'{names[k]} ({v})' for k,v in r['machines'].items())
  lines.append(f"| `{r['id']}` | {used} | No crafting/assembler recipe |")
 lines+=['','## Additional recipe work','',
  '| Item / system | Missing work |','|---|---|',
  '| `revoindustry:nexus_construction_program` | Retained builder program needs its recipe and server-verified endgame unlock. No survival recipe exists yet. |',
  '| Nexus processing chain | The three native MI recipe types are registered; the proposed alloy, staged plating, target, essence and lattice recipes/items still need the approved recipe ledger. No production recipes were invented in this registration pass. |',
  '| Nexus bill of materials | Apply the approved roughly 1.5× material target across the block and Core recipes, including embedded intermediates, without double charging. Keep the builder cheap. |','',
  '## Existing routes and exceptions','',
  '| Block / group | Audit result |','|---|---|',
  '| Existing Revo machine controllers, casings, hatches and parts | Existing recipe routes found; inspect the JSON inventory for every output and recipe ID. |',
  '| Three `revoindustry:*power_converter` tiers | Recipes already exist and require Flux Networks plus MI. Their absence in a stripped test instance is not a missing pack recipe. |',
  '| `revocultivation:cotton_crop` | Uses the `revocultivation:cotton_seeds` item and its existing seed recipe. The block ID is not a separate craftable item. |',
  '| `revoindustry:salt_water` | Fluid-world block has no item by design; geode processing produces the fluid. |',
  '| `revoextraction:dust` | No grid recipe: hammer Sand or Red Sand using the existing `HammerItem.result` interaction. This is a real manual production route, not a missing progression ingredient. |',
  '| Native MI item/fluid/energy hatches used by Nexus | Existing native MI recipes; no duplicate cosmetic hatches were registered. |','',
  '## How to refresh','',
  'Run `dev.cmd :revoindustry:runNexusTests -PrevoNexusTests`, confirm the native tests pass, then run `.local/venv/Scripts/python.exe scripts/audit_machine_recipes.py`. This refreshes the registry evidence and this report without creating recipes.','']
 (ROOT/'missingrecipies.md').write_text('\n'.join(lines))
 print(f'Audited {len(rows)} registered Revo blocks; {len(missing)} Nexus block recipes pending. No recipes changed.')

if __name__=='__main__':main()
