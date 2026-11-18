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
 lines=['# Missing machine-block recipes','',f"Audited {len(rows)} registered Revo blocks against the native recipe manager and packaged recipes. See [machine-recipe-audit.json](docs/machine-recipe-audit.json) for evidence.",'',f"**{len(missing)} Nexus block recipes are missing.** The [Nexus material ledger](docs/NEXUS-MATERIAL-LEDGER.md) tracks the full survival route, including the retained Construction Program.",'']
 remaining=[r for r in rows if r['status']=='no recipe']
 if remaining:
  lines += ['| Block | Status |','| --- | --- |']+[f"| `{r['id']}` | No crafting route found |" for r in remaining]+['']
 lines+=['Fluid blocks have buckets and processing routes. World-only crops use their seed items. Dust uses the manual sand hammering interaction. Nexus hatches use the native MI recipes.','','Refresh with `dev.cmd :revoindustry:runNexusTests -PrevoNexusTests` followed by `python scripts/audit_machine_recipes.py`.','']
 (ROOT/'missingrecipies.md').write_text('\n'.join(lines),encoding='utf-8')
 print(f'Audited {len(rows)} registered Revo blocks; {len(missing)} Nexus block recipes pending.')

if __name__=='__main__':main()
