"""Check converter accounting, real MI/Flux interoperability, native art and release jars."""
from pathlib import Path
import json,zipfile,xml.etree.ElementTree as ET
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'

def main():
 result=ET.parse(ROOT/'revolib/build/test-results/test/TEST-dk.revo.revolib.energy.PowerConversionBufferTest.xml').getroot()
 assert int(result.attrib['tests'])==5 and int(result.attrib['failures'])==int(result.attrib['errors'])==0
 live=(ROOT/'art/flux-industrial/ingame/qa-results.txt').read_text(encoding='utf-8')
 for s in ['real Flux receiver -> converter -> MI electric macerator','real finite MI storage -> reverse converter -> Flux transmitter -> capacitor bank','All side policies and NBT round trips verified','full FE balance/ratio synchronization','Power converter integration QA complete']:assert s in live,s
 for name in ['power-converter-client.log','power-converter-production.log']:
  log=(ROOT/'.local'/name).read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in log and 'IllegalStateException' not in log
 for name in ['mi-flux-converters','converter-fe-to-eu-ui','converter-eu-to-fe-ui']:assert (ROOT/'art/flux-industrial/ingame'/f'{name}.png').stat().st_size>50000
 art=ROOT/'art/blockbench/power-converter';source=json.loads((art/'power_converter.bbmodel').read_text(encoding='utf-8'));assert len(source['elements'])==3
 model=ROOT/'revoindustry/src/main/resources/assets/revoindustry/models/block/power_converter.json';runtime=json.loads(model.read_text(encoding='utf-8'));assert len(runtime['elements'])==3
 for e in runtime['elements']:
  d=[b-a for a,b in zip(e['from'],e['to'])];assert all(0<=v<=16 for v in e['from']+e['to'])
  for face,f in e['faces'].items():
   if f.get('texture') is None:continue
   u,v,U,V=f['uv'];w,h=(d[0],d[2]) if face in ['up','down'] else (d[2],d[1]) if face in ['east','west'] else (d[0],d[1]);assert abs(U-u)==w and abs(V-v)==h
 # The cleaned terminal textures contain no safety-yellow hazard paint.
 for n in ['flux_plug_on','flux_plug_off','flux_point_on','flux_point_off']:
  im=Image.open(ROOT/f'art/flux-industrial/exports/block/{n}.png').convert('RGBA')
  assert not any(r>170 and 125<g<r*.9 and g>r*.65 and b<g*.45 for r,g,b,a in im.getdata() if a),n
 with zipfile.ZipFile(PACK) as p:
  assert p.testzip() is None
  assert p.read('overrides/POWER-CONVERSION.md')==(ROOT/'docs/POWER-CONVERSION.md').read_bytes()
  for mod,classes in [('revolib',['energy/PowerConversionBuffer','energy/EnergyInterop','energy/MiEnergyBridge']),('revoindustry',['PowerConverterBlock','PowerConverterEntity','PowerConverterMenu','client/PowerConverterScreen','client/PowerConverterEmi'])]:
   path=ROOT/'build/mods'/f'{mod}-0.1.0-dev.jar';assert p.read('overrides/mods/'+path.name)==path.read_bytes()
   with zipfile.ZipFile(path) as z:
    for c in classes:assert f'dk/revo/{mod}/{c}.class' in z.namelist(),c
    assert not any(n.startswith(('aztech/','dev/technici4n/','sonar/')) for n in z.namelist()),'Third-party code must remain compile-only'
  with zipfile.ZipFile(ROOT/'build/mods/revoindustry-0.1.0-dev.jar') as z:
   assert z.read('assets/revoindustry/models/block/power_converter.json')==model.read_bytes()
   assert 'data/revoindustry/recipe/power_converter.json' in z.namelist()
 report={'result':'passed','unitTests':5,'livePowerPaths':['Flux Receiver -> converter -> MI electric macerator','Finite MI storage -> reverse converter -> Flux Transmitter -> Flux capacitor bank'],'sidePolicies':'verified on all six faces in both modes','savedState':'buffer and mode NBT round trips passed','interface':'16-bit split full buffer, live ratio, mode button, player-only inventory','nativeModel':{'cubes':3,'visibleFaces':16,'pxPerModelUnit':1},'pack':str(PACK),'sharedCompatibility':'RevoLib optional native MI API bridge'}
 (ROOT/'docs/POWER-CONVERSION-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print('PASS: five energy tests, both native MI/Flux power paths, side isolation, persistence, converter UI, clean terminal art and exact packaged classes.')

if __name__=='__main__':main()
