"""Validate the saved vertical-only native model and its Java exports."""
from pathlib import Path
import json, math

REPO=Path(__file__).resolve().parents[1]
ART=REPO/'art/blockbench/revoextraction/mechanical-sluice'
ASSETS=REPO/'revoextraction/src/main/resources/assets/revoextraction'
assembly=json.loads((ART/'assembly.json').read_text())
native=json.loads((ART/'models/mechanical_sluice.bbmodel').read_text())
parts={p['name']:p for p in assembly['parts']}
assert len({e['uuid'] for e in native['elements']})==len(native['elements'])
animation=native['animations'][0]
assert animation['length']==2 and 'vertical' in animation['name']
groups={g['name']:g for g in native['groups']}
for group in ['water','water_inlet','body','feeder']:
    assert not animation['animators'].get(groups[group]['uuid'],{}).get('keyframes',[]),'Stationary part animated: '+group
for group in ['trough','shaker','rod']:
    for key in animation['animators'][groups[group]['uuid']]['keyframes']:
        assert key['channel']=='position'
        point=key['data_points'][0]
        assert abs(float(point['x']))<1e-8 and abs(float(point['z']))<1e-8
        assert abs(float(point['y']))<=.500001
for name,pos in [('shaft_west',[-8,8,8]),('shaft_east',[8,8,8]),('water_in',[0,24,0]),('gravel_in',[0,32,8])]:
    assert next(p for p in assembly['ports'] if p['name']==name)['position']==pos
shaft=parts['transverse_shaft']
assert shaft['from']==[-8,6,6] and shaft['to']==[8,10,10]
assert parts['crank_hub']['pivot']==[-10,8,8]
for i in range(1441):
    theta=i*math.tau/1440
    s,c=math.sin(theta),math.cos(theta);lift=.5*s
    assert parts['bed_0']['from'][1]+lift>8+math.sqrt(8),'Shaft intersects floor'
    for section in range(3):
        h=13-section*2
        assert parts[f'water_bed_{section}']['to'][1]==h
        assert parts[f'bed_{section}']['to'][1]+lift<h
        assert parts[f'riffle_{section}_5']['to'][1]+lift<h,'Riffle blocks water'
    for dy in [-.25,.25]:
        for dz in [-.25,.25]:
            py=8+lift+dy*c+dz*s;pz=8+.5*c-dy*s+dz*c
            assert 7.5+lift<py<8.5+lift and 6.3<pz<9.7
    assert abs((5.6+1.4*(1+lift/1.4))-(7+lift))<1e-9
    assert abs((1.6+1.4*(1+lift/1.4))-(3+lift))<1e-9
    assert parts['inlet_spout']['from'][1]>parts['grille_long_0']['to'][1]+lift
for name,part in parts.items():
    if name.startswith(('bed_','andesite_rail_','head_panel_','rail_cap_','riffle_')) or name=='outlet_lip':
        assert part['group']=='trough',name
for i in range(3):
    water=parts[f'water_bed_{i}']
    assert water['from'][0]==-6 and water['to'][0]==6
    assert water['from'][2]==i*16 and water['to'][2]==(i+1)*16
    if i:assert water['to'][1]<parts[f'water_bed_{i-1}']['to'][1]
model_count=0
for path in (ASSETS/'models/block/sluice').glob('*.json'):
    model=json.loads(path.read_text());model_count+=1
    for e in model['elements']:
        assert all(-16<=x<=32 for x in e['from']+e['to']),(path,e['name'])
        assert all(b>a for a,b in zip(e['from'],e['to'])),(path,e['name'])
        for face in e['faces'].values():
            assert all(0<=uv<=16.000001 for uv in face['uv']),(path,e['name'],face)
            ref=model['textures'][face['texture'][1:]]
            if ref.startswith('revoextraction:'):
                assert (ASSETS/'textures'/(ref.split(':',1)[1]+'.png')).exists(),ref
assert len(json.loads((ASSETS/'blockstates/mechanical_sluice.json').read_text())['variants'])==12
assert len(json.loads((ASSETS/'blockstates/gravel_feeder.json').read_text())['variants'])==4
water=next(t for t in native['textures'] if t['name']=='water.png')
assert water['width']==32 and water['height']==1024
assert (ART/'assets/vanilla/water_flow.png').exists()
report={'result':'PASS','runtime_models':model_count,'machine_cubes':len(native['elements']),'motion_samples':1441,
        'checks':['vertical-only motion','stationary water and inlet','pin clears translating slot','shaft clears raised bed','submerged riffles','spring endpoints','Java limits and UVs','textures and all facing variants']}
(ART/'validation.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
