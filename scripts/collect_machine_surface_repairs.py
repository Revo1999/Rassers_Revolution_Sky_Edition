"""Collect native MCP exports; no replacement geometry is authored here."""
from pathlib import Path
import json,shutil
from audit_model_planes import ROOT

# The editable fluid projects embed preview sprites; the game uses native animated
# atlas sprites. This is texture metadata adaptation, never geometry generation.
FLUID_TEXTURES={'water':'minecraft:block/water_still','water_preview':'minecraft:block/water_still','lava_preview':'minecraft:block/lava_still'}

def main():
 import sys
 if '--refresh' in sys.argv:
  for row in json.loads((ROOT/'art/blockbench/surface-repairs/manifest.json').read_text()):
   collect(Path(ROOT/row['source']).with_suffix('.json'),ROOT/row['model'])
  print('Refreshed runtime models from checked-in Blockbench exports.');return
 rows=json.loads((ROOT/'.local/machine-surface-repair-plan.json').read_text())
 manual=ROOT/'.local/manual-machine-operations.json'
 if manual.exists():
  for row in json.loads(manual.read_text()):
   if not any(r['file']==row['target'] for r in rows):rows.append({'file':row['target'],'native':row['native'],'disabled':[],'patches':[]})
   if row.get('canonical'):shutil.copy2(row['native'],ROOT/row['canonical'])
 art=ROOT/'art/blockbench/surface-repairs';art.mkdir(parents=True,exist_ok=True)
 report=[]
 for row in rows:
  source=Path(row['native']);export=source.with_suffix('.json');target=ROOT/row['file']
  collect(export,target)
  dest=art/row['file'].split('/')[0]
  dest.mkdir(parents=True,exist_ok=True);shutil.copy2(source,dest/source.name);shutil.copy2(export,dest/export.name)
  report.append({'model':row['file'],'source':(dest/source.name).relative_to(ROOT).as_posix(),'textureBindings':{k:FLUID_TEXTURES[v] for k,v in json.loads(export.read_text()).get('textures',{}).items() if v in FLUID_TEXTURES}})
 (art/'manifest.json').write_text(json.dumps(report,indent=2)+'\n');print('Collected',len(report),'native model repairs.')

def collect(export,target):
 original=json.loads(target.read_text());data=json.loads(export.read_text());assert data.get('format_version')=='1.9.0'
 for key,value in data.get('textures',{}).items():
  if value in FLUID_TEXTURES:data['textures'][key]=FLUID_TEXTURES[value]
 for key in ['parent','render_type','display','ambientocclusion','gui_light']:
  if key in original:data[key]=original[key]
 if 'particle' in original.get('textures',{}):
  particle=original['textures']['particle']
  while particle.startswith('#'):particle=original['textures'][particle[1:]]
  data['textures']['particle']=particle
 target.write_text(json.dumps(data,indent=2)+'\n')
if __name__=='__main__':main()
