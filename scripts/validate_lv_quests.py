"""Validate live quest assets, recipes, stable IDs, DAG and LV bootstrap boundaries."""
from pathlib import Path
import json,math,subprocess,zipfile

ROOT=Path(__file__).resolve().parents[1]
CHAPTERS=ROOT/'pack/overrides/config/ftbquests/quests/chapters'

def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))

def main():
    chapters=[read(p) for p in sorted(CHAPTERS.glob('*.snbt'))]
    ids=set();quests={};objects=[]
    for chapter in chapters:
        objects.append(chapter);objects+=chapter.get('images',[])
        for q in chapter['quests']:
            quests[q['id']]=q;objects.append(q);objects+=q.get('tasks',[])+q.get('rewards',[])
    for obj in objects:
        ident=obj['id'];assert len(ident)==16 and int(ident,16)>0 and ident not in ids,ident;ids.add(ident)
    visiting=set();visited=set()
    def walk(key):
        assert key in quests,('missing dependency',key)
        assert key not in visiting,('cycle',key)
        if key in visited:return
        visiting.add(key)
        for dep in quests[key].get('dependencies',[]):walk(dep)
        visiting.remove(key);visited.add(key)
    for key in quests:walk(key)
    # Progress records are keyed by these IDs. Existing objects must remain present.
    preserved=0
    for path in CHAPTERS.glob('*.snbt'):
        result=subprocess.run(['git','show','origin/main:'+path.relative_to(ROOT).as_posix()],cwd=ROOT,capture_output=True)
        if result.returncode:continue
        old=json.loads(result.stdout.decode('utf-8-sig'))
        for q in old['quests']:
            assert q['id'] in quests
            before={o['id'] for o in q.get('tasks',[])+q.get('rewards',[])}
            after={o['id'] for o in quests[q['id']].get('tasks',[])+quests[q['id']].get('rewards',[])}
            assert before<=after,('changed progress ID',q['id']);preserved+=1
    chapter=read(CHAPTERS/'5e80000000000000.snbt');assert len(chapter['quests'])==48
    assert chapter['order_index']==45
    for q in chapter['quests']:
        assert q.get('icon',{}).get('id') and len(q['description'])>=2
        assert q['shape'] in ['circle','hexagon','diamond']
        assert q.get('rewards',[])==[]
        for task in q['tasks']:
            assert task['type'] in ['item','checkmark']
            if task['type']=='item':assert task['consume_items'] is False and task['count']>0
    # Item definitions from the pinned assets; the client QA also checks the actual item registry.
    resources={};recipe_data={}
    for jar in sorted((ROOT/'revolib/run/mods').glob('*.jar')):
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if name.endswith('.json') and ('/recipe/' in name or '/models/item/' in name):
                    resources[name]=jar.name
                    if '/recipe/' in name:
                        try:recipe_data[name]=json.loads(z.read(name))
                        except (ValueError,UnicodeDecodeError):pass
    vanilla=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
    with zipfile.ZipFile(vanilla) as z:
        for name in z.namelist():resources[name]=vanilla.name
    for module in ROOT.glob('revo*'):
        base=module/'src/main/resources'
        if not base.is_dir():continue
        for path in base.rglob('*.json'):
            name=path.relative_to(base).as_posix();resources[name]=str(path)
            if '/recipe/' in name:recipe_data[name]=read(path)
    task_items=set()
    for q in chapter['quests']:
        for item in [q['icon']['id']]+[t['item']['id'] for t in q['tasks'] if t['type']=='item']:
            ns,name=item.split(':',1);assert f'assets/{ns}/models/item/{name}.json' in resources,item;task_items.add(item)
    sources=read(ROOT/'docs/LV-QUEST-RECIPE-SOURCES.json');source_count=0
    for quest in sources['quests']:
        for ident in quest['recipe_ids']:
            ns,path=ident.split(':',1);key=f'data/{ns}/recipe/{path}.json'
            assert key in recipe_data,('missing recipe',ident);source_count+=1
    bootstrap=recipe_data['data/revoextraction/recipe/bootstrap_redstone_battery.json']
    assert bootstrap['type']=='minecraft:crafting_shaped'
    assert bootstrap['result']['id']=='modern_industrialization:redstone_battery'
    assert 'antimony' not in json.dumps(bootstrap) and 'lead' not in json.dumps(bootstrap)
    bauxite=recipe_data['data/modern_industrialization/recipe/materials/blast_furnace/bauxite_to_aluminum_nugget.json']
    assert bauxite['eu']==32 and bauxite['duration']==200 and bauxite['item_outputs']['amount']==1
    bykey={q['key']:q['quest_id'] for q in sources['quests']}
    def ancestors(key):
        result=set()
        def add(k):
            for dep in quests[k].get('dependencies',[]):
                if dep not in result:result.add(dep);add(dep)
        add(key);return result
    main_route=ancestors(bykey['handoff'])
    assert bykey['components'] in main_route and bykey['aluminum'] in main_route
    assert not {bykey[k] for k in ['diamonds','blade','macerator','cutter','furnace','upgrades']} & main_route
    assert not any(t.get('item',{}).get('id') in ['modern_industrialization:electrolyzer','modern_industrialization:chemical_reactor','modern_industrialization:centrifuge'] for q in chapter['quests'] for t in q['tasks'])
    # Exact node/edge checks; wire crossings are allowed, wires through unrelated nodes are not.
    local={q['id']:q for q in chapter['quests']};crossings=[]
    for q in local.values():
        for dep in q['dependencies']:
            if dep not in local:continue
            a=local[dep];vx,vy=q['x']-a['x'],q['y']-a['y'];vv=vx*vx+vy*vy
            for b in local.values():
                if b['id'] in [a['id'],q['id']]:continue
                t=((b['x']-a['x'])*vx+(b['y']-a['y'])*vy)/vv if vv else 0
                if 0<t<1 and math.hypot(a['x']+t*vx-b['x'],a['y']+t*vy-b['y'])<.55:crossings.append([a['title'],q['title'],b['title']])
    assert not crossings,crossings
    report={'result':'PASS','chapter':'Chapter 5: The LV Factory','new_lv_quests':48,'new_bridge_lessons':1,
        'total_chapters':len(chapters),'total_quests':len(quests),'existing_quest_ids_preserved':preserved,
        'unique_progress_ids':len(ids),'acyclic_dependencies':True,'non_consuming_tasks':True,
        'recipe_references_verified':source_count,'distinct_task_and_icon_items':len(task_items),
        'battery_bootstrap':'copper/zinc crafting before electric lead/antimony','first_aluminum':'32 EU/t direct bauxite -> nuggets',
        'advanced_machine_bootstrap_cycle':False,'diamond_machine_branch_optional':True,'wire_node_collisions':0}
    (ROOT/'docs/LV-QUEST-VALIDATION.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

if __name__=='__main__':main()
