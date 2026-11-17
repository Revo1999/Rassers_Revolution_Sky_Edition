"""Plan hidden-face removal and UV-preserving face partitions for Blockbench MCP.

The output is an operation plan, never a replacement runtime model. Applying and
exporting geometry is exclusively done by Blockbench MCP.
"""
from pathlib import Path
from functools import lru_cache
import json,math,zipfile
from PIL import Image
from io import BytesIO
from audit_model_planes import ROOT,DIRECTIONS
from audit_machine_surfaces import near_faces
from audit_model_planes import faces

@lru_cache(None)
def texture(name):
 ns,path=name.split(':');entry=f'assets/{ns}/textures/{path}.png'
 p=ROOT/ns/'src/main/resources'/entry
 if p.exists():return Image.open(p).convert('RGBA')
 jars=[Path('C:/Users/victo/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')] if ns=='minecraft' else list((ROOT/'revolib/run/mods').glob('*.jar'))
 for jar in jars:
  with zipfile.ZipFile(jar) as z:
   if entry in z.namelist():return Image.open(BytesIO(z.read(entry))).convert('RGBA')
 return None

def opaque(cube,textures):
 if len(cube.get('faces',{}))<6:return False
 for face in cube['faces'].values():
  key=face.get('texture','')
  while key.startswith('#'):key=textures.get(key[1:],'')
  if ':' not in key:return False
  im=texture(key)
  if im is None:return False
  u,v,U,V=face['uv'];scale=im.width/16
  region=im.crop((max(0,math.floor(min(u,U)*scale)),max(0,math.floor(min(v,V)*scale)),min(im.width,math.ceil(max(u,U)*scale)),min(im.height,math.ceil(max(v,V)*scale))))
  if region.width<1 or region.height<1 or region.getchannel('A').getextrema()[0]!=255:return False
 return True

def axis_aligned(cube):return not cube.get('rotation',{}).get('angle',0)

def subtract(rect,cut):
 x,y,X,Y=rect;a,b,A,B=cut;a=max(a,x);b=max(b,y);A=min(A,X);B=min(B,Y)
 if A-a<=1e-7 or B-b<=1e-7:return [rect]
 return [r for r in [(x,y,a,Y),(A,y,X,Y),(a,y,A,b),(a,B,A,Y)] if r[2]-r[0]>1e-7 and r[3]-r[1]>1e-7]

def merge(rects):
 while True:
  combined=False
  for i,a in enumerate(rects):
   for j in range(i+1,len(rects)):
    b=rects[j]
    for axis in [0,1]:
     other=1-axis
     if abs(a[other]-b[other])<1e-6 and abs(a[other+2]-b[other+2])<1e-6 and (abs(a[axis+2]-b[axis])<1e-6 or abs(b[axis+2]-a[axis])<1e-6):
      c=list(a);c[axis]=min(a[axis],b[axis]);c[axis+2]=max(a[axis+2],b[axis+2]);rects=rects[:i]+[tuple(c)]+rects[i+1:j]+rects[j+1:];combined=True;break
    if combined:break
   if combined:break
  if not combined:return rects

def clipped_uv(cube,face,rect):
 name,fd=face;axis,sign=DIRECTIONS[name];axes=[i for i in range(3) if i!=axis]
 lo,hi=cube['from'],cube['to'];uaxis=2 if axis==0 else 0;vaxis=2 if axis==1 else 1
 bounds={axes[0]:(rect[0],rect[2]),axes[1]:(rect[1],rect[3])}
 a,A=[(x-lo[uaxis])/(hi[uaxis]-lo[uaxis]) for x in bounds[uaxis]];b,B=[(x-lo[vaxis])/(hi[vaxis]-lo[vaxis]) for x in bounds[vaxis]]
 if name in ['north','east']:a,A=1-A,1-a
 if name!='up':b,B=1-B,1-b
 points=[(a,b),(a,B),(A,b),(A,B)];rotation=fd.get('rotation',0)
 for _ in range(rotation//90):points=[(y,1-x) for x,y in points]
 a=min(p[0] for p in points);A=max(p[0] for p in points);b=min(p[1] for p in points);B=max(p[1] for p in points)
 u,v,U,V=fd['uv'];return [u+(U-u)*a,v+(V-v)*b,u+(U-u)*A,v+(V-v)*B]

def plan(data):
 cubes=data['elements'];solid=[i for i,c in enumerate(cubes) if axis_aligned(c) and opaque(c,data.get('textures',{}))];changes=[];patches=[]
 risky={}
 for hit in near_faces(faces(data)):
  back,front=(hit['b'],hit['a']) if hit['gap']<1e-7 else (hit['a'],hit['b'])
  risky.setdefault((back[0],back[2]),set()).add(front[0])
 for i,c in enumerate(cubes):
  if not axis_aligned(c):continue
  disabled=[]
  for face,fd in c.get('faces',{}).items():
   if (i,face) not in risky:continue
   axis,sign=DIRECTIONS[face];axes=[a for a in range(3) if a!=axis];plane=c['to' if sign==1 else 'from'][axis]
   full=(c['from'][axes[0]],c['from'][axes[1]],c['to'][axes[0]],c['to'][axes[1]]);rects=[full]
   for j in solid:
    if j not in risky[i,face]:continue
    if j==i:continue
    other=cubes[j];lo,hi=other['from'][axis],other['to'][axis]
    if plane<lo-1e-7 or plane>hi+1e-7:continue
    outward=hi-plane if sign>0 else plane-lo
    if outward<=1e-7 and j>i:continue
    cut=(other['from'][axes[0]],other['from'][axes[1]],other['to'][axes[0]],other['to'][axes[1]])
    rects=[piece for rect in rects for piece in subtract(rect,cut)]
    if not rects:break
   if rects==[full]:continue
   rects=merge(rects)
   disabled.append(face)
   for n,rect in enumerate(rects):
    low=c['from'].copy();high=c['to'].copy();low[axes[0]],low[axes[1]]=rect[:2];high[axes[0]],high[axes[1]]=rect[2:]
    thickness=min(.03125,high[axis]-low[axis]);low[axis]=plane-thickness if sign>0 else plane;high[axis]=plane if sign>0 else plane+thickness
    patch={'name':c.get('name',str(i))+f'__visible_{face}_{n}','from':low,'to':high,'face':face,'data':{**fd,'uv':clipped_uv(c,(face,fd),rect)}}
    if 'shade' in c:patch['shade']=c['shade']
    patches.append(patch)
  if disabled:changes.append({'index':i,'faces':disabled})
 return changes,patches

def main():
 import argparse
 parser=argparse.ArgumentParser();parser.add_argument('--baseline-assets',type=Path);args=parser.parse_args()
 rows=[]
 for path in sorted(ROOT.glob('revo*/src/main/resources/assets/*/models/**/*.json')):
  relative=path.as_posix().split('/assets/',1)[1]
  baseline=args.baseline_assets/relative if args.baseline_assets else path
  data=json.loads((baseline if baseline.exists() else path).read_text(encoding='utf-8-sig'))
  if not data.get('elements'):continue
  changes,patches=plan(data)
  if changes:rows.append({'file':path.relative_to(ROOT).as_posix(),'disabled':changes,'patches':patches})
 dest=ROOT/'.local/machine-surface-repair-plan.json';dest.write_text(json.dumps(rows,indent=2)+'\n')
 for r in rows:print(r['file'],sum(len(c['faces']) for c in r['disabled']),'faces replaced with',len(r['patches']),'visible patches')
 print('Planned',len(rows),'models; Blockbench MCP must apply/export this plan.')
if __name__=='__main__':main()
