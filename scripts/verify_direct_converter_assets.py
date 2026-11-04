"""Lightweight release/artifact check. This does not launch or play-test Minecraft."""
from pathlib import Path
import json,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'

def main():
 log=(ROOT/'.local/direct-converter-production.log').read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in log
 lock=json.loads((ROOT/'pack/mods.lock.json').read_text(encoding='utf-8-sig'));assert len(lock['mods'])==86
 assert not any(m['project']=='analog-audio' for m in lock['mods'])
 with zipfile.ZipFile(PACK) as pack:
  index=json.loads(pack.read('modrinth.index.json'));assert index['versionId']=='0.1.21-dev-direct-power' and len(index['files'])==87
  assert not any('analog-audio' in n['path'].lower() for n in index['files'])
  for mod in ['revolib','revoindustry','revocultivation']:
   path=ROOT/'build/mods'/f'{mod}-0.1.0-dev.jar';assert pack.read('overrides/mods/'+path.name)==path.read_bytes()
  with zipfile.ZipFile(ROOT/'build/mods/revolib-0.1.0-dev.jar') as lib:
   assert 'dk/revo/revolib/energy/DirectPowerConversion.class' in lib.namelist()
   assert not any('PowerConversionBuffer' in n for n in lib.namelist())
  with zipfile.ZipFile(ROOT/'build/mods/revoindustry-0.1.0-dev.jar') as industry:
   assert not any('AnalogAudioCreateCompat' in n for n in industry.namelist())
   for name in ['power_converter','advanced_power_converter','elite_power_converter']:
    states=json.loads(industry.read(f'assets/revoindustry/blockstates/{name}.json'));assert len(states['variants'])==6
    assert f'data/revoindustry/recipe/{name}.json' in industry.namelist()
    assert f'assets/revoindustry/models/block/{name}.json' in industry.namelist()
   for n in ['unfired_clay_bucket','clay_bucket','clay_water_bucket','clay_lava_bucket']:
    assert industry.read(f'assets/revoindustry/textures/item/{n}.png')==(ROOT/f'art/items/chapter-one-refresh/exports/{n}.png').read_bytes()
  with zipfile.ZipFile(ROOT/'build/mods/revocultivation-0.1.0-dev.jar') as cultivation:
   assert cultivation.read('assets/revocultivation/textures/item/crook.png')==(ROOT/'art/items/chapter-one-refresh/exports/crook.png').read_bytes()
  assert pack.read('overrides/POWER-CONVERSION.md')==(ROOT/'docs/POWER-CONVERSION.md').read_bytes()
 for p in (ROOT/'art/items/chapter-one-refresh/exports').glob('*.png'):assert Image.open(p).size==(16,16)
 report={'result':'build and artifact checks passed','scope':'Compilation and static release assets only; gameplay testing left to user',
         'tiers':[{'name':n,'euPerTick':r,'fePerTickAtRatio10':r*10} for n,r in [('Basic',32),('Advanced',128),('Elite',512)]],
         'storage':'none','orientations':6,'kineticConversion':False,'radioRemoved':True,'itemTextures':'four clay buckets and wooden crook, 16px',
         'pack':str(PACK)}
 (ROOT/'docs/POWER-CONVERSION-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print('PASS: assemble-only build, three six-direction converter assets, radio removal and refreshed item textures packaged. Gameplay not tested.')

if __name__=='__main__':main()
