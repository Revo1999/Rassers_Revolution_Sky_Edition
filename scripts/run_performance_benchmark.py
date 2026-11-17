"""Alternating full-client runs with a fixed heap and isolated factory save."""
from pathlib import Path
import argparse, ctypes, json, subprocess, sys, time

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'.local/performance-benchmark'

def main():
    # Keep an unattended comparison from being interrupted by Windows idle sleep.
    # The execution-state request ends automatically when this process exits.
    if sys.platform=='win32':
        if not ctypes.windll.kernel32.SetThreadExecutionState(0x80000003):
            raise OSError('Could not prevent idle sleep during the benchmark')
    parser=argparse.ArgumentParser();parser.add_argument('--runs',nargs='+',default=['warmup','baseline-1','lithium-1','both-1','baseline-2','both-2','lithium-2']);args=parser.parse_args()
    results=[]
    for label in args.runs:
        if not all(c.isalnum() or c=='-' for c in label):raise ValueError('Invalid run name')
        lithium=label.startswith(('lithium','both'));dynamic=label.startswith('both')
        prepare=[sys.executable,str(ROOT/'scripts/prepare_performance_benchmark.py')]
        if lithium:prepare.append('--lithium')
        if dynamic:prepare.append('--dynamic')
        subprocess.run(prepare,cwd=ROOT,check=True)
        print('START',label,flush=True)
        out=BASE/'results'/label;out.mkdir(parents=True,exist_ok=True)
        if (out/'result.json').exists() or (out/'failure.json').exists():raise RuntimeError('Use a fresh run label: '+label)
        start=time.monotonic()
        with (BASE/(label+'.log')).open('w',encoding='utf-8') as log:
            process=subprocess.Popen(['cmd.exe','/c',str(ROOT/'dev.cmd'),':revolib:runPerformanceBenchmark','-PrevoPerformanceBenchmark='+label,'--console=plain'],cwd=ROOT,stdout=log,stderr=subprocess.STDOUT)
            try:code=process.wait(timeout=720)
            except subprocess.TimeoutExpired:
                (out/'stop-request').touch()
                try:process.wait(timeout=20)
                except subprocess.TimeoutExpired:subprocess.run(['taskkill','/PID',str(process.pid),'/T','/F'],stdout=log,stderr=log)
                code=-1
        path=out/'result.json'
        row=json.loads(path.read_text()) if path.exists() else {'success':False,'error':(out/'failure.json').read_text() if (out/'failure.json').exists() else 'No result; inspect '+label+'.log'}
        row.update({'label':label,'dynamicResources':dynamic,'processExit':code,'taskWallSeconds':time.monotonic()-start,'excludedWarmup':label.startswith('warmup')});results.append(row)
        (BASE/'summary.json').write_text(json.dumps(results,indent=2)+'\n')
        print('RESULT',label,json.dumps(row),flush=True)
        if not row.get('success') and label.startswith(('baseline','warmup')):raise RuntimeError('Baseline failed; cannot compare candidates')
    print('COMPLETE',BASE/'summary.json',flush=True)

if __name__=='__main__':main()
