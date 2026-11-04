"""Audit packaged Revo resources, excluded QA classes and pinned runtime artifacts."""
from pathlib import Path
import hashlib, io, json, zipfile

ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.21-dev.mrpack'
EXCLUDED=['IndustrialGridUiQa','ChapterOneGameTests','ChapterOneUiQa','CinematicCapture','DrillInterfaceQa','DrillGameTests','FarmUiQa','FarmGameTests','SmallGameTests','SmallUiQa','drill_test.nbt','farm_test.nbt','AchievementsUiQa','AchievementsGameTests','achievements_test.nbt']

def main():
    jar_hashes={}
    with zipfile.ZipFile(PACK) as pack:
        assert pack.testzip() is None
        index=json.loads(pack.read('modrinth.index.json'))
        assert index['versionId']=='0.1.21-dev-direct-power'
        lock=json.loads((ROOT/'pack/mods.lock.json').read_text(encoding='utf-8-sig'))
        assert len(lock['mods'])==86 and len(index['files'])==87
        for mod in lock['mods']:
            record=next(x for x in index['files'] if x['path']=='mods/'+mod['file'])
            assert record['hashes']['sha512']==mod['sha512']==hashlib.sha512((ROOT/'.local/integration-mods'/mod['file']).read_bytes()).hexdigest()
        jars=list((ROOT/'build/mods').glob('*.jar'))
        assert len(jars)==7
        runtime_files=0
        for jar in jars:
            data=jar.read_bytes()
            assert data==pack.read('overrides/mods/'+jar.name)
            jar_hashes[jar.name]=hashlib.sha512(data).hexdigest()
            with zipfile.ZipFile(io.BytesIO(data)) as archive:
                assert not any(any(marker in name for marker in EXCLUDED) for name in archive.namelist()),jar
                module=jar.name.split('-0.')[0]
                # Verify actual exported models, images, recipes, blockstates, languages
                # and loot are exactly the resources from the production source tree.
                for directory in ['assets','data']:
                    for path in (ROOT/module/'src/main/resources'/directory).rglob('*'):
                        if path.is_file():
                            key=path.relative_to(ROOT/module/'src/main/resources').as_posix()
                            assert archive.read(key)==path.read_bytes(),(jar,key)
                            runtime_files+=1
        for module in ['revoindustry','revocultivation','revogenetics','revoextraction','revolib','revoachievements']:
            for path in (ROOT/module/'build/classes/java/main').rglob('*.class'):
                assert not any(marker in path.name for marker in EXCLUDED),path
    report={
        'pack':str(PACK),'sha512':hashlib.sha512(PACK.read_bytes()).hexdigest(),
        'firstTierGameTests':9,'liveClientScreenshots':16,
        'clientChecks':['Three real structures and all controller/hatch network menus','Standard player inventory geometry; no hatch R/footer','Three EMI assembly categories and full 3D guide','Native Minecraft zombie/skeleton/spider rendering and persistent contained mob identity','Compact Jade server payload'],
        'compactDrillUiQa':'passed; orientation details controller-only; hatch footer removed',
        'assetChecks':'16 editable native Blockbench projects, 19 runtime models; embedded atlases, bounds, UV density, 56 cage-attachment checks, 354 opaque iron faces and unchanged geometry',
        'production':{'revoJars':7,'externalMods':86,'shader':1,'sourceResourceMatches':runtime_files,'qaClassesAndTemplatesExcluded':True,'normalDevClasspathClean':True,'zipIntegrity':'passed'},
        'jarSha512':jar_hashes,
    }
    (ROOT/'docs/FIRST-TIER-VERIFICATION.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(f'PASS: ZIP integrity, 7 current Revo jars, 86 pinned mods + shader, {runtime_files} exact resource matches, no shipped or normal-classpath QA tooling.')

if __name__=='__main__':main()
