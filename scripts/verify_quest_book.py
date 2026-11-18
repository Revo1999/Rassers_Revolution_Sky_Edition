"""Check quest IDs, dependency cycles, native item references and stable saved progress."""
from pathlib import Path
from collections import Counter
import hashlib,json,subprocess,zipfile

ROOT=Path(__file__).resolve().parents[1]
CHAPTERS=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
NEW={'5ec0000000000000','5ed0000000000000','5ea0000000000000','5eb0000000000000'}
def verify():
    chapters=[json.loads(p.read_text(encoding='utf-8')) for p in sorted(CHAPTERS.glob('*.snbt'))]
    seen={};quests={};newquests=[]
    def record(value,where):
        assert len(value)==16 and all(c in '0123456789ABCDEF' for c in value),value
        assert value not in seen,(value,seen.get(value),where);seen[value]=where
    for c in chapters:
        record(c['id'],c['title'])
        for image in c.get('images',[]):record(image['id'],c['title']+' banner')
        positions=[]
        for q in c['quests']:
            record(q['id'],q['title']);quests[q['id']]=q
            if c['filename'] in NEW:newquests.append(q)
            for task in q['tasks']:record(task['id'],q['title']+' task')
            for reward in q.get('rewards',[]):record(reward['id'],q['title']+' reward')
            if c['filename'] in NEW:
                assert q['description'] and q['tasks'] and not q['rewards']
                assert q['shape'] in ['hexagon','circle','diamond'] and q['icon']['id']
                for x,y,title in positions:assert (x-q['x'])**2+(y-q['y'])**2>=2.5**2,(title,q['title'])
                positions.append((q['x'],q['y'],q['title']))
                for t in q['tasks']:
                    assert t['type'] in ['item','checkmark']
                    if t['type']=='item':assert t['consume_items'] is False and t['count']>0
    active=set();done=set()
    def visit(ident):
        assert ident in quests,'Missing dependency '+ident
        assert ident not in active,'Dependency cycle at '+ident
        if ident in done:return
        active.add(ident)
        for dep in quests[ident].get('dependencies',[]):visit(dep)
        active.remove(ident);done.add(ident)
    for ident in quests:visit(ident)
    # Compare old quest/task/reward IDs with committed files, never saved player data.
    preserved=0
    for c in chapters:
        if c['filename'] in NEW:continue
        path=f"pack/overrides/config/ftbquests/quests/chapters/{c['filename']}.snbt"
        before=json.loads(subprocess.check_output(['git','show','origin/main:'+path],cwd=ROOT).decode('utf-8'))
        current={q['id']:q for q in c['quests']}
        for old in before['quests']:
            assert old['id'] in current
            for field in ['tasks','rewards']:
                assert {e['id'] for e in old.get(field,[])}=={e['id'] for e in current[old['id']].get(field,[])}
            preserved+=1
    resources=set()
    for mod in ROOT.glob('revo*'):
        for p in (mod/'src/main/resources/assets').glob('*/models/item/*.json'):
            resources.add(p.parts[-4]+':'+p.stem)
    jars=list((ROOT/'revolib/run/mods').glob('*.jar'))
    hint=ROOT/'.local/quest-minecraft-assets.txt'
    if hint.exists():jars.append(Path(hint.read_text().strip()))
    jars+=list(Path('C:/Users/victo/.gradle/caches/neoformruntime/intermediate_results').glob('stripClient*_resourcesOutput.jar'))
    for jar in jars:
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                parts=n.split('/')
                if len(parts)==5 and parts[0]=='assets' and parts[2:4]==['models','item'] and n.endswith('.json'):resources.add(parts[1]+':'+parts[-1][:-5])
    for q in newquests:
        for item in [q['icon']['id'],*[t['item']['id'] for t in q['tasks'] if t['type']=='item']]:
            assert item in resources,'Unknown item/model reference '+item+' in '+q['title']
            assert 'quarry' not in item and 'nexus' not in item
    addedSedimentQuests=['5E4000000000000E','5E5000000000001A','5E5000000000001B','5E5000000000001C']
    for ident in addedSedimentQuests:
        q=quests[ident]
        for item in [q['icon']['id'],*[t['item']['id'] for t in q['tasks'] if t['type']=='item']]:assert item in resources,'Unknown sediment quest item '+item
    assert len(newquests)==83
    assert '5EC0000000000015' in quests['5E50000000000014']['dependencies']
    # Crucial bootstrap ordering: EBF before aluminum/electrolyzer, never the reverse.
    def ancestors(ident):
        result=set()
        for d in quests[ident].get('dependencies',[]):result.add(d);result.update(ancestors(d))
        return result
    assert '5EC0000000000007' in ancestors('5EC000000000000A')
    assert '5EC000000000000A' in ancestors('5EC0000000000016')
    assert '5EC0000000000016' not in ancestors('5EC0000000000007')
    # First freezer cannot depend on the stainless/coil materials it must cool.
    assert '5EB000000000000B' not in ancestors('5EB0000000000008')
    assert '5EB000000000000E' not in ancestors('5EB000000000000D')
    assert '5EB000000000000E' in ancestors('5EB0000000000010')
    result={'chapters':len(chapters),'quests':len(quests),'newQuests':83,'sedimentAndDrillQuestsVerified':4,'existingQuestsPreserved':preserved,'uniqueIds':len(seen),'acyclicDependencies':True,'newItemReferencesResolved':True,'nonConsumingItemTasks':True,'newMaterialRewards':0,'fluidTasks':'explicit self-reported commissioning checkmarks','liveSurvivalPlaythrough':'not performed','chaptersSummary':[{'title':c['title'],'quests':len(c['quests'])} for c in chapters]}
    out=ROOT/'art/quests';out.mkdir(parents=True,exist_ok=True);(out/'validation.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,indent=2))
if __name__=='__main__':verify()
