"""Detect positive-area coplanar faces, including rotated cubes and native meshes.
Same-facing faces are z-fighting candidates. Opposing faces are contact seams and excluded.
Inherited Java geometry is scanned at its defining file; historical art is reported separately.
"""
from pathlib import Path
import collections, itertools, json, math
import numpy as np

ROOT=Path(__file__).resolve().parents[1]
DIRECTIONS={'west':(0,-1),'east':(0,1),'down':(1,-1),'up':(1,1),'north':(2,-1),'south':(2,1)}

def rotation(axis,angle):
    c,s=math.cos(math.radians(angle)),math.sin(math.radians(angle))
    if axis==0:return np.array([[1,0,0],[0,c,-s],[0,s,c]])
    if axis==1:return np.array([[c,0,s],[0,1,0],[-s,0,c]])
    return np.array([[c,-s,0],[s,c,0],[0,0,1]])

def faces(data):
    result=[]
    for index,e in enumerate(data.get('elements',[])):
        if e.get('export') is False or e.get('visibility') is False:continue
        r=np.eye(3);o=np.array(e.get('origin',[0,0,0]),float)
        if isinstance(e.get('rotation'),dict):
            v=e['rotation'];axis='xyz'.index(v['axis']);r=rotation(axis,v['angle']);o=np.array(v['origin'],float)
            if v.get('rescale'):
                stretch=np.ones(3)/math.cos(math.radians(v['angle']));stretch[axis]=1;r=r@np.diag(stretch)
        else:
            for axis,angle in enumerate(e.get('rotation',[0,0,0])):r=rotation(axis,angle)@r
        for name,f in e.get('faces',{}).items():
            if f.get('texture') is None:continue
            if e.get('type','cube')=='cube':
                axis,sgn=DIRECTIONS[name];lo,hi=e['from'],e['to']
                p=np.array([v for v in itertools.product(*zip(lo,hi)) if v[axis]==(hi[axis] if sgn==1 else lo[axis])],float)
                n=np.zeros(3);n[axis]=sgn
            elif e.get('type')=='mesh':
                p=np.array([e['vertices'][k] for k in f['vertices']],float)
                if len(p)<3:continue
                n=np.cross(p[1]-p[0],p[2]-p[0])
                if np.linalg.norm(n)<1e-10:continue
                n/=np.linalg.norm(n)
            else:continue
            n=np.linalg.inv(r).T@n;n/=np.linalg.norm(n);p=o+(p-o)@r.T
            result.append(dict(index=index,name=e.get('name',str(index)),face=name,p=p,n=n,plane=float(p[0]@n)))
    return result

def ordered(p):
    p=np.unique(p,axis=0);c=p.mean(0)
    return p[np.argsort(np.arctan2(p[:,1]-c[1],p[:,0]-c[0]))]

def area(p):return abs(np.dot(p[:,0],np.roll(p[:,1],-1))-np.dot(p[:,1],np.roll(p[:,0],-1)))/2 if len(p)>=3 else 0

def intersect(p,q):
    for a,b in zip(q,np.roll(q,-1,axis=0)):
        out=[]
        def cross(u,v):return u[0]*v[1]-u[1]*v[0]
        for s,e in zip(p,np.roll(p,-1,axis=0)):
            ds=cross(b-a,s-a);de=cross(b-a,e-a)
            if ds>=-1e-9:out.append(s)
            if (ds>=-1e-9)!=(de>=-1e-9):out.append(s+(e-s)*(ds/(ds-de)))
        p=np.array(out)
        if len(p)<3:return 0
    return area(p)

def audit(data):
    return audit_faces(faces(data))

def audit_faces(face_list):
    groups=collections.defaultdict(list);hits=[]
    for f in face_list:
        # Rounded buckets need a tiny tolerance only; this is an exact coplanarity audit.
        groups[tuple(np.round(f['n'],5))+(round(f['plane'],4),)].append(f)
    for group in groups.values():
        for a,b in itertools.combinations(group,2):
            if a['index']==b['index']:continue
            axes=[j for j in range(3) if j!=int(np.argmax(np.abs(a['n'])))];p=ordered(a['p'][:,axes]);q=ordered(b['p'][:,axes])
            if np.any(np.minimum(p.max(0),q.max(0))-np.maximum(p.min(0),q.min(0))<=1e-6):continue
            overlap=intersect(p,q)
            if overlap>1e-5:hits.append(dict(a=[a['index'],a['name'],a['face']],b=[b['index'],b['name'],b['face']],area=round(overlap,6),a_area=round(area(p),6),b_area=round(area(q),6)))
    return hits

def read_model(path):
    try:return json.loads(path.read_text(encoding='utf-8-sig'))
    except UnicodeDecodeError:return json.loads(path.read_text(encoding='cp1252'))

def main():
    report={'runtime':[],'native':[],'historical':[]}
    for path in sorted(ROOT.glob('revo*/src/main/resources/assets/*/models/**/*.json')):
        data=read_model(path);hits=audit(data)
        report['runtime'].append({'file':path.relative_to(ROOT).as_posix(),'elements':len(data.get('elements',[])),'overlaps':hits})
    for path in sorted((ROOT/'art').rglob('*.bbmodel')):
        data=read_model(path)
        historical=any(v in path.as_posix() for v in ['/history/','/archive/','/variants/','/preserved-','/before-'])
        report['historical' if historical else 'native'].append({'file':path.relative_to(ROOT).as_posix(),'elements':len(data.get('elements',[])),'overlaps':audit(data)})
    dest=ROOT/'docs/model-plane-audit.json';dest.write_text(json.dumps(report,indent=2)+'\n')
    for key,entries in report.items():
        print(key,len(entries),'models,',sum(len(e['overlaps']) for e in entries),'overlaps')
        for entry in entries:
            if entry['overlaps'] and key=='runtime':print(entry['file'],len(entry['overlaps']))

if __name__=='__main__':main()
