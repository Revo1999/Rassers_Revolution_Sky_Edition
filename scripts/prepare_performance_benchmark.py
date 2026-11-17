"""Prepare an isolated, fully pinned client; no player instance/save is modified."""
from pathlib import Path
import argparse, hashlib, json, shutil, urllib.request

ROOT=Path(__file__).resolve().parents[1]
INSTANCE=ROOT/'.local/performance-benchmark/instance'

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--lithium',action='store_true');parser.add_argument('--dynamic',action='store_true');args=parser.parse_args()
    INSTANCE.mkdir(parents=True,exist_ok=True)
    for path in (ROOT/'pack/overrides').rglob('*'):
        if path.is_file() and path.name not in ['.gitkeep','README.md']:
            dest=INSTANCE/path.relative_to(ROOT/'pack/overrides');dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(path,dest)
    mods=INSTANCE/'mods';mods.mkdir(exist_ok=True)
    for mod in json.loads((ROOT/'pack/mods.lock.json').read_text(encoding='utf-8-sig'))['mods']:
        src=ROOT/'.local/integration-mods'/mod['file'];assert hashlib.sha512(src.read_bytes()).hexdigest()==mod['sha512'];shutil.copy2(src,mods/src.name)
    request=urllib.request.Request('https://api.modrinth.com/v2/version/DDUrRVCA',headers={'User-Agent':'RevoPerformanceBenchmark/1.0'})
    version=json.load(urllib.request.urlopen(request));file=next(f for f in version['files'] if f['primary'])
    candidate=ROOT/'.local/candidates'/file['filename'];candidate.parent.mkdir(parents=True,exist_ok=True)
    if not candidate.exists():urllib.request.urlretrieve(file['url'],candidate)
    assert hashlib.sha512(candidate.read_bytes()).hexdigest()==file['hashes']['sha512']
    if args.lithium:shutil.copy2(candidate,mods/candidate.name)
    elif (mods/candidate.name).exists():(mods/candidate.name).unlink()
    shader=json.loads((ROOT/'pack/shader.lock.json').read_text());dst=INSTANCE/'shaderpacks'/shader['file'];dst.parent.mkdir(exist_ok=True);shutil.copy2(ROOT/'revolib/run/shaderpacks'/shader['file'],dst)
    (INSTANCE/'options.txt').write_text('onboardAccessibility:false\npauseOnLostFocus:false\nenableVsync:false\nmaxFps:120\nrenderDistance:8\nsimulationDistance:6\nguiScale:2\nresourcePacks:["vanilla","mod_resources","file/revo-departure-mono","file/revo-industrial-grid"]\n')
    (INSTANCE/'config/modernfix-mixins.properties').write_text('mixin.perf.dynamic_resources='+str(args.dynamic).lower()+'\n')
    (ROOT/'.local/performance-benchmark/lithium-version.json').write_text(json.dumps(version,indent=2))
    print('Prepared isolated benchmark:', 'Lithium' if args.lithium else 'baseline', '+ dynamic models' if args.dynamic else '')

if __name__=='__main__':main()
