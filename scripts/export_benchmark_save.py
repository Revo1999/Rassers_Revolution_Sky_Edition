"""Export the isolated benchmark after Minecraft has closed and flushed its save."""
from pathlib import Path
import hashlib, json, shutil, zipfile

ROOT=Path(__file__).resolve().parents[1]
WORLD=ROOT/'.local/performance-benchmark/instance/saves/Revo Performance Benchmark'
DEST=ROOT.parent/'Revo-Performance-Benchmark.zip'

def main():
    assert (WORLD/'level.dat').is_file(), 'Run the full benchmark first'
    with zipfile.ZipFile(DEST,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
        for p in sorted(WORLD.rglob('*')):
            if p.is_file() and p.name not in ['session.lock'] and not p.name.endswith('.log'):
                archive.write(p,'Revo Performance Benchmark/'+p.relative_to(WORLD).as_posix())
    with zipfile.ZipFile(DEST) as archive:assert archive.testzip() is None
    manifest={'file':DEST.name,'bytes':DEST.stat().st_size,'sha256':hashlib.sha256(DEST.read_bytes()).hexdigest(),'worldFolder':'Revo Performance Benchmark','heapForComparison':'10 GiB','game':'Minecraft 1.21.1 / NeoForge 21.1.265','mode':'creative benchmark, not a survival save'}
    out=ROOT/'art/performance-benchmark';out.mkdir(parents=True,exist_ok=True)
    shutil.copy2(DEST,out/DEST.name)
    (out/'save-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    print('Exported',DEST,manifest['bytes'],'bytes')

if __name__=='__main__':main()
