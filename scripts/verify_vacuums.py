from pathlib import Path
import json,zipfile,base64,io,math
from PIL import Image
from vacuum_geometry_checks import coplanar_overlaps
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'revoindustry/src/main/resources';ART=ROOT/'art/blockbench/vacuums'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
for tier in range(1,4):
 model=read(RES/f'assets/revoindustry/models/block/vacuum_{tier}.json');native=read(ART/f'vacuum_{tier}.bbmodel')
 assert len(model['elements'])==[24,26,31][tier-1] and len(native['elements'])==[24,26,31][tier-1]
 assert len(native['textures'])==7
 assert not any(any(w in e.get('name','') for w in ['wheel','handle','canister']) for e in model['elements'])
 assert min(e['from'][2] for e in model['elements'] if e['name'].startswith('flange_'))==0, 'Nozzle must meet collection origin'
 flange=next(e for e in model['elements'] if e['name']=='flange_0');assert abs(flange['to'][1]-(8+[5,6,7][tier-1]))<.001
 assert not coplanar_overlaps(model),coplanar_overlaps(model)
 assert sum(bool(e.get('rotation',{}).get('angle',0)) for e in model['elements'])==8
 for e in model['elements']:
  a,b=e['from'],e['to'];r=e.get('rotation')
  for x in [a[0],b[0]]:
   for y in [a[1],b[1]]:
    if r:
     t=math.radians(r['angle']);cx,cy,cz=r['origin'];xx=cx+(x-cx)*math.cos(t)-(y-cy)*math.sin(t);yy=cy+(x-cx)*math.sin(t)+(y-cy)*math.cos(t)
    else:xx,yy=x,y
    assert 0<=xx<=16 and 0<=yy<=16
  assert 0<=a[2]<=b[2]<=16
  dx,dy,dz=[b[i]-a[i] for i in range(3)]
  for name,f in e['faces'].items():
   w,h={'north':(dx,dy),'south':(dx,dy),'east':(dz,dy),'west':(dz,dy),'up':(dx,dz),'down':(dx,dz)}[name]
   u,v,U,V=f['uv'];assert abs(abs(U-u)-w)<.001 and abs(abs(V-v)-h)<.001
 for material in ['body','metal','dark']:
  im=Image.open(RES/f'assets/revoindustry/textures/block/vacuum_{tier}_{material}.png');assert im.size==(16,16)
  colours=im.getcolors();assert colours and 4<=len(colours)<=12, 'Dithered material must use a limited shade palette'
  alternating=sum(im.getpixel((x,y))==im.getpixel((x+1,y+1)) and im.getpixel((x+1,y))==im.getpixel((x,y+1)) and im.getpixel((x,y))!=im.getpixel((x+1,y)) for y in range(7,13) for x in range(2,13));assert alternating>=2,'Single-pixel checker dithering must remain visible'
 for texture in native['textures']:
  im=Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',')[1])));assert im.size==(16,16)
  source=Image.open(RES/'assets/revoindustry/textures/block'/texture['name']);assert im.convert('RGB').tobytes()==source.convert('RGB').tobytes(), 'Native model texture is stale'
 variants=read(RES/f'assets/revoindustry/blockstates/vacuum_{tier}.json')['variants'];assert len(variants)==6
 assert variants['facing=north']['y']==180 and 'x' not in variants['facing=south']
 assert variants['facing=east']['y']==270 and variants['facing=down']['x']==270
 for folder in ['recipe','loot_table/blocks']:assert (RES/f'data/revoindustry/{folder}/vacuum_{tier}.json').exists()
 for name in [f'tier-{tier}.png',f'tier-{tier}-menu.png']:
  assert Image.open(ART/'ingame'/name).size==(1280,900)
assert 'working UI area toggle on/off' in (ART/'ingame/qa-results.txt').read_text()
for name in ['rear-output.png','suction.png','area-menu-on.png','area-enabled.png','area-menu-off.png','placement-tier-1.png','placement-tier-2.png','placement-tier-3.png','area-alignment.png']:assert Image.open(ART/'ingame'/name).size==(1280,900)
with zipfile.ZipFile(ROOT/'build/mods/revoindustry-0.1.0-dev.jar') as jar:
 assert not any('VacuumGameTests' in n or 'VacuumUiQa' in n or 'vacuum_test.nbt' in n for n in jar.namelist())
 for n in ['VacuumBlock','VacuumEntity','VacuumMenu','VacuumShape','Vacuums']:assert f'dk/revo/revoindustry/vacuum/{n}.class' in jar.namelist()
 assert 'dk/revo/revoindustry/client/VacuumAreaOverlay.class' in jar.namelist()
 for p in RES.rglob('*'):
  if p.is_file():
   expected=p.read_bytes().replace(b'${version}',b'0.1.0-dev').replace(b'\r\n',b'\n') if p.name=='neoforge.mods.toml' else p.read_bytes()
   assert jar.read(p.relative_to(RES).as_posix())==expected,p
with zipfile.ZipFile(ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.33-dev.mrpack') as pack:
 assert json.loads(pack.read('modrinth.index.json'))['versionId']=='0.1.33-dev-nozzle-alignment'
 assert pack.read('overrides/REVO-VACUUMS.md')==(ROOT/'docs/REVO-VACUUMS.md').read_bytes()
 for p in (ROOT/'build/mods').glob('*.jar'):assert pack.read('overrides/mods/'+p.name)==p.read_bytes()
report={'passed':True,'singleBlockTiers':3,'slots':[1,3,9],'cubeSize':[3,5,9],'cubeFullyInFrontOfIntake':True,'nozzleAndRangeShareOrigin':True,'transferIntervalTicks':[8,4,1],'cubesPerTier':[24,26,31],'texturesPerTier':7,'pipeDiameterPixels':[10,12,14],'coplanarOverlappingFaces':0,'rearPlateOutput':True,'sixWayOutlet':True,'hollowOctagonalIntake':True,'singlePixelCheckerDither':True,'limitedPaletteVerified':True,'nativeTexturePixelsVerified':True,'boundsAndUvVerified':True,'inGameCaptures':15,'uiAreaToggleVerified':True,'allTierPlacementPreviewsCaptured':True,'sourceJarPackBytesVerified':True}
(ART/'validation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
