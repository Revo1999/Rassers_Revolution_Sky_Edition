"""Find near-coplanar surfaces missed by the exact-plane audit, including inset details."""
from pathlib import Path
from collections import defaultdict
import json,itertools
import numpy as np
from audit_model_planes import ROOT,faces,ordered,intersect,area

def near_faces(fs,tolerance=.08):
 groups=defaultdict(list);hits=[]
 for face in fs:groups[tuple(np.round(face['n'],5))].append(face)
 for group in groups.values():
  group.sort(key=lambda f:f['plane'])
  for i,a in enumerate(group):
   for b in group[i+1:]:
    gap=b['plane']-a['plane']
    if gap>tolerance:break
    if a['index']==b['index']:continue
    axes=[j for j in range(3) if j!=int(np.argmax(np.abs(a['n'])))];p=ordered(a['p'][:,axes]);q=ordered(b['p'][:,axes])
    if np.any(np.minimum(p.max(0),q.max(0))-np.maximum(p.min(0),q.min(0))<=1e-6):continue
    overlap=intersect(p,q)
    if overlap>1e-5:hits.append({'a':[a['index'],a['name'],a['face']],'b':[b['index'],b['name'],b['face']],'gap':round(gap,6),'area':round(overlap,6),'a_area':round(area(p),6),'b_area':round(area(q),6)})
 return hits

def main():
 rows=[]
 for path in sorted(ROOT.glob('revo*/src/main/resources/assets/*/models/**/*.json')):
  data=json.loads(path.read_text(encoding='utf-8-sig'))
  if not data.get('elements'):continue
  hits=near_faces(faces(data))
  if hits:rows.append({'file':path.relative_to(ROOT).as_posix(),'hits':hits})
 dest=ROOT/'.local/machine-near-surfaces.json';dest.write_text(json.dumps(rows,indent=2)+'\n')
 for row in rows:print(row['file'],len(row['hits']))
 print('Total',sum(len(r['hits']) for r in rows),'near-surface pairs in',len(rows),'models')
if __name__=='__main__':main()
