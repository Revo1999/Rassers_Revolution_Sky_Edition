"""Sample native rigid parts through a turn, checking separate moving assemblies.

Static joints and parts rigidly joined within one assembly may intentionally
intersect. Fluid preview volumes are excluded. Rotated boxes use all 15 OBB
separating axes, rather than axis-aligned bounds alone.
"""
from pathlib import Path
import json,math
import numpy as np
R=Path(__file__).resolve().parents[1]
A=R/'art/blockbench/revoextraction/mechanical-sluice'
d=json.loads((A/'models/mechanical_sluice.bbmodel').read_text())
groups={g['uuid']:g for g in d['groups']};owner={}
def walk(nodes,parent=None):
    for n in nodes:
        if isinstance(n,str):owner[n]=parent
        else:walk(n.get('children',[]),groups.get(n['uuid'],n).get('name',parent))
walk(d['outliner'])
def rx(a):
    c,s=math.cos(a),math.sin(a)
    return np.array([[1,0,0],[0,c,-s],[0,s,c]],float)
def cohort(g):
    if g in ['drive','crank']:return 'rotor'
    if g in ['trough','rod','shaker']:return 'vertical'
    if g.startswith('spring_'):return g
    return 'fixed'
elements=[e for e in d['elements'] if not owner[e['uuid']].startswith('water')]
def box(e,theta):
    g=owner[e['uuid']];motion=cohort(g)
    lo,hi=np.array(e['from'],float),np.array(e['to'],float)
    mid=(lo+hi)/2;half=(hi-lo)/2
    rot=e.get('rotation',[0,0,0]);assert rot[1:]==[0,0],e['name']
    axes=rx(math.radians(rot[0]));origin=np.array(e.get('origin',[0,0,0]),float)
    center=origin+axes@(mid-origin)
    lift=.5*math.sin(theta)
    if motion=='rotor':
        rg=rx(-theta);p=np.array([0,8,8],float);center=p+rg@(center-p);axes=rg@axes
    elif motion=='vertical':center[1]+=lift
    elif motion.startswith('spring_'):
        anchor=5.6 if motion=='spring_front' else 1.6
        scale=1+lift/1.4;center[1]=anchor+(center[1]-anchor)*scale;half[1]*=scale
    extent=np.abs(axes)@half
    return center,half,axes,center-extent,center+extent
def penetration(a,b):
    ca,ha,aa,_,_=a;cb,hb,ab,_,_=b
    delta=cb-ca;depth=float('inf')
    for axis in [*aa.T,*ab.T,*[np.cross(x,y) for x in aa.T for y in ab.T]]:
        length=np.linalg.norm(axis)
        if length<1e-8:continue
        axis=axis/length
        overlap=np.dot(ha,np.abs(aa.T@axis))+np.dot(hb,np.abs(ab.T@axis))-abs(np.dot(delta,axis))
        if overlap<=.025:return None
        depth=min(depth,overlap)
    return depth
conflicts={}
for step in range(129):
    angle=step*math.tau/128
    boxes=[box(e,angle) for e in elements]
    for i,e in enumerate(elements):
        cg=cohort(owner[e['uuid']])
        for j in range(i):
            other=elements[j];og=cohort(owner[other['uuid']])
            if cg==og:continue
            a,b=boxes[i],boxes[j]
            if np.any(np.minimum(a[4],b[4])-np.maximum(a[3],b[3])<=.025):continue
            depth=penetration(a,b)
            if depth is not None:
                key=tuple(sorted([e['name'],other['name']]))
                prev=conflicts.get(key,{'depth':0})
                if depth>prev['depth']:conflicts[key]={'depth':round(float(depth),4),'angle_degrees':round(step*360/128,2),'assemblies':[cg,og]}
report={'result':'PASS' if not conflicts else 'FAIL','samples':129,'solid_elements':len(elements),
        'scope':'Separate moving assemblies; full oriented-box SAT. Touches under 0.025 units excluded. Rigid internal joints and fluid volumes excluded.',
        'conflicts':[{'parts':list(names),**info} for names,info in sorted(conflicts.items())]}
(A/'clearance-report.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
raise SystemExit(bool(conflicts))
