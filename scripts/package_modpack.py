"""Build an importable Modrinth pack from verified local artifacts and overrides."""
from pathlib import Path
import hashlib
import json
import zipfile

ROOT=Path(__file__).resolve().parents[1]

def main():
    lock=json.loads((ROOT/'pack/mods.lock.json').read_text(encoding='utf-8-sig'))
    files=[]
    for mod in lock['mods']:
        path=ROOT/'.local/integration-mods'/mod['file']
        data=path.read_bytes()
        assert hashlib.sha512(data).hexdigest()==mod['sha512'],mod['file']
        files.append({'path':'mods/'+mod['file'],'hashes':{'sha512':mod['sha512'],'sha1':hashlib.sha1(data).hexdigest()},
            'env':{'client':'required','server':mod['server']},'downloads':[mod['url']],'fileSize':len(data)})
    shader=json.loads((ROOT/'pack/shader.lock.json').read_text())
    data=(ROOT/'revolib/run/shaderpacks'/shader['file']).read_bytes()
    assert hashlib.sha512(data).hexdigest()==shader['sha512']
    files.append({'path':'shaderpacks/'+shader['file'],'hashes':{'sha512':shader['sha512'],'sha1':hashlib.sha1(data).hexdigest()},
        'env':{'client':'required','server':'unsupported'},'downloads':[shader['url']],'fileSize':len(data)})
    index={'formatVersion':1,'game':'minecraft','versionId':'0.1.31-dev-vacuum-dither',
        'name':'Rassers Revolution: Sky Edition','summary':"Ten MI-backed furnace tiers with adaptive casings, permanent Factory smelting, graded item/fuel hatches, roof vents and night/rain greenhouse LEDs.",
        'files':files,'dependencies':{'minecraft':'1.21.1','neoforge':'21.1.265'}}
    output=ROOT.parent/'Rassers-Revolution-Sky-Edition-0.1.31-dev.mrpack'
    jars=sorted((ROOT/'build/mods').glob('*.jar'))
    assert len(jars)==7
    assert (ROOT/'pack/overrides/config/fancymenu/assets/skyblock_loop.mp4').stat().st_size>1_000_000
    with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as z:
        z.writestr('modrinth.index.json',json.dumps(index,indent=2)+'\n')
        for path in sorted((ROOT/'pack/overrides').rglob('*')):
            if path.is_file() and path.name not in ['.gitkeep','README.md']:
                z.write(path,'overrides/'+path.relative_to(ROOT/'pack/overrides').as_posix())
        for path in jars:
            with zipfile.ZipFile(path) as jar:
                assert not any(any(marker in n for marker in ['IndustrialGridUiQa','ChapterOneGameTests','ChapterOneUiQa','CinematicCapture','DrillInterfaceQa','DrillGameTests','drill_test.nbt','FarmUiQa','FarmGameTests','FarmLightingGameTests','FurnaceGameTests','FurnaceUiQa','furnace_test.nbt','SmallGameTests','SmallUiQa','farm_test.nbt','AchievementsUiQa','AchievementsGameTests','achievements_test.nbt']) for n in jar.namelist())
            z.write(path,'overrides/mods/'+path.name)
        # A clean new instance needs the bundled font explicitly selected.
        z.writestr('overrides/options.txt','resourcePacks:["vanilla","mod_resources","file/revo-departure-mono","file/revo-industrial-grid"]\nguiScale:0\n')
        z.write(ROOT/'docs/SKYBLOCK-AND-MENU.md','overrides/REVO-README.md')
        z.write(ROOT/'docs/POWER-CONVERSION.md','overrides/POWER-CONVERSION.md')
        z.write(ROOT/'docs/INDUSTRIAL-GRID.md','overrides/INDUSTRIAL-GRID.md')
        z.write(ROOT/'docs/FIRST-CHAPTER.md','overrides/FIRST-CHAPTER.md')
        z.write(ROOT/'docs/CREATE-IRON-STEAM.md','overrides/CREATE-IRON-STEAM.md')
        z.write(ROOT/'docs/QUEST-BOOK.md','overrides/QUEST-BOOK.md')
        z.write(ROOT/'docs/STEEL-DRILL.md','overrides/STEEL-DRILL.md')
        z.write(ROOT/'docs/METAL-FARMS.md','overrides/METAL-FARMS.md')
        z.write(ROOT/'docs/REVO-FURNACES.md','overrides/REVO-FURNACES.md')
        z.write(ROOT/'docs/REVO-VACUUMS.md','overrides/REVO-VACUUMS.md')
        z.write(ROOT/'docs/REVO-ACHIEVEMENTS.md','overrides/REVO-ACHIEVEMENTS.md')
        z.write(ROOT/'docs/SINGLE-BLOCK-CULTIVATION.md','overrides/SINGLE-BLOCK-CULTIVATION.md')
    with zipfile.ZipFile(output) as z:
        assert z.testzip() is None
        assert len(json.loads(z.read('modrinth.index.json'))['files'])==len(lock['mods'])+1
    print(f'Created {output} ({output.stat().st_size:,} bytes).')

if __name__=='__main__':main()
