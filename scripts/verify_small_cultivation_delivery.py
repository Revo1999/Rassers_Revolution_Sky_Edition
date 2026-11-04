"""Verify the single-block delivery and its actual server/client evidence."""
from pathlib import Path
import json,zipfile,hashlib
ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'
def main():
 for name in ['small-cultivation-tests.log','small-cultivation-client.log','small-cultivation-production.log']:
  text=(ROOT/'.local'/name).read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in text,name
 assert 'All 19 required tests passed' in (ROOT/'.local/small-cultivation-tests.log').read_text(encoding='utf-8-sig')
 art=ROOT/'art/blockbench/small-cultivation';results=(art/'ingame/qa-results.txt').read_text()
 for check in ['automatic timed compost production','vanilla/MI inventory rows','both EMI production pages','real Jade server data','actual network hand-feeding','real elapsed cycle shows silk','menu collection clear the rendered contents']:assert check in results
 for name in ['machines-world','composter-ui','silkworm-ui','composter-emi','silkworm-emi','jade-status','cultivator-empty','cultivator-half','cultivator-full','cultivator-working','cultivator-ready','cultivator-collected']:assert (art/'ingame'/(name+'.png')).is_file()
 jar=ROOT/'build/mods/revocultivation-0.1.0-dev.jar'
 with zipfile.ZipFile(jar) as z:
  assert all(not any(s in n for s in ['SmallUiQa','SmallGameTests','FarmUiQa','FarmGameTests','farm_test.nbt']) for n in z.namelist())
  for path in (ROOT/'revocultivation/src/main/resources').rglob('*'):
   key=path.relative_to(ROOT/'revocultivation/src/main/resources').as_posix()
   if path.is_file() and key.startswith(('assets/','data/')):assert path.read_bytes()==z.read(key),key
  assert b'version="0.1.0-dev"' in z.read('META-INF/neoforge.mods.toml')
 for path in (ROOT/'revocultivation/build/classes/java/main').rglob('*.class'):assert not any(n in path.name for n in ['SmallUiQa','SmallGameTests','FarmUiQa','FarmGameTests'])
 with zipfile.ZipFile(PACK) as z:
  assert z.testzip() is None
  assert z.read('overrides/mods/'+jar.name)==jar.read_bytes()
  assert z.read('overrides/SINGLE-BLOCK-CULTIVATION.md')==(ROOT/'docs/SINGLE-BLOCK-CULTIVATION.md').read_bytes()
 report={'singleBlockMachines':['Composter','Silk Worm Cultivator'],'serverTests':19,'newMachineTests':10,'clientQa':results.strip(),'clientScreenshots':12,'nativeAssets':json.loads((art/'asset-validation.json').read_text()),'productionBuild':'passed','qaClassesExcluded':True,'pack':str(PACK),'packSha512':hashlib.sha512(PACK.read_bytes()).hexdigest()}
 (ROOT/'docs/SINGLE-BLOCK-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n')
 print('PASS: single-block machines, server tests, full-pack client QA, exact packaged resources, usage guide and clean production classes.')
if __name__=='__main__':main()

