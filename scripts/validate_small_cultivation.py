"""Audit both one-block native exports, face density, resources and delivery evidence."""
from pathlib import Path
import json,base64,math,zipfile,hashlib
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/small-cultivation'
RES=ROOT/'revocultivation/src/main/resources'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def main():
 atlas=ART/'small_cultivation_atlas.png';assert Image.open(atlas).size==(128,128)
 assert atlas.read_bytes()==(RES/'assets/revocultivation/textures/block/small_cultivation_atlas.png').read_bytes()
 report={}
 for name in ['composter','silkworm_cultivator']:
  src=read(ART/(name+'.bbmodel'));model=read(RES/('assets/revocultivation/models/block/'+name+'.json'))
  assert src['meta']['model_format']=='java_block' and len(src['elements'])==(5 if name=='composter' else 12)
  assert base64.b64decode(src['textures'][0]['source'].split(',')[1])==atlas.read_bytes()
  assert len(model['elements'])==(5 if name=='composter' else 8)
  if name=='silkworm_cultivator':assert not any(e['name'] in ['feed_bed','silkworm_0','hanging_cocoon_left','hanging_cocoon_right'] for e in model['elements']),'Live contents must not remain baked into the body'
  assert model['render_type']=='minecraft:cutout'
  checked=0
  for e in src['elements']:
   a,b=e['from'],e['to'];d=[y-x for x,y in zip(a,b)]
   assert all(math.isfinite(v) and 0<=v<=16 for v in a+b) and all(v>0 for v in d)
   for f,data in e['faces'].items():
    if data.get('texture') is None:continue
    u,v,U,V=data['uv'];wh=(d[0],d[2]) if f in ['up','down'] else (d[2],d[1]) if f in ['east','west'] else (d[0],d[1])
    assert all(0<=n<=128 for n in [u,v,U,V])
    assert abs(abs(U-u)-wh[0])<1e-5 and abs(abs(V-v)-wh[1])<1e-5,(name,e['name'],f)
    checked+=1
  assert not any('shaft' in e['name'] or 'drive' in e['name'] for e in src['elements']),'Passive machines have no kinetic port'
  assert not any(e['name'].startswith(('stone_','iron_','oak_post_','cobble_','analogue_','front_stone_')) for e in src['elements'])
  for e in src['elements']:
   if e['name'].startswith(('wooden_','rear_shaft_','silk_rack_','cocoon_rack_')):
    for face in e['faces'].values():assert 2<=face['uv'][0]<face['uv'][2]<=18 and 42<=face['uv'][1]<face['uv'][3]<=58,(name,e['name'],'Single dithered wood material required')
  assert next(e for e in src['elements'] if e['name']=='wooden_bucket_front')['to'][1]==(16 if name=='composter' else 10)
  if name=='silkworm_cultivator':
   rack=next(e for e in src['elements'] if e['name']=='cocoon_rack_crossbar')
   original=read(ART/'archive/detailed-v1/silkworm_cultivator.bbmodel')
   old=next(e for e in original['elements'] if e['name']=='cocoon_rack_crossbar')
   assert rack['uuid']==old['uuid'] and rack['from']==old['from'] and rack['to']==old['to'],'Central rack must be preserved'
  assert len(read(RES/('assets/revocultivation/blockstates/'+name+'.json'))['variants'])==4
  for e in model['elements']:
   for face in e['faces'].values():assert all(0<=v<=16 for v in face['uv'])
  assert (ART/'previews'/(name+'.png')).is_file()
  report[name]={'sourceCubes':len(src['elements']),'bakedCubes':len(model['elements']),'sourceQuads':checked,'sourceTriangles':checked*2,'dimensions':'1 x 1 x 1','density':'16px/block','power':'none'}
 for path in (ART/'contents').glob('*.bbmodel'):
  src=read(path);model=read(RES/('assets/revocultivation/models/block/'+path.stem+'.json'))
  assert len(src['elements'])==1 and len(model['elements'])==1
  assert base64.b64decode(src['textures'][0]['source'].split(',')[1])==atlas.read_bytes()
  assert list(model['elements'][0]['faces'])==['up']
  uv=src['elements'][0]['faces']['up']['uv'];assert uv[2]-uv[0]==10 and uv[3]-uv[1]==10
 assert len(list((ART/'contents').glob('*.bbmodel')))==6
 for name,count in [('cultivator_live_worm',1),('cultivator_live_cocoons',2)]:assert len(read(RES/('assets/revocultivation/models/block/'+name+'.json'))['elements'])==count
 report['liveContents']={'surfaceModels':6,'wormCubes':1,'cocoonCubes':2,'staticContentsRemoved':True}
 for path in RES.rglob('*.json'):read(path)
 for item in ['compost','silkworm','silk_cocoon','silk']:assert Image.open(RES/('assets/revocultivation/textures/item/'+item+'.png')).size==(16,16)
 if (ROOT/'.local/small-cultivation-tests.log').exists():
  log=(ROOT/'.local/small-cultivation-tests.log').read_text(encoding='utf-8-sig');assert 'All 19 required tests passed' in log and 'BUILD SUCCESSFUL' in log
  report['serverTests']='19 passed (9 existing farms + 10 single-block tests, including fixed passive timers)'
 (ART/'asset-validation.json').write_text(json.dumps(report,indent=2)+'\n')
 print('PASS: two native 1-block models; bounds, proportional UVs, embedded 128px atlas, passive bodies without kinetic ports and JSON resources; '+str(report.get('serverTests','tests pending')))
if __name__=='__main__':main()

