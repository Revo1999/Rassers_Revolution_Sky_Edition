from pathlib import Path
import json,math
arrays=[]
for tier in range(1,4):
 boxes=[]
 for e in json.loads(Path(f'art/blockbench/vacuums/vacuum_{tier}.json').read_text())['elements']:
  a,b=e['from'],e['to'];r=e.get('rotation');corners=[]
  for x in [a[0],b[0]]:
   for y in [a[1],b[1]]:
    for z in [a[2],b[2]]:
     if r:
      t=math.radians(r['angle']);cx,cy,cz=r['origin'];v=(cx+(x-cx)*math.cos(t)-(y-cy)*math.sin(t),cy+(x-cx)*math.sin(t)+(y-cy)*math.cos(t),z)
     else:v=(x,y,z)
     corners.append(v)
  boxes.append([min(c[i] for c in corners) for i in range(3)]+[max(c[i] for c in corners) for i in range(3)])
 arrays.append('{\n'+',\n'.join('  {'+','.join(f'{n:.6f}' for n in b)+'}' for b in boxes)+'\n }')
Path('revoindustry/src/main/java/dk/revo/revoindustry/vacuum/VacuumShape.java').write_text('''package dk.revo.revoindustry.vacuum;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
final class VacuumShape {
 private static final VoxelShape[][] SHAPES=new VoxelShape[3][6];
 private static final double[][][] BOXES={
'''+',\n'.join(arrays)+'''
 };
 static {for(int tier=0;tier<3;tier++)for(var direction:Direction.values()){
  var shape=Shapes.empty();for(var b:BOXES[tier]){var a=rotate(direction,b[0],b[1],b[2]);var z=rotate(direction,b[3],b[4],b[5]);shape=Shapes.or(shape,Block.box(Math.min(a[0],z[0]),Math.min(a[1],z[1]),Math.min(a[2],z[2]),Math.max(a[0],z[0]),Math.max(a[1],z[1]),Math.max(a[2],z[2])));}SHAPES[tier][direction.ordinal()]=shape.optimize();
 }}
 private static double[] rotate(Direction d,double x,double y,double z){return switch(d){
  case SOUTH->new double[]{x,y,z};case NORTH->new double[]{16-x,y,16-z};
  case EAST->new double[]{z,y,16-x};case WEST->new double[]{16-z,y,x};
  case UP->new double[]{x,z,16-y};case DOWN->new double[]{x,16-z,y};
 };}
 static VoxelShape get(int tier,Direction direction){return SHAPES[tier][direction.ordinal()];}
}
''')
