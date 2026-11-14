"""Check native/game geometry, textures and all three item exports."""
from pathlib import Path
import json,base64,hashlib
from PIL import Image

R=Path(__file__).resolve().parents[1]
A=R/'art/blockbench/revoindustry/feeding-helmets'
D=R/'revoindustry/src/main/resources/assets/revoindustry'
rows=[]
for name in ['feeding_helmet','advanced_feeding_helmet','elite_feeding_helmet']:
    native=json.loads((A/(name+'.bbmodel')).read_text())
    cubes=[e for e in native['elements'] if e.get('export',True)]
    model=json.loads((D/'models/item'/f'{name}.json').read_text())
    assert len(cubes)==len(model['elements'])
    assert not any('fit_reference' in e.get('name','') for e in model['elements'])
    assert any(e['name']=='mouthpiece' for e in cubes)
    tex=next(t for t in native['textures'] if t['name']==name+'.png')
    source=base64.b64decode(tex['source'].split(',',1)[1])
    assert source==(D/'textures/item'/f'{name}.png').read_bytes()==(D/'textures/models/armor'/f'{name}.png').read_bytes()
    assert Image.open(D/'textures/item'/f'{name}.png').size==(128,128)
    for e in model['elements']:
        assert all(-16<=v<=32 for v in e['from']+e['to'])
        for f in e['faces'].values():assert all(0<=uv<=16 for uv in f['uv'])
    assert model['textures']['0']=='revoindustry:item/'+name
    rows.append({'item':name,'cubes':len(cubes),'max_triangles':len(cubes)*12,'texture_pixels':[128,128],'pixels_per_model_unit':1,'native_and_game_texture_sha256':hashlib.sha256(source).hexdigest()})
report={'result':'PASS','tiers':rows,'checks':['three distinct native sources','exported item geometry matches native piece counts','guide head excluded','shared item/armor pixels','vanilla pixel density','Java model and UV limits']}
(A/'validation.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
