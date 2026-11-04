"""Verify the shipped wardrobe, catalogue, quest bindings and completed integration checks."""
from pathlib import Path
import hashlib,json,zipfile

ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'
RES=ROOT/'revoachievements/src/main/resources'
BAD=['AchievementsUiQa','AchievementsGameTests','achievements_test.nbt']

def main():
    entries={}
    for path in (RES/'data/revoachievements/revo_cosmetics').rglob('*.json'):
        key='revoachievements:'+path.relative_to(RES/'data/revoachievements/revo_cosmetics').with_suffix('').as_posix()
        entry=json.loads(path.read_text());assert key not in entries;entries[key]=entry
        assert entry['kind'] in ['title','hat'] and 0<len(entry['name'])<=64
    assert len(entries)==17 and sum(e['kind']=='title' for e in entries.values())==9
    chapter_path=ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e70000000000000.snbt'
    chapter=json.loads(chapter_path.read_text());assert len(chapter['quests'])==5
    ids=[chapter['id']]
    for quest in chapter['quests']:
        ids.append(quest['id']);assert len(quest['rewards'])==2
        for task in quest['tasks']:
            ids.append(task['id'])
            if task['type']=='item':assert task['consume_items'] is False
        for reward in quest['rewards']:
            ids.append(reward['id']);entry=entries[reward['cosmetic']]
            assert reward['type']=='revoachievements:'+entry['kind']
            assert reward['team_reward'] is False and reward['auto']=='enabled'
    assert len(set(ids))==len(ids) and all(len(i)==16 and int(i,16)>0 for i in ids)
    names=(ROOT/'pack/overrides/config/playernamestyler-common.toml').read_text();assert names.count('%revo_title%')==4 # comment + chat/tab/nameplate
    tests=(ROOT/'.local/achievements-gametests.log').read_text(encoding='utf-8-sig')
    assert 'All 8 required tests passed' in tests and 'BUILD SUCCESSFUL' in tests
    for name in ['achievements-client-qa.log','achievements-reload-qa.log','achievements-production-build.log']:
        log=(ROOT/'.local'/name).read_text(encoding='utf-8-sig')
        assert 'BUILD SUCCESSFUL' in log,name
        assert 'Wardrobe QA server check failed' not in log and 'Title missing from tab-name formatter' not in log,name
    results=(ROOT/'art/revoachievements/ingame/qa-results.txt').read_text()
    for phrase in ['real FTB task completion','independent title/hat equip','real PlayerName Styler','world player hat layer','second client/world launch retained','inventory R button, Back navigation, non-op command, owned filter, paging and search']:
        assert phrase in results,phrase
    assert 'FAIL:' not in results
    jar=ROOT/'build/mods/revoachievements-0.1.0-dev.jar'
    with zipfile.ZipFile(jar) as z:
        assert not any(any(marker in name for marker in BAD) for name in z.namelist())
        assert not any(name.startswith(('dev/ftb/','sierra/','fonnymunkey/')) for name in z.namelist()),'Bundled third-party code'
        for path in RES.rglob('*'):
            if path.is_file() and path.suffix=='.json':assert z.read(path.relative_to(RES).as_posix())==path.read_bytes(),path
        assert 'dk/revo/revoachievements/client/WardrobeScreen.class' in z.namelist()
        assert 'dk/revo/revoachievements/compat/FtbCosmeticRewards$TitleReward.class' in z.namelist()
    for path in (ROOT/'revoachievements/build/classes/java/main').rglob('*.class'):
        assert not any(marker in path.name for marker in BAD),path
    with zipfile.ZipFile(PACK) as z:
        assert z.testzip() is None
        assert z.read('overrides/mods/'+jar.name)==jar.read_bytes()
        assert z.read('overrides/config/ftbquests/quests/chapters/5e70000000000000.snbt')==chapter_path.read_bytes()
        assert z.read('overrides/config/playernamestyler-common.toml')==(ROOT/'pack/overrides/config/playernamestyler-common.toml').read_bytes()
        assert z.read('overrides/REVO-ACHIEVEMENTS.md')==(ROOT/'docs/REVO-ACHIEVEMENTS.md').read_bytes()
    report={'catalogue':{'titles':9,'hats':8},'exampleQuests':5,'serverGameTests':8,
            'clientChecks':['Actual FTB task/reward claims','Independent server-validated selections and locked previews','PlayerName Styler chat and tab formatting','Native hat layer without changing inventory/armor','Full client/world stop and reload persistence','Keybinding, creative inventory R button, Back, non-op command, filters, search and paging'],
            'screenshots':len(list((ROOT/'art/revoachievements/ingame').glob('*.png'))),
            'productionQaExcluded':True,'thirdPartyCodeBundled':False,'pack':str(PACK),
            'jarSha512':hashlib.sha512(jar.read_bytes()).hexdigest(),'packSha512':hashlib.sha512(PACK.read_bytes()).hexdigest()}
    (ROOT/'docs/REVO-ACHIEVEMENTS-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n')
    print('PASS: 9 titles, 8 hats, 5 real quest bindings, 8 server tests, live UI/restart/access checks, current packaged assets and no shipped QA/third-party classes.')

if __name__=='__main__':main()
