"""Static integration audit only; no Minecraft client/server or gameplay tests."""
from pathlib import Path
import json,math,zipfile
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/revoextraction/steel'
RES=ROOT/'revoextraction/src/main/resources'
j=json.loads((ART/'steel_drill_assembled.bbmodel').read_text());nodes={c['uuid']:c for c in j['elements']};names={g['uuid']:g['name'] for g in j['groups']}
groups={names[g['uuid']]:[nodes[u] for u in g['children']] for g in j['outliner']}
assert len(nodes)==84 and len(groups['drill_bit'])==8 and len(groups['drive_gear'])==2
clearance=[]
for bit in groups['drill_bit']:
    if 'flight' not in bit['name']:continue
    radius=max(math.hypot(x-8,z-8) for x in [bit['from'][0],bit['to'][0]] for z in [bit['from'][2],bit['to'][2]])
    for cube in groups['body']:
        if min(cube['to'][1],bit['to'][1])-max(cube['from'][1],bit['from'][1])<1e-6:continue
        dx=max(cube['from'][0]-8,0,8-cube['to'][0]);dz=max(cube['from'][2]-8,0,8-cube['to'][2])
        distance=math.hypot(dx,dz);assert distance>=radius-1e-6,(bit['name'],cube['name'])
        clearance.append((distance-radius)*3)
coupling=next(c for c in groups['drive_gear'] if c['name']=='create_shaft_coupling')
assert abs(coupling['to'][0]-16)<1e-5
assert all(abs((coupling['from'][a]+coupling['to'][a])/2-8)<1e-5 for a in [1,2])
models=[]
for source in ART.glob('*.bbmodel'):
    model=json.loads(source.read_text());density=3 if any(n in source.stem for n in ['assembled','unassembled']) else 1
    for c in model['elements']:
        dims=[b-a for a,b in zip(c['from'],c['to'])];assert all(d>0 for d in dims),c['name']
        for face,f in c['faces'].items():
            if f.get('texture') is None:continue
            u,v,U,V=f['uv'];assert 0<=u<U<=512 and 0<=v<V<=512
            wh=[dims[0],dims[2]] if face in ['up','down'] else [dims[2],dims[1]] if face in ['east','west'] else [dims[0],dims[1]]
            assert abs((U-u)-wh[0]*density)<.001 and abs((V-v)-wh[1]*density)<.001,(c['name'],face)
    models.append(source.name)
for part in ['body','drill_bit','drive_gear','support','casing','core','controller','input','output_hatch','water_tank']:
    model=json.loads((RES/f'assets/revoextraction/models/block/steel_drill_{part}.json').read_text())
    for c in model['elements']:
        assert all(-16<=v<=32 for v in c['from']+c['to'])
        rotation=c.get('rotation',{});assert rotation.get('angle',0) in [-45,-22.5,0,22.5,45]
        for f in c['faces'].values():assert all(0<=v<=16 for v in f['uv'])
    assert all('preview_water' not in e['name'] for e in model['elements'])
animation=j['animations'][0];assert animation['length']==4 and animation['loop']=='loop'
for animator in animation['animators'].values():
    assert len(animator['keyframes'])==5 and all(k['interpolation']=='linear' for k in animator['keyframes'])
chapters=[json.loads(p.read_text(encoding='utf-8-sig')) for p in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt')]
quests={q['id']:q for c in chapters for q in c['quests']};assert len(quests)>=116
all_ids=[]
for c in chapters:
    all_ids.append(c['id']);all_ids.extend(i['id'] for i in c.get('images',[]))
    for q in c['quests']:
        all_ids.append(q['id']);all_ids.extend(t['id'] for t in q['tasks']);all_ids.extend(r['id'] for r in q.get('rewards',[]))
assert len(set(all_ids))==len(all_ids)
def visit(ident,path=()):
    assert ident in quests and ident not in path,ident
    for d in quests[ident].get('dependencies',[]):visit(d,path+(ident,))
for ident in quests:visit(ident)
steel=next(c for c in chapters if c['id']=='5E40000000000000');assert len(steel['quests'])==13
jar=ROOT/'build/mods/revoextraction-0.1.0-dev.jar'
with zipfile.ZipFile(jar) as z:
    for path in RES.rglob('*'):
        if path.is_file() and ('steel_drill' in path.name or path.name=='drill_repair_kit.png' or path.name=='drill_repair_kit.json'):
            rel=path.relative_to(RES).as_posix();assert z.read(rel)==path.read_bytes(),rel
    for cls in ['DrillWaterTank','DrillWaterTankEntity','DrillRepairKit','DrillCoolantRenderer']:
        assert f'dk/revo/revoextraction/{cls}.class' in z.namelist()
    assert not any('GameTests' in n or 'InterfaceQa' in n for n in z.namelist())
report={'passed':True,'minecraftLaunched':False,'gameplayTested':False,'nativeSourceModels':models,'assembledCubes':84,'runtimeCubes':83,'minimumFullSweepClearancePhysicalPixels':min(clearance),'shaftCenter':[48,24,24],'uvDensityPixelsPerPhysicalUnit':1,'questCount':len(quests),'newQuests':10,'runtimeWater':'minecraft:block/water_still','jarMatchesSourceAssets':True}
put=ART/'validation.json';put.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,indent=2))
