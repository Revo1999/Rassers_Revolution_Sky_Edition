from pathlib import Path
import json,math
p=Path('revoindustry/src/main/java/dk/revo/revoindustry/vacuum/VacuumShape.java');s=p.read_text();parts=json.loads(Path('art/blockbench/vacuums/vacuum_1.json').read_text())['elements'];boxes=[]
for e in parts:
 a,b=e['from'],e['to'];r=e.get('rotation');corners=[]
 for x in [a[0],b[0]]:
  for y in [a[1],b[1]]:
   for z in [a[2],b[2]]:
    if r:
     t=math.radians(r['angle']);cx,cy,cz=r['origin'];v=(cx+(x-cx)*math.cos(t)-(y-cy)*math.sin(t),cy+(x-cx)*math.sin(t)+(y-cy)*math.cos(t),z)
    else:v=(x,y,z)
    corners.append(v)
 boxes.append([min(c[i] for c in corners) for i in range(3)]+[max(c[i] for c in corners) for i in range(3)])
values=',\n'.join('  {'+','.join(f'{n:.6f}' for n in b)+'}' for b in boxes)
start=s.index(' private static final double[][] BOXES={');end=s.index('\n };',start);s=s[:start]+' private static final double[][] BOXES={\n'+values+s[end:];p.write_text(s)
