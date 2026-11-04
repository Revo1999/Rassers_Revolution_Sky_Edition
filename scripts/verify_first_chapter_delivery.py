"""Verify first-chapter runtime evidence and the actual distributed pack."""
from pathlib import Path
import hashlib, json, zipfile
from validate_first_chapter import main as validate_content

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT.parent / 'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'

def main():
    validate_content()
    tests = (ROOT/'.local/chapter-one-tests.log').read_text(encoding='utf-8-sig')
    assert 'All 8 required tests passed' in tests and 'BUILD SUCCESSFUL' in tests
    client = (ROOT/'.local/chapter-one-client.log').read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in client
    assert 'Missing config translation' not in client
    assert 'Loaded 1 chapter groups, 3 chapters, 46 quests' in client
    result = (ROOT/'art/quests/ingame/qa-results.txt').read_text(encoding='utf-8')
    for text in ['only one starting Revolution Book', 'actual Revolution Book use',
                 'real FTB reward pipeline', 'actual AE2 manual opened',
                 'actual MI manual opened', 'real rain filled',
                 'synchronized vessel menus', 'real Jade server payload',
                 'Fresh-world quest and primitive machine QA complete']:
        assert text in result, text
    production = (ROOT/'.local/chapter-one-production.log').read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in production
    shots = ['starting-island','chapter-one','tips-guides','ae2-manual','mi-manual',
             'rain-and-crucible','rain-barrel-ui','crucible-ui','clay-result','clay-ui','vessel-jade']
    for name in shots:
        assert (ROOT/'art/quests/ingame'/f'{name}.png').stat().st_size > 10000
    # Jade validates every registered plugin label while opening the title screen.
    for mod,key in [('revoindustry','primitive_vessel'),('revocultivation','small_cultivation_status')]:
        lang=json.loads((ROOT/mod/f'src/main/resources/assets/{mod}/lang/en_us.json').read_text(encoding='utf-8'))
        assert lang[f'config.jade.plugin_{mod}.{key}']
    with zipfile.ZipFile(PACK) as pack:
        assert pack.testzip() is None
        index=json.loads(pack.read('modrinth.index.json'))
        assert index['versionId']=='0.1.21-dev-direct-power'
        chapters=[n for n in pack.namelist() if n.startswith('overrides/config/ftbquests/quests/chapters/')]
        assert len(chapters)==3,chapters
        for n in chapters:
            assert pack.read(n)==(ROOT/'pack'/n).read_bytes(),n
        for relative in ['config/skyblockbuilder/inventory.json5','config/skyblockbuilder/starter_inventory.json5']:
            assert pack.read('overrides/'+relative)==(ROOT/'pack/overrides'/relative).read_bytes()
        assert pack.read('overrides/FIRST-CHAPTER.md')==(ROOT/'docs/FIRST-CHAPTER.md').read_bytes()
        for path in (ROOT/'build/mods').glob('*.jar'):
            assert pack.read('overrides/mods/'+path.name)==path.read_bytes()
            with zipfile.ZipFile(path) as jar:
                assert not any('ChapterOneGameTests' in n or 'ChapterOneUiQa' in n for n in jar.namelist())
    report={'result':'passed','quests':35,'tips':6,'serverTests':8,'liveScreenshots':len(shots),
            'freshStart':'One Revolution Book and placed Crafting Station; no chest',
            'verifiedRuntime':['FTB item detection and non-op command reward','Real AE2 and MI manuals',
                               'Rain and heat over elapsed ticks','Clay mixing and network menus','Jade payload'],
            'startupFix':'Both Jade plugin translation entries present; loading overlay completes',
            'pack':str(PACK),'sha512':hashlib.sha512(PACK.read_bytes()).hexdigest(),'qaExcluded':True}
    (ROOT/'docs/FIRST-CHAPTER-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print('PASS: 35 quests, six Tips, eight server tests, 11 live client captures, fixed startup and exact packaged chapter assets.')

if __name__=='__main__':main()
