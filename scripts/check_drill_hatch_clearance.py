"""Continuous vertical separation plus sampled full-turn hatch clearance."""
import itertools,json
import numpy as np
from audit_model_planes import ROOT,rotation

base=ROOT/'revoextraction/src/main/resources/assets/revoextraction/models/block'
lo=np.array([16/3,32/3,32/3]);hi=np.array([32/3,16,16]);rows=[]
for part,axis,pivot in [('drill_bit',1,np.array([8,6,8])),('drive_gear',0,np.array([46/3,8,8]))]:
 data=json.loads((base/f'steel_drill_{part}.json').read_text());maximum=-float('inf');conflicts=[]
 for e in data['elements']:
  vertices=np.array(list(itertools.product(*zip(e['from'],e['to']))));v=e.get('rotation',{});m=rotation('xyz'.index(v.get('axis','y')),v.get('angle',0));o=np.array(v.get('origin',[0,0,0]));vertices=o+(vertices-o)@m.T
  # Rotation around Y cannot change height. Around X, each corner's Y/Z
  # radius gives an upper bound for every angle, including between samples.
  bound=vertices[:,1].max() if axis==1 else pivot[1]+np.linalg.norm(vertices[:,1:]-pivot[1:],axis=1).max()
  maximum=max(maximum,float(bound))
  for angle in range(360):
   posed=pivot+(vertices-pivot)@rotation(axis,angle).T
   if np.all(np.minimum(posed.max(0),hi)-np.maximum(posed.min(0),lo)>1e-7):conflicts.append({'part':e['name'],'angle':angle})
 gap=float(lo[1]-maximum);assert gap>0 and not conflicts,(part,gap,conflicts)
 rows.append({'part':part,'fullTurnSamples':360,'continuousVerticalBound':round(maximum,6),'minimumVerticalGapModelUnits':round(gap,6),'minimumVerticalGapWorldBlocks':round(gap*3/16,6),'collisions':0})
report={'result':'PASS','appliesTo':['Steel Drill sediment output','Electric Drill: all five top-layer native MI hatches'],'hatchCell':'rear-top centre (0,2,-1)','normalizedHatchBounds':{'from':lo.tolist(),'to':hi.tolist()},'parts':rows,'proof':'Continuous Y separation at every rotation angle, plus 360 sampled bounding-box tests per component.'}
(ROOT/'docs/steel-drill-hatch-clearance.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
