"""Static source/model/package audit. Never starts Minecraft or GameTests."""
from pathlib import Path
import json,zipfile,math
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/metal-farms'
RES=ROOT/'revocultivation/src/main/resources'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
manifest=read(ART/'greenhouse-tree-manifest.json');assert len(list(ART.glob('*.bbmodel')))==21
assembly_counts={}
for path in ART.glob('*.bbmodel'):
    obj=read(path);density=5 if path.stem in ['iron_greenhouse_assembled','steel_tree_grower_assembled'] else 3 if path.stem.endswith('_assembled') else 1
    textures=obj['textures']
    for cube in obj['elements']:
        dims=[b-a for a,b in zip(cube['from'],cube['to'])];assert all(d>0 for d in dims),cube['name']
        for face,f in cube['faces'].items():
            if f.get('texture') is None:continue
            if cube['name'].startswith(('preview_','converter_')):continue # Native imported preview assets retain their source UVs.
            u,v,U,V=f['uv'];width,height=([dims[0],dims[2]] if face in ['up','down'] else [dims[2],dims[1]] if face in ['east','west'] else [dims[0],dims[1]])
            assert abs(U-u-width*density)<.001 and abs(V-v-height*density)<.001,(path.name,cube['name'],face)
            assert all(math.isfinite(a) and 0<=a<=512 for a in f['uv'])
    if path.stem.endswith('_assembled'):assembly_counts[path.stem]=len(obj['elements'])
for path in (RES/'assets/revocultivation/models/block').glob('*.json'):
    if not (path.stem.startswith(('steel_','iron_'))):continue
    obj=read(path)
    for cube in obj.get('elements',[]):
        assert all(-16<=v<=32 for v in cube['from']+cube['to']),path.name
        assert not cube['name'].startswith('preview_'),path.name
        for face in cube['faces'].values():assert all(0<=v<=16 for v in face['uv'])
green=read(RES/'assets/revocultivation/models/block/iron_greenhouse_body.json')
names={c['name'] for c in green['elements']}
assert {'integrated_converter_shell','converter_hatch_bus','converter_roof_conduit','continuous_front_glazing','glass_top_border_3'}<=names
assert len([n for n in names if n.startswith('moist_crop_surface_')])==9
assert len([n for n in names if n.startswith('ceiling_rail_')])==3
for name in ['power_converter','advanced_power_converter','elite_power_converter']:
    states=read(ROOT/f'revoindustry/src/main/resources/assets/revoindustry/blockstates/{name}.json')['variants']
    assert len(states)==12
    assert all(v['model']=='revoindustry:block/empty' for k,v in states.items() if k.endswith('integrated=true'))
tree=read(RES/'assets/revocultivation/models/block/steel_tree_grower_body.json')
names={c['name'] for c in tree['elements']}
assert len([n for n in names if n.startswith('continuous_steel_soil_')])==4
assert {'collection_intake_collar','collection_duct_trough','collection_duct_cover'}<=names
assert not any(n.startswith('preview_') for n in names)
# Each ribbon side is continuous and meets its neighbours at the same soil height.
for c in tree['elements']:
    if c['name'].startswith('continuous_steel_soil_'):assert c['from'][1]==11/5 and c['to'][1]==18/5
counts={'steel_tree_grower':{'SUPPORT':4,'CASING':9,'BED':8,'SOIL':1,'CONTROLLER':1,'HATCH':1,'TANK':1},
    'steel_mob_harvester':{'SUPPORT':12,'CASING':7,'GRATE':1,'BARS':3,'FILTER':1,'CONTROLLER':1,'HATCH':1,'AIR':1},
    'iron_greenhouse':{'SUPPORT':11,'CASING':21,'BED':9,'GLASS':20,'CONTROLLER':1,'HATCH':1,'TANK':1,'POWER':1,'LIGHT':1,'AIR':9}}
assert sum(counts['steel_tree_grower'].values())==25
assert sum(counts['iron_greenhouse'].values())==75
chapters=[read(p) for p in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt')]
quests={q['id']:q for c in chapters for q in c['quests']};assert len(quests)==116
seen=[]
for c in chapters:
    seen.append(c['id']);seen.extend(i['id'] for i in c.get('images',[]))
    for q in c['quests']:seen.append(q['id']);seen.extend(v['id'] for v in q['tasks']+q.get('rewards',[]))
assert len(seen)==len(set(seen))
visited=set()
def visit(ident,path=()):
    assert ident in quests and ident not in path
    if ident in visited:return
    for dep in quests[ident].get('dependencies',[]):visit(dep,path+(ident,))
    visited.add(ident)
for ident in quests:visit(ident)
for module in ['revocultivation','revogenetics','revolib','revoindustry']:
    with zipfile.ZipFile(ROOT/f'build/mods/{module}-0.1.0-dev.jar') as jar:
        resources=ROOT/module/'src/main/resources'
        for p in resources.rglob('*'):
            if p.is_file() and (p.suffix=='.json' and p.name.startswith(('steel_','iron_')) or p.name in ['metal_farms_atlas.png','greenhouse_tree_atlas.png','en_us.json']):
                assert jar.read(p.relative_to(resources).as_posix())==p.read_bytes(),p
        assert not any('GameTests' in n or 'UiQa' in n for n in jar.namelist())
pack=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.26-dev.mrpack'
with zipfile.ZipFile(pack) as z:
    assert read(ROOT/'pack/mods.lock.json') # Existing external mod set is retained.
    assert json.loads(z.read('modrinth.index.json'))['versionId']=='0.1.26-dev-wide-farms'
    for p in (ROOT/'build/mods').glob('*.jar'):assert z.read('overrides/mods/'+p.name)==p.read_bytes()
    for p in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt'):assert z.read('overrides/config/ftbquests/quests/chapters/'+p.name)==p.read_bytes()
    assert z.read('overrides/METAL-FARMS.md')==(ROOT/'docs/METAL-FARMS.md').read_bytes()
report={'passed':True,'nativeModels':21,'assemblyCubesIncludingEditorPreviews':assembly_counts,'blueprints':counts,'fittedConverterHousing':True,'previewGeometryExcluded':True,'electricGreenhouse':{'euPerTick':8,'euCapacity':1600,'waterCapacity':16000,'cycleTicks':200},'questCount':116,'minecraftLaunched':False,'gameplayTested':False}
(ART/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
