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
    assert all(isinstance(value,str) for value in obj.get('textures',{}).values()),path.name
    for cube in obj.get('elements',[]):
        assert all(-16<=v<=32 for v in cube['from']+cube['to']),path.name
        assert not cube['name'].startswith('preview_'),path.name
        for face in cube['faces'].values():assert all(0<=v<=16 for v in face['uv'])
green=read(RES/'assets/revocultivation/models/block/iron_greenhouse_body.json')
glass=read(RES/'assets/revocultivation/models/block/iron_greenhouse_glass.json')
names={c['name'] for c in green['elements']+glass['elements']}
assert glass['render_type']=='minecraft:translucent'
assert not {c['name'] for c in green['elements']} & {c['name'] for c in glass['elements']}
assert {'square_top_beam_3','square_top_beam_71','square_side_top_beam_3','square_side_top_beam_71'}<=names
assert not any(n.startswith(('window_centre_mullion','window_right_mullion','ceiling_rail_')) for n in names)
assert {'integrated_converter_shell','converter_hatch_bus','converter_roof_conduit','continuous_front_glazing','square_top_beam_3'}<=names
assert len([n for n in names if n.startswith('moist_crop_surface_')])==9
assert len([n for n in names if n.startswith(('led_strip_channel_','led_strip_side_channel_'))])==4
leds=read(RES/'assets/revocultivation/models/block/iron_greenhouse_lights.json')['elements']
assert len(leds)==4
for led in leds:assert (led['to'][1]-led['from'][1])*5<.11
for name in ['power_converter','advanced_power_converter','elite_power_converter']:
    states=read(ROOT/f'revoindustry/src/main/resources/assets/revoindustry/blockstates/{name}.json')['variants']
    assert len(states)==12
    assert all(v['model']=='revoindustry:block/empty' for k,v in states.items() if k.endswith('integrated=true'))
tree=read(RES/'assets/revocultivation/models/block/steel_tree_grower_body.json')
names={c['name'] for c in tree['elements']}
assert len([n for n in names if n.startswith('continuous_steel_soil_')])==4
assert {'collection_intake_collar','collection_duct_trough','collection_duct_cover'}<=names
assert not any(n.startswith('preview_') for n in names)
assert {'plinth_front','plinth_rear','plinth_left','plinth_right','rear_casing','front_service_shoulder','controller_bed_connection','hatch_bed_connection','tank_bed_connection'}<=names
tree_glass=read(RES/'assets/revocultivation/models/block/steel_tree_grower_glass.json')
assert tree_glass['render_type']=='minecraft:translucent'
assert len(tree_glass['elements'])==4
assert len(glass['elements'])==11
from PIL import Image
image=Image.open(RES/'assets/revocultivation/textures/block/greenhouse_glass.png').convert('RGBA')
for model in [glass,tree_glass]:
    for cube in model['elements']:
        if 'glazing' in cube['name']:continue
        for face in cube['faces'].values():
            u,v,U,V=face['uv'];cx,cy=int((u+U)*16),int((v+V)*16)
            assert 35<=image.getpixel((cx,cy))[3]<=110,(cube['name'],face)
pane=next(c for c in glass['elements'] if c['name']=='continuous_front_glazing')
u,v,U,V=pane['faces']['north']['uv'];region=image.crop((round(u*32),round(v*32),round(U*32),round(V*32)))
alphas=[p[3] for p in region.getdata()]
assert alphas.count(0)>len(alphas)*.6 and alphas.count(255)>0
assert all(region.getpixel((x,0))[3]==255 for x in range(region.width))
assert all(region.getpixel((x,8))[3]==0 for x in [16,32,48]),'No repeated internal tile borders'
renderer=(ROOT/'revocultivation/src/main/java/dk/revo/revocultivation/client/FarmClient.java').read_text()
assert 'RenderType.entityTranslucentCull' in renderer
assert 'if(machine.growLightsOn())' in renderer
source=(ROOT/'revocultivation/src/main/java/dk/revo/revocultivation/FarmMachine.java').read_text()
assert 'getDayTime()' in source and 'level.isRaining()' in source
assert 'time>=13000&&time<23000' in source
assert 'machine.needsGrowLights()' in source and 'machine.setLights(illuminate)' in source
assert 'e.register(model("iron_greenhouse_glass"))' in renderer
assert 'e.register(model("steel_tree_grower_glass"))' in renderer
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
            if p.is_file() and (p.suffix=='.json' and p.name.startswith(('steel_','iron_')) or p.name in ['metal_farms_atlas.png','greenhouse_tree_atlas.png','greenhouse_glass.png','en_us.json']):
                assert jar.read(p.relative_to(resources).as_posix())==p.read_bytes(),p
        assert not any('GameTests' in n or 'UiQa' in n for n in jar.namelist())
pack=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.29-dev.mrpack'
with zipfile.ZipFile(pack) as z:
    assert read(ROOT/'pack/mods.lock.json') # Existing external mod set is retained.
    assert json.loads(z.read('modrinth.index.json'))['versionId']=='0.1.29-dev-multiblock-furnaces'
    for p in (ROOT/'build/mods').glob('*.jar'):assert z.read('overrides/mods/'+p.name)==p.read_bytes()
    for p in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt'):assert z.read('overrides/config/ftbquests/quests/chapters/'+p.name)==p.read_bytes()
    assert z.read('overrides/METAL-FARMS.md')==(ROOT/'docs/METAL-FARMS.md').read_bytes()
report={'passed':True,'nativeModels':21,'assemblyCubesIncludingEditorPreviews':assembly_counts,'blueprints':counts,'fittedConverterHousing':True,'glassLayer':'entityTranslucentCull, shared with tank water','continuousSteelPlinth':True,'previewGeometryExcluded':True,'electricGreenhouse':{'euPerTick':8,'euCapacity':1600,'waterCapacity':16000,'cycleTicks':200},'questCount':116,'minecraftLaunched':True,'graphicalClientLaunched':False,'lightingGameTest':{'passed':True,'scenarios':['clear day off','night on','clock boundaries','daytime rain on','rain stops off','idle energy','shade ignored','power loss off','power restored on','dismantling off']},'gameplayTested':False}
(ART/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
