"""Audit shipped models and renderer assemblies; does not claim GPU/gameplay QA.

The near-plane threshold is a conservative modelling heuristic, not a guarantee
for every shader/camera. Opposing normals at structural joints are intentional.
"""
from pathlib import Path
from functools import lru_cache
import copy, json, math
from collections import defaultdict
import numpy as np
from audit_model_planes import ROOT, faces, audit_faces, rotation, ordered, intersect
from audit_machine_surfaces import near_faces

@lru_cache(None)
def model(identifier):
 ns,name=identifier.split(':')
 p=ROOT/ns/f'src/main/resources/assets/{ns}/models/{name}.json'
 if not p.exists():return [] # External MI/vanilla geometry belongs to its own loader.
 d=json.loads(p.read_text());return faces(d) if 'elements' in d else model(d['parent']) if d.get('parent','').split(':')[0].startswith('revo') else []

def part(identifier,owner,matrix=None,offset=(0,0,0),pivot=(0,0,0)):
 fs=copy.deepcopy(model(identifier));m=np.eye(3) if matrix is None else matrix;o=np.array(pivot);t=np.array(offset)
 for f in fs:
  f['p']=o+(f['p']-o)@m.T+t;f['n']=np.linalg.inv(m).T@f['n'];f['n']/=np.linalg.norm(f['n']);f['plane']=float(f['p'][0]@f['n']);f['index']=(owner,f['index'])
 return fs

def cross(fs):
 # A spatial sweep keeps large block-built Nexus decks out of a quadratic
 # all-pairs polygon test. Only same-plane faces from different parts qualify.
 groups=defaultdict(list);hits=[]
 for f in fs:
  axes=[j for j in range(3) if j!=int(np.argmax(np.abs(f['n'])))];p=ordered(f['p'][:,axes])
  groups[tuple(np.round(f['n'],5))+(round(f['plane'],4),)].append((f,p,p.min(0),p.max(0)))
 for group in groups.values():
  group.sort(key=lambda f:f[2][0])
  for i,(a,p,lo,hi) in enumerate(group):
   for b,q,low,high in group[i+1:]:
    if low[0]>=hi[0]-1e-6:break
    if a['index'][0]==b['index'][0] or min(hi[1],high[1])-max(lo[1],low[1])<=1e-6:continue
    overlap=intersect(p,q)
    if overlap>1e-5:hits.append({'a':[a['index'],a['name'],a['face']],'b':[b['index'],b['name'],b['face']],'area':round(overlap,6)})
 return hits

def main():
 report={'scope':'All shipped Revo model JSON, plus explicitly transformed renderer/blueprint assemblies. No graphical Minecraft session or shader verification is claimed.','models':{},'assemblies':[],'failures':[],'reviewedNearPlanes':[],'outOfScopeFindings':[]}
 # These are real transparent tank walls sitting just in front of opaque ports,
 # not two opaque skins competing for one depth value. Keep the tank design.
 accepted={('reservoir_outer_glass','coolant_external_port'),('reservoir_inner_glass','spray_nozzle_13')}
 for p in sorted(ROOT.glob('revo*/src/main/resources/assets/*/models/**/*.json')):
  d=json.loads(p.read_text(encoding='utf-8-sig'));mod=p.parts[len(ROOT.parts)];stats=report['models'].setdefault(mod,{'files':0,'geometryFiles':0,'renderedFaces':0})
  stats['files']+=1;stats['geometryFiles']+=bool(d.get('elements'));fs=faces(d);stats['renderedFaces']+=len(fs)
  for e in d.get('elements',[]):
   assert all(math.isfinite(v) and -16<=v<=32 for v in e['from']+e['to']),(p,e['name'],'bounds')
   assert all(b>a for a,b in zip(e['from'],e['to'])),(p,e['name'],'inverted/sliver')
   for fd in e.get('faces',{}).values():
    assert all(math.isfinite(v) and -.001<=v<=16.001 for v in fd['uv']),(p,e['name'],'UV')
    key=fd.get('texture','')
    while key.startswith('#'):key=d['textures'][key[1:]]
    assert ':' in key,(p,e['name'],'unresolved texture')
  if audit_faces(fs):report['failures'].append({'model':p.relative_to(ROOT).as_posix(),'exact':audit_faces(fs)})
  # These Java models represent a three-block scene normalized to 16 units.
  # Their deliberate gear terraces are 0.05 scene units / 0.15 world units apart.
  threshold=.08/3 if p.stem in ['crank','mechanical_sluice'] else .08
  for h in near_faces(fs,threshold):
   names={h['a'][1].split('__electric_clearance')[0],h['b'][1].split('__electric_clearance')[0]}
   reviewed=any(names==set(pair) for pair in accepted)
   row={'model':p.relative_to(ROOT).as_posix(),**h}
   if '/item/elite_feeding_helmet.json' in p.as_posix():
    report['outOfScopeFindings'].append({**row,'reason':'Wearable equipment item, not a machine; retained for follow-up.'});continue
   (report['reviewedNearPlanes'] if reviewed else report['failures']).append(row)
 def record(name,samples,findings,**extra):
  report['assemblies'].append({'name':name,'samples':samples,'exactCrossPartPairs':len(findings),**extra})
  if findings:report['failures'].append({'assembly':name,'examples':findings[:12]})
 # Native multi-block drills, normalized model units before common scale/rotation.
 for tier in ['wooden','iron','steel']:
  findings=[]
  for a in range(0,360,10):
   fs=part(f'revoextraction:block/{tier}_drill_body','body')
   fs+=part(f'revoextraction:block/{tier}_drill_drill_bit','bit',rotation(1,a),pivot=(8,6,8))
   fs+=part(f'revoextraction:block/{tier}_drill_drive_gear','gear',rotation(0,-a),pivot=(46/3,8,8));findings+=cross(fs)
  record(tier+' drill',36,findings)
 # Native MI supplies a full-block hatch at the rear-top cell. Check its
 # casing against the trimmed special-rendered body in the same normalized space.
 fixture={'elements':[{'name':'native MI fluid hatch casing','from':[16/3,32/3,32/3],'to':[32/3,16,16],'faces':{n:{'texture':'fixture'} for n in ['east','west','north','south','up','down']}}]}
 hatch=faces(fixture)
 for f in hatch:f['index']=('sediment hatch',f['index'])
 record('steel drill body + rear native MI fluid hatch',1,cross(part('revoextraction:block/steel_drill_body','body')+hatch))
 findings=[]
 for a in range(0,360,10):findings+=cross(part('revoextraction:block/electric_drill','body')+part('revoextraction:block/electric_drill_fan','fan',rotation(2,a),pivot=(8,8,0)))
 record('electric drill fan',36,findings)
 # Five native full-block hatches surround the electric rig's roof.
 boxes=[([16/3,32/3,32/3],[32/3,16,16]),([32/3,32/3,16/3],[16,16,32/3]),([16/3,32/3,0],[32/3,16,16/3]),([0,32/3,16/3],[16/3,16,32/3]),([0,32/3,32/3],[16/3,16,16])]
 hatches=[]
 for i,(lo,hi) in enumerate(boxes):
  fs=faces({'elements':[{'name':'MI hatch '+str(i),'from':lo,'to':hi,'faces':{n:{'texture':'fixture'} for n in ['east','west','north','south','up','down']}}]})
  for f in fs:f['index']=('hatch'+str(i),f['index'])
  hatches+=fs
 findings=[]
 for a in range(0,360,10):findings+=cross(part('revoextraction:block/electric_drill_body','body')+hatches+part('revoextraction:block/steel_drill_drill_bit','bit',rotation(1,a),pivot=(8,6,8))+part('revoextraction:block/steel_drill_drive_gear','gear',rotation(0,-a),pivot=(46/3,8,8)))
 record('electric 3x3 drill + five native MI hatches',36,findings)
 findings=[]
 for mode in ['input','output','energy']:
  for axis,angle in [(1,0),(1,90),(1,180),(1,270),(0,90),(0,-90)]:findings+=cross(part('revoextraction:block/digital_drill','body')+part('revoextraction:block/digital_'+mode,'configured face',rotation(axis,angle),pivot=(8,8,8)))
 record('digital drill configurable MI face overlays',18,findings)

 for kind in ['greenhouse','tree_grower','mob_harvester','iron_greenhouse','steel_tree_grower','steel_mob_harvester']:
  fs=part('revocultivation:block/'+kind+'_body','body')
  if kind in ['iron_greenhouse','steel_tree_grower']:fs+=part('revocultivation:block/'+kind+'_glass','glass')
  if kind=='iron_greenhouse':fs+=part('revocultivation:block/iron_greenhouse_lights','lights')
  record(kind,1,cross(fs))
 # Sluice: baked segments in real block space, moving models scaled by three.
 fixed=[]
 for n in range(3):fixed+=part(f'revoextraction:block/sluice/body_{n}','body'+str(n),offset=(0,0,n*16))
 fixed+=part('revoextraction:block/sluice/feeder','feeder',offset=(0,16,0))
 findings=[]
 for a in range(0,360,10):
  lift=.5*math.sin(math.radians(a));fs=copy.deepcopy(fixed)
  for n in ['drive','crank','rod','shaker','trough','spring_front','spring_rear']:
   moving=part('revoextraction:block/sluice/'+n,n,np.eye(3)*3)
   if n in ['drive','crank']:m=rotation(0,-a);o=np.array([8 if n=='drive' else -2,8,8]);shift=np.zeros(3)
   elif n.startswith('spring_'):m=np.diag([1,1+lift/1.4,1]);o=np.array([0,5.6 if n=='spring_front' else 1.6,0]);shift=np.zeros(3)
   else:m=np.eye(3);o=np.zeros(3);shift=np.array([0,lift,0])
   for f in moving:
    f['p']=o+(f['p']-o)@m.T+shift;f['n']=np.linalg.inv(m).T@f['n'];f['n']/=np.linalg.norm(f['n']);f['plane']=float(f['p'][0]@f['n'])
   fs+=moving
  findings+=cross(fs)
 record('mechanical sluice: feeder, three bodies and seven moving parts',36,findings)
 # Water/geode composites include all growth and water-height extrema.
 findings=[]
 for tick in range(0,400,5):
  fs=part('revoindustry:block/geode_rock_bed','bed')+part('revoindustry:block/geode_electrodes','electrodes')
  def smooth(v):v=min(1,max(0,v));return v*v*(3-2*v)
  centres=[(5,6),(10,10),(8,6),(11,5),(7,11)]
  for i,(x,z) in enumerate(centres):
   growth=.025+.975*smooth((tick-i*18)/(300-i*18));harvest=smooth((tick-330-i*8)/28);height=growth*(1-.98*harvest);width=(.65+.25*growth)*(1-.65*harvest)*(.55 if i==3 else 1)
   fs+=part('revoindustry:block/geode_crystal_'+str(i),'bud'+str(i),np.diag([width,height,width]),pivot=(x,2,z),offset=(1 if i==0 else 0,0,-1 if i==4 else 0))
  findings+=cross(fs)
 record('saline geode: bed + electrodes + five staggered growing/harvesting buds',80,findings)
 findings=[];floorClearance=float('inf');ceilingClearance=float('inf')
 for tick in range(0,2400,10):
  f=min(1,tick/2400);rock=math.sin(tick*(.09+.2*f))*(1+f*9);yaw=math.sin(tick*.045)*f*7;pulse=1+math.sin(tick*.14)*f*.035
  fs=part('revoindustry:block/wither_egg','egg',rotation(2,rock)@rotation(1,yaw)*pulse,pivot=(8,0,8),offset=(0,16.16,0))
  pts=np.concatenate([v['p'] for v in fs]);floorClearance=min(floorClearance,float(pts[:,1].min()-16));ceilingClearance=min(ceilingClearance,float(64-2.25-pts[:,1].max()))
  for x in [-16,0,16]:fs+=part('revoindustry:block/wither_roof_lamp_lit','lamp'+str(x),offset=(x,64,0))
  findings+=cross(fs)
 assert floorClearance>0 and ceilingClearance>0,(floorClearance,ceilingClearance)
 record('wither hatchery: rocking egg + three roof lamps',240,findings,floorClearance=round(floorClearance,4),ceilingClearance=round(ceilingClearance,4))
 # The early fluid vessels should only submit their exposed top surface.
 for fluid in ['water','lava']:
  fs=model('revoindustry:block/primitive_'+fluid+'_surface');assert len(fs)==1 and fs[0]['face']=='up'
 record('primitive water/lava vessels',2,[],exposedFacesPerFluid=1)
 for kind in ['composter','silkworm_cultivator']:
  findings=[]
  for stage in range(16):
   fill=(stage+1)/16;silk=kind=='silkworm_cultivator';top=2+(6.5 if silk else 12)*fill
   fs=part('revocultivation:block/'+kind,'body')
   surface='cultivator_fill_'+str(min(3,stage//4)) if silk else 'composter_fill_ready' if stage==15 else 'composter_fill_raw'
   fs+=part('revocultivation:block/'+surface,'fill',offset=(0,top,0))
   if silk:
    fs+=part('revocultivation:block/cultivator_live_worm','worm',offset=(0,top-7+.08,0))
    size=max(.04,(fill-.35)/.65)
    if fill>.35:fs+=part('revocultivation:block/cultivator_live_cocoons','cocoons',np.diag([1,size,1]),offset=(0,14*(1-size),0))
   findings+=cross(fs)
  record(kind+' body + contents',16,findings)
 # Native MI machines and hatches use MI's baked loader; there is no giant
 # replacement body overlay. Inspect every individual placed Revo blueprint cell.
 bp=json.loads((ROOT/'revoindustry/src/main/resources/nexus/structures.json').read_text())
 for name,shape in bp['structures'].items():
  fs=[];external=0;occupied={tuple(c['position']) for c in shape['cells']}
  for i,c in enumerate(shape['cells']):
   if not c['block'].startswith('revoindustry:'):external+=1;continue
   state=ROOT/'revoindustry/src/main/resources/assets/revoindustry/blockstates'/f"{c['block'].split(':')[1]}.json"
   states=json.loads(state.read_text());variants=states.get('variants',{});v=variants.get('facing='+c.get('facing','north'),variants.get('',next(iter(variants.values()),None)))
   if v is None:
    directions={'north':(0,0,-1),'east':(1,0,0),'south':(0,0,1),'west':(-1,0,0)}
    for arm in states.get('multipart',[]):
     if not all(tuple(np.array(c['position'])+directions[direction]) in occupied for direction,value in arm.get('when',{}).items() if value=='true'):continue
     armModel=arm['apply'];fs+=part(armModel['model'],str(i),rotation(1,-armModel.get('y',0)),offset=np.array(c['position'])*16,pivot=(8,8,8))
    continue
   if isinstance(v,list):v=v[0]
   fs+=part(v['model'],str(i),rotation(1,-v.get('y',0)),offset=np.array(c['position'])*16,pivot=(8,8,8))
  record(name+' blueprint',1,cross(fs),placedCells=len(shape['cells']),externalMiCells=external)
 # Checked-in MCP export provenance: collection may alter only loader metadata.
 repairs=json.loads((ROOT/'art/blockbench/surface-repairs/manifest.json').read_text())
 for row in repairs:
  exported=json.loads((ROOT/row['source']).with_suffix('.json').read_text());runtime=json.loads((ROOT/row['model']).read_text())
  assert exported['elements']==runtime['elements'],row['model']
  for key,value in exported['textures'].items():
   expected=row.get('textureBindings',{}).get(key,value)
   actual=runtime['textures'][key]
   while expected.startswith('#'):expected=exported['textures'][expected[1:]]
   while actual.startswith('#'):actual=runtime['textures'][actual[1:]]
   if ':' not in expected:expected='minecraft:'+expected
   if ':' not in actual:actual='minecraft:'+actual
   assert actual==expected,(row['model'],key,actual,expected)
 report['nativeMcpRepairs']=len(repairs);report['result']='PASS' if not report['failures'] else 'FAIL'
 (ROOT/'docs/all-machine-model-audit.json').write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps(report,indent=2));raise SystemExit(bool(report['failures']))

if __name__=='__main__':main()
