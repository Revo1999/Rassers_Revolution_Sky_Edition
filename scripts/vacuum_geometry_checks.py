from pathlib import Path
import json,math
NORMALS={'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0),'down':(0,-1,0),'up':(0,1,0)}
def dot(a,b):return sum(x*y for x,y in zip(a,b))
def overlaps(a,b):
 for poly in [a,b]:
  for p,q in zip(poly,poly[1:]+poly[:1]):
   n=(p[1]-q[1],q[0]-p[0]);aa=[dot(v,n) for v in a];bb=[dot(v,n) for v in b]
   if min(max(aa),max(bb))-max(min(aa),min(bb))<1e-6:return False
 return True

def coplanar_overlaps(model):
 faces=[]
 for e in model['elements']:
  a,b=e['from'],e['to'];r=e.get('rotation');angle=math.radians(r['angle']) if r and r['axis']=='z' else 0;cos,sin=math.cos(angle),math.sin(angle);o=r['origin'] if r else [0,0,0]
  def rotate(v):return (o[0]+(v[0]-o[0])*cos-(v[1]-o[1])*sin,o[1]+(v[0]-o[0])*sin+(v[1]-o[1])*cos,v[2])
  for name in e['faces']:
   n=NORMALS[name];axis=next(i for i in range(3) if n[i]);other=[i for i in range(3) if i!=axis];points=[]
   for u,v in [(0,0),(1,0),(1,1),(0,1)]:
    p=list(a);p[axis]=b[axis] if n[axis]>0 else a[axis];p[other[0]]=[a,b][u][other[0]];p[other[1]]=[a,b][v][other[1]];points.append(rotate(p))
   norm=(n[0]*cos-n[1]*sin,n[0]*sin+n[1]*cos,n[2]);faces.append((e['name']+'/'+name,norm,points))
 result=[]
 for i,(name,n,a) in enumerate(faces):
  for name2,n2,b in faces[i+1:]:
   if dot(n,n2)<.999999 or abs(dot(n,a[0])-dot(n,b[0]))>1e-6:continue
   drop=max(range(3),key=lambda j:abs(n[j]));axes=[j for j in range(3) if j!=drop];aa=[tuple(p[j] for j in axes) for p in a];bb=[tuple(p[j] for j in axes) for p in b]
   if overlaps(aa,bb):result.append((name,name2))
 return result
if __name__=='__main__':
 for tier in range(1,4):
  model=json.loads(Path(f'revoindustry/src/main/resources/assets/revoindustry/models/block/vacuum_{tier}.json').read_text());result=coplanar_overlaps(model);print(tier,len(result),result)
