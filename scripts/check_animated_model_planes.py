"""Check assembled renderer parts, supplementing the individual asset plane audit."""
import sys,copy,json,pathlib,numpy as np
from audit_model_planes import faces,audit_faces,ROOT,rotation
D=ROOT/'revoextraction/src/main/resources/assets/revoextraction/models/block'
rows=[]
for tier in ['wooden','iron','steel']:
 parts={p:faces(json.loads((D/f'{tier}_drill_{p}.json').read_text())) for p in ['body','drill_bit','drive_gear']}
 for angle in range(0,360,10):
  fs=[];owners={}
  for part,data in parts.items():
   r=rotation(1,angle) if part=='drill_bit' else rotation(0,-angle) if part=='drive_gear' else np.eye(3);origin=np.array([.5,.375,.5])*16 if part=='drill_bit' else np.array([46/48,24/48,.5])*16
   # Renderer pivot coordinates are in the normalized Java model's 16-unit space.
   for f in copy.deepcopy(data):
    f['p']=origin+(f['p']-origin)@r.T;f['n']=r@f['n'];f['plane']=float(f['p'][0]@f['n']);f['index']=(part,f['index']);owners[f['index']]=part;fs.append(f)
  hits=[h for h in audit_faces(fs) if owners[h['a'][0]]!=owners[h['b'][0]]]
  if hits:rows.append({'tier':tier,'angle':angle,'overlaps':hits})
assert not rows,rows
D=ROOT/'revocultivation/src/main/resources/assets/revocultivation/models/block'
for kind,parts in [('iron_greenhouse',['body','glass','lights']),('steel_tree_grower',['body','glass'])]:
 fs=[]
 for part in parts:
  for f in faces(json.loads((D/(kind+'_'+part+'.json')).read_text())):f['index']=(part,f['index']);fs.append(f)
 hits=[h for h in audit_faces(fs) if h['a'][0][0]!=h['b'][0][0]]
 assert not hits,(kind,hits)
report={'result':'PASS','drill_angles_per_tier':36,'drill_tiers':3,'cross_part_overlaps':0,'farm_composites':['iron_greenhouse body + glass + active lights','steel_tree_grower body + glass'],'checks':'Exact same-facing coplanar faces after renderer pivot transforms; static native sources audited separately.'}
(ROOT/'docs/animated-model-plane-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
