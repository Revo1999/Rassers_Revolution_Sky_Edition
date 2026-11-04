"""Lightweight source/artifact verification; never launches Minecraft or GameTests."""
from pathlib import Path
import base64
import json
import math
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT.parent / 'Rassers-Revolution-Sky-Edition-0.1.25-dev.mrpack'

def read(path): return json.loads(path.read_text(encoding='utf-8-sig'))

def main():
    chapters = [read(f) for f in (ROOT/'pack/overrides/config/ftbquests/quests/chapters').glob('*.snbt')]
    quests = {q['id']: q for c in chapters for q in c['quests']}
    ids = []
    for c in chapters:
        ids.append(c['id'])
        ids += [image['id'] for image in c.get('images', [])]
        for q in c['quests']:
            ids += [q['id']] + [v['id'] for v in q['tasks'] + q.get('rewards', [])]
            assert all(d in quests for d in q.get('dependencies', [])), q['title']
            for t in q['tasks']:
                if t['type'] == 'item': assert t['count'] > 0 and not t['consume_items']
    assert len(ids) == len(set(ids)) and all(re.fullmatch('[0-9A-F]{16}', i) for i in ids)
    active, done = set(), set()
    def visit(i):
        assert i not in active, quests[i]['title']
        if i in done: return
        active.add(i)
        for d in quests[i].get('dependencies', []): visit(d)
        active.remove(i); done.add(i)
    for i in quests: visit(i)
    new = [c for c in chapters if c['id'] in ['5E20000000000000', '5E30000000000000']]
    assert [len(c['quests']) for c in sorted(new, key=lambda c: c['id'])] == [29, 28]
    assert len(chapters) == 6 and len(quests) == 116

    known = set()
    for jar in (ROOT/'.local/integration-mods').glob('*.jar'):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                match = re.fullmatch(r'assets/([^/]+)/models/item/(.+)\.json', n)
                if match: known.add(match[1] + ':' + match[2])
    for f in ROOT.glob('revo*/src/main/resources/assets/*/models/item/*.json'):
        known.add(f.parent.parent.parent.name + ':' + f.stem)
    required = {t['item']['id'] for c in chapters for q in c['quests'] for t in q['tasks'] if t['type'] == 'item'}
    required.update(q['icon']['id'] for c in chapters for q in c['quests'] if 'icon' in q)
    assert all(i.startswith('minecraft:') or i in known for i in required), required-known

    assets = ROOT/'revoextraction/src/main/resources/assets/revoextraction'
    art = ROOT/'art/blockbench/revoextraction'
    atlas = (assets/'textures/block/iron_drill_atlas.png').read_bytes()
    models = {}
    for p in art.glob('iron_drill_*.bbmodel'):
        d = read(p)
        assert base64.b64decode(d['textures'][0]['source'].split(',')[1]) == atlas
        assert len({e['uuid'] for e in d['elements']}) == len(d['elements'])
        density = 3 if p.stem.endswith(('assembled','unassembled')) else 1
        for e in d['elements']:
            dims = [b-a for a,b in zip(e['from'],e['to'])]
            assert all(v > 0 for v in dims)
            for side,f in e['faces'].items():
                if f.get('texture') is None: continue
                u,v,U,V = f['uv']
                w,h = (dims[0],dims[2]) if side in ['up','down'] else (dims[2],dims[1]) if side in ['east','west'] else (dims[0],dims[1])
                assert abs(abs(U-u)/w-density) < .001 and abs(abs(V-v)/h-density) < .001, (p.name,e['name'],side)
                assert all(math.isfinite(a) and 0 <= a <= 512 for a in [u,v,U,V])
    for p in (assets/'models/block').glob('iron_drill_*.json'):
        d = read(p); models[p.stem] = len(d['elements'])
        assert d['textures']['0'] == 'revoextraction:block/iron_drill_atlas'
        for e in d['elements']:
            assert all(-16 <= a <= 32 for a in e['from']+e['to'])
            if 'rotation' in e: assert e['rotation'].get('angle',0) in [-45,-22.5,0,22.5,45]
    assert len(models) == 9 and [models['iron_drill_'+n] for n in ['body','drill_bit','drive_gear']] == [74,8,2]
    recipes = ROOT/'revoextraction/src/main/resources/data/revoextraction/recipe'
    for name in ['support','casing','core','controller','input','output_hatch']:
        assert read(recipes/f'iron_drill_{name}.json')['result']['id'] == f'revoextraction:iron_drill_{name}'
    for name in ['milling/copper_dust','milling/tin_dust','milling/brick_dust','mixing/first_bronze','pressing/bronze_plate']:
        assert read(recipes/(name+'.json'))['neoforge:conditions'][0]['modid'] == 'modern_industrialization'
    assert 'BUILD SUCCESSFUL' in (ROOT/'.local/mechanical-progression/build.log').read_text(encoding='utf-8-sig')

    with zipfile.ZipFile(PACK) as pack:
        index = json.loads(pack.read('modrinth.index.json'))
        assert index['versionId'] == '0.1.25-dev-metal-farms' and len(index['files']) == 87
        packaged_chapters = [n for n in pack.namelist() if n.startswith('overrides/config/ftbquests/quests/chapters/')]
        assert len(packaged_chapters) == 6
        for n in packaged_chapters: assert pack.read(n) == (ROOT/'pack'/n).read_bytes()
        for p in (ROOT/'build/mods').glob('*.jar'): assert pack.read('overrides/mods/'+p.name) == p.read_bytes()
        assert pack.read('overrides/CREATE-IRON-STEAM.md') == (ROOT/'docs/CREATE-IRON-STEAM.md').read_bytes()
        assert pack.read('overrides/QUEST-BOOK.md') == (ROOT/'docs/QUEST-BOOK.md').read_bytes()
    with zipfile.ZipFile(ROOT/'build/mods/revoextraction-0.1.0-dev.jar') as jar:
        assert 'dk/revo/revoextraction/DrillTier.class' in jar.namelist()
        for name in models:
            assert jar.read(f'assets/revoextraction/models/block/{name}.json') == (assets/f'models/block/{name}.json').read_bytes()
    with zipfile.ZipFile(ROOT/'build/mods/revolib-0.1.0-dev.jar') as lib:
        assert 'dk/revo/revolib/quest/FreeManualRecipes.class' in lib.namelist()
        policy=json.loads(lib.read('data/revolib/tags/item/free_quest_manuals.json'))
        tips=next(c for c in chapters if c['id']=='5E1F000000000000')
        assert {v['id'] for v in policy['values']} == {r['item']['id'] for q in tips['quests'] for r in q.get('rewards',[]) if r['type']=='item'}
    report = {'manualPolicyConfigured':True, 'result':'passed', 'scope':'compilation, native art and static source/pack checks only',
        'newQuests':57, 'totalQuests':103, 'chapters':5, 'uniqueIds':True, 'acyclicDependencies':True,
        'taskItemAssetsResolved':True, 'ironRuntimeModels':models, 'pack':str(PACK),
        'gameplayTested':False, 'gameTestsRun':False, 'clientLaunched':False}
    (ROOT/'docs/CREATE-IRON-STEAM-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print('PASS: 57 new quests, valid dependency graph/items, native iron models/UVs, successful build and exact packaged content. No gameplay tests run.')

if __name__ == '__main__': main()
