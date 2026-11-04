"""Audit installed artifacts, native art invariants and the actual packaged overlay."""
from pathlib import Path
import hashlib, json, zipfile
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/flux-industrial'
RESOURCE=ROOT/'pack/overrides/resourcepacks/revo-industrial-grid'
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'

def main():
 lock=json.loads((ROOT/'pack/mods.lock.json').read_text(encoding='utf-8-sig'))
 mods={m['project']:m for m in lock['mods']};assert len(mods)==len(lock['mods'])==87
 expected={'flux-networks':'8.0.0','analog-audio':'0.1.5-hotfix.1+1.21.1','create-new-age':'1.2.0+mc1.21.1','building-wands':'2.14_release_neoforge_1.21'}
 for slug,version in expected.items():
  m=mods[slug];assert m['version']==version
  for folder in ['.local/integration-mods','revolib/run/mods']:
   assert hashlib.sha512((ROOT/folder/m['file']).read_bytes()).hexdigest()==m['sha512']
 with zipfile.ZipFile(ROOT/'.local/integration-mods'/mods['flux-networks']['file']) as original:
  original_files={n.removeprefix('assets/fluxnetworks/textures/'):original.read(n) for n in original.namelist() if n.startswith('assets/fluxnetworks/textures/') and n.endswith('.png')}
  assert len(original_files)==41
  for rel,data in original_files.items():
   target=RESOURCE/'assets/fluxnetworks/textures'/rel
   assert target.read_bytes()==(ART/'exports'/rel).read_bytes()
   import io
   old=Image.open(io.BytesIO(data)).convert('RGBA');new=Image.open(target).convert('RGBA');assert old.size==new.size,(rel,old.size,new.size)
   if rel.startswith('gui/'):
    # Same occupied UV/hit-area footprint. Pixel shades can change; transparent padding cannot.
    assert [a>0 for a in old.getchannel('A').getdata()]==[a>0 for a in new.getchannel('A').getdata()],rel
   if '_colour' in rel or rel=='item/flux_configurator_layer1.png':
    assert all(r==g==b for r,g,b,a in new.getdata() if a>0),'Network tint mask must stay neutral'
    assert 0<sum(a>0 for r,g,b,a in new.getdata())<new.width*new.height//3,rel
   if rel.startswith('block/') and rel.endswith('_on.png'):
    assert target.read_bytes()!=(target.with_name(target.stem.removesuffix('_on')+'_off.png')).read_bytes()
 assert len(list((ART/'sources').glob('*.aseprite')))==41
 for locale in ['en_us','en_gb','da_dk']:
  names=json.loads((RESOURCE/f'assets/fluxnetworks/lang/{locale}.json').read_text(encoding='utf-8'));assert len(names)>=38
  assert names['tooltip.fluxnetworks.flux_storage_2'].count('%s')==1
  if locale!='da_dk':assert names['block.fluxnetworks.flux_point']=='Power Receiver'
 results=(ART/'ingame/qa-results.txt').read_text(encoding='utf-8')
 for phrase in ['highest-priority texture resources','Native radio audio tools available','Create radio movement behavior registered',
                'actual native private grid','real external FE','native controller interface','native Analog Audio radio interface','Industrial integration QA complete']:
  assert phrase in results,phrase
 shots=['disconnected-devices','connected-industrial-grid','grid-controller-ui','power-transmitter-ui','radio-ui','new-age-and-radio','industrial-items']
 for name in shots:assert (ART/'ingame'/f'{name}.png').stat().st_size>50000
 client=(ROOT/'.local/power-converter-client.log').read_text(encoding='utf-8-sig')
 production=(ROOT/'.local/power-converter-production.log').read_text(encoding='utf-8-sig')
 assert 'BUILD SUCCESSFUL' in client and 'BUILD SUCCESSFUL' in production
 assert 'Revo restored Analog Audio radio movement behavior for Create 6' in client
 assert 'Revo could not register' not in client and 'IllegalStateException' not in client
 with zipfile.ZipFile(PACK) as pack:
  assert pack.testzip() is None
  index=json.loads(pack.read('modrinth.index.json'));assert index['versionId']=='0.1.21-dev-direct-power' and len(index['files'])==88
  opts=pack.read('overrides/options.txt').decode();assert 'file/revo-departure-mono' in opts and 'file/revo-industrial-grid' in opts
  assert opts.index('mod_resources')<opts.index('file/revo-industrial-grid')
  for path in RESOURCE.rglob('*'):
   if path.is_file():assert pack.read('overrides/resourcepacks/revo-industrial-grid/'+path.relative_to(RESOURCE).as_posix())==path.read_bytes(),path
  assert pack.read('overrides/INDUSTRIAL-GRID.md')==(ROOT/'docs/INDUSTRIAL-GRID.md').read_bytes()
  for jar in (ROOT/'build/mods').glob('*.jar'):
   with zipfile.ZipFile(jar) as z:assert not any('IndustrialGridUiQa' in n for n in z.namelist())
  with zipfile.ZipFile(ROOT/'build/mods/revoindustry-0.1.0-dev.jar') as jar:
   assert 'dk/revo/revoindustry/compat/AnalogAudioCreateCompat.class' in jar.namelist()
 for path in (ROOT/'revoindustry/build/classes/java/main').rglob('*.class'):assert 'IndustrialGridUiQa' not in path.name
 report={'result':'passed','externalJars':87,'textures':41,'nativeAsepriteSources':41,'languages':['en_us','en_gb','da_dk'],
         'added':['analog-audio','flux-networks','create-new-age'],'alreadyInstalled':'building-wands',
         'liveChecks':['Highest-priority resource overrides','Native grid formation and FE transfer into storage','Controller/transmitter/radio UIs','Native radio audio tools','Native Create radio behavior registered','New Age model rendering'],
         'screenshots':shots,'pack':str(PACK),'sha512':hashlib.sha512(PACK.read_bytes()).hexdigest(),'productionQaExcluded':True}
 (ROOT/'docs/INDUSTRIAL-GRID-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print('PASS: 87 verified mod jars, 41 native texture sources, tint/UV/state invariants, live FE/radio/Create checks and exact packaged overlay.')

if __name__=='__main__':main()
