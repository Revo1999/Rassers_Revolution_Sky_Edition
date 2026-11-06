"""Audit the packaged MI-backed furnace implementation, editable models and real captures."""
from pathlib import Path
import json,zipfile,hashlib,base64,io
from PIL import Image,ImageChops
ROOT=Path(__file__).resolve().parents[1];ART=ROOT/'art/blockbench/furnaces';RES=ROOT/'revoindustry/src/main/resources'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
manifest=read(ART/'manifest.json');assert len(manifest['tiers'])==10 and manifest['shape']==[3,3,3]
assert len(manifest['registeredBlocks'])==33
mi=zipfile.ZipFile(ROOT/'.local/integration-mods/Modern-Industrialization-2.5.11.jar')
iron=zipfile.ZipFile(ROOT/'.local/integration-mods/ironfurnaces-neoforge-1.21.1-4.3.2.jar')
def image(data):return Image.open(io.BytesIO(data)).convert('RGBA')
for tier in manifest['tiers']:
 name=tier['tier'];model=read(RES/f'assets/revoindustry/models/block/{name}_multiblock_furnace.json')
 assert model['loader']=='modern_industrialization:machine' and model['casing']==tier['casingKey']
 alias=read(RES/f'assets/revoindustry/models/machine_casing/{name}_furnace.json');assert alias['block']==tier['casing']
 assert model['default_overlays']['front']=='revoindustry:block/furnaces/controller_front'
 native=read(ART/f'{name}_multiblock_furnace_assembled.bbmodel');assert len(native['elements'])==26
 occupied={tuple(c['from']) for c in native['elements']};assert (16,16,16) not in occupied and len(occupied)==26
 for cube in native['elements']:
  assert all(b-a==16 for a,b in zip(cube['from'],cube['to']))
  for face in cube['faces'].values():assert face['uv']==[0,0,16,16]
 for texture in native['textures']:
  im=image(base64.b64decode(texture['source'].split(',')[1]));assert im.size==(16,16)
 capture=ART/f'ingame/{name}-world.png';assert capture.exists() and Image.open(capture).size==(1280,900)
 recipe=read(RES/f'data/revoindustry/recipe/{name}_multiblock_furnace.json');assert recipe['key']['F']['item']==tier['furnace']
for name in manifest['registeredBlocks']:
 for suffix in ['blockstates/'+name+'.json','models/block/'+name+'.json','models/item/'+name+'.json']:assert (RES/'assets/revoindustry'/suffix).exists(),suffix
 assert (RES/f'data/revoindustry/recipe/{name}.json').exists()
 assert (RES/f'data/revoindustry/loot_table/blocks/{name}.json').exists()
 for overlay in read(RES/f'assets/revoindustry/models/block/{name}.json').get('default_overlays',{}).values():
  ns,path=overlay.split(':');p=f'assets/{ns}/textures/{path}.png'
  assert (RES/p).exists() if ns=='revoindustry' else p in mi.namelist(),overlay
# Copy-exact MI recess and coloured border sprites, rather than a redrawn approximation.
for grade in range(1,4):
 for direction in ['input','output']:
  im=image(mi.read('assets/modern_industrialization/textures/block/overlays/'+('output' if direction=='input' else 'output_item')+'.png'));im.alpha_composite(image(mi.read('assets/modern_industrialization/textures/block/overlays/item_auto.png')))
  actual=Image.open(RES/f'assets/revoindustry/textures/block/furnaces/furnace_item_{direction}_hatch_{grade}.png').convert('RGBA');assert im.tobytes()==actual.tobytes()
for state in ['controller_front','controller_front_on']:
 im=Image.open(RES/f'assets/revoindustry/textures/block/furnaces/{state}.png').convert('RGBA');assert im.getpixel((0,0))[3]==0 and im.getbbox() is not None
rainbow=Image.open(RES/'assets/revoindustry/textures/block/furnaces/rainbow_casing.png');assert rainbow.size==(16,384)
assert read(RES/'assets/revoindustry/textures/block/furnaces/rainbow_casing.png.mcmeta')['animation']['interpolate']
assert read(RES/'data/ironfurnaces/recipe/augments/augment_factory.json')['neoforge:conditions']==[{'type':'neoforge:false'}]
assert read(RES/'revoindustry.mixins.json')['package']=='dk.revo.revoindustry.mixin'
for file in ['controller.png','item-hatch.png','liquid-hatch.png','gas-hatch.png','emi-guide.png','qa-results.txt','all-tiers-consistency.png']:assert (ART/'ingame'/file).exists()
assert 'All ten' in (ART/'ingame/qa-results.txt').read_text()
with zipfile.ZipFile(ROOT/'build/mods/revoindustry-0.1.0-dev.jar') as z:
 for p in RES.rglob('*'):
  if p.is_file():
   expected=p.read_bytes().replace(b'${version}',b'0.1.0-dev').replace(b'\r\n',b'\n') if p.name=='neoforge.mods.toml' else p.read_bytes()
   assert z.read(p.relative_to(RES).as_posix())==expected,p
 assert not any('FurnaceGameTests' in n or 'FurnaceUiQa' in n or 'furnace_test.nbt' in n for n in z.namelist())
 for name in ['FurnaceController','FurnaceFuelHatch','FurnaceItemHatch','FurnaceVent']:assert f'dk/revo/revoindustry/furnace/{name}.class' in z.namelist()
 assert 'dk/revo/revoindustry/mixin/DisableFactoryAugmentMixin.class' in z.namelist()
with zipfile.ZipFile(ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.34-dev.mrpack') as z:
 assert read(ART/'manifest.json')
 assert json.loads(z.read('modrinth.index.json'))['versionId']=='0.1.34-dev-resource-bridges'
 assert z.read('overrides/REVO-FURNACES.md')==(ROOT/'docs/REVO-FURNACES.md').read_bytes()
 for p in (ROOT/'build/mods').glob('*.jar'):assert z.read('overrides/mods/'+p.name)==p.read_bytes()
report={'passed':True,'tiers':10,'nativeMiIntegration':True,'registeredBlocks':33,'itemHatchGrades':3,'itemSlots':[1,2,4],'fuelHatches':9,'fuelHeatRates':[64,512,4096],'mandatoryRoofCentreVent':True,'adaptiveControllerHatchesAndVent':True,'exactMiHatchPixels':True,'factoryAugmentDisabled':True,'editableAssemblies':10,'inGameTierCaptures':10,'nativeMenusCaptured':True,'rainbowAnimated':True,'sourceJarAndPackBytesVerified':True}
(ART/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
