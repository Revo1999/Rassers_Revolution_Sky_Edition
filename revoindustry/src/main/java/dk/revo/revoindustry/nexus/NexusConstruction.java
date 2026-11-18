package dk.revo.revoindustry.nexus;

import aztech.modern_industrialization.machines.MachineComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.util.Simulation;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/** Server-owned high-capacity construction ledger. Native MI hatches are its automation boundary. */
public final class NexusConstruction implements MachineComponent.ServerOnly {
 public static final long ENERGY_COST=100_000_000L;
 public static final int READY=0,MATERIALS=1,POWER=2,OBSTRUCTED=3,UNLOADED=4,PROGRAM=5,STRUCTURE=6,BUILDING=7,COMPLETE=8;
 public record Cell(BlockPos offset,Block block,String part,Direction facing) {public boolean preplaced(){return part.equals("builder")||part.startsWith("builder_");}}
 public record Requirement(Item item,int amount){}
 private static final class Blueprint {
  static final List<Cell> CELLS = readCells();
  static final List<Requirement> REQUIREMENTS = readRequirements();

  private static List<Cell> readCells() {
   var portal = Nexus.blueprint("nexus_portal");
   var origin = Nexus.position(portal.getAsJsonArray("builderOrigin"));
   var cells = new ArrayList<Cell>();
   for (var raw : portal.getAsJsonArray("cells")) {
    var cell = raw.getAsJsonObject();
    var offset = Nexus.position(cell.getAsJsonArray("position")).subtract(origin);
    var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.get("block").getAsString()));
    var facing = Direction.byName(cell.get("facing").getAsString());
    cells.add(new Cell(offset, block, cell.get("part").getAsString(), facing));
   }
   return List.copyOf(cells);
  }

  private static List<Requirement> readRequirements() {
   var counts = new LinkedHashMap<Item, Integer>();
   counts.put(Nexus.PROGRAM.get(), 1);
   for (var cell : CELLS) if (!cell.preplaced()) counts.merge(cell.block.asItem(), 1, Integer::sum);
   return counts.entrySet().stream().map(entry -> new Requirement(entry.getKey(), entry.getValue())).toList();
  }
 }

 public static List<Cell> cells() { return Blueprint.CELLS; }
 public static List<Requirement> requirements() { return Blueprint.REQUIREMENTS; }
 private final NexusMachine machine;
 private final int[] stored=new int[requirements().size()],used=new int[requirements().size()];
 private int cursor,status=STRUCTURE,scanStatus=STRUCTURE;
 private boolean started;
 private long energy;
 private Direction lockedFacing=Direction.NORTH;
 private String problem="Fit native MI input, output and energy hatches";
 public NexusConstruction(NexusMachine machine){this.machine=machine;}
 public int cursor(){return cursor;}public boolean started(){return started;}public long energy(){return energy;}public int status(){return status;}public String problem(){return problem;}
 public int stored(int index){return stored[index];}public int used(int index){return used[index];}
 public int totalRequired(){return requirements().stream().mapToInt(Requirement::amount).sum();}
 public int supplied(){int n=0;for(int i=0;i<stored.length;i++)n+=stored[i]+used[i];return n;}
 private int placed(){return Arrays.stream(used).sum();}
 private int buildable(){return totalRequired()-1;}
 private long paid(){return ENERGY_COST*placed()/buildable();}
 private Direction facing(){return started?lockedFacing:machine.orientation.facingDirection;}
 public BlockPos target(Cell c){return ShapeMatcher.toWorldPos(machine.getBlockPos(),facing(),c.offset);}
 public AABB bounds(){double x=Double.POSITIVE_INFINITY,y=x,z=x,X=-x,Y=-x,Z=-x;for(var c:cells()){var p=target(c);x=Math.min(x,p.getX());y=Math.min(y,p.getY());z=Math.min(z,p.getZ());X=Math.max(X,p.getX()+1);Y=Math.max(Y,p.getY()+1);Z=Math.max(Z,p.getZ()+1);}return new AABB(x,y,z,X,Y,Z);}
 private int index(Item item){for(int i=0;i<requirements().size();i++)if(requirements().get(i).item==item)return i;return -1;}
 public int deposit(ItemStack stack,boolean simulate){int i=index(stack.getItem());if(i<0||stack.has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)||stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))return 0;int n=Math.min(stack.getCount(),requirements().get(i).amount-stored[i]-used[i]);if(!simulate&&n>0){stored[i]+=n;machine.setChanged();}return n;}
 public ItemStack withdraw(int i,int count){if(i<0||i>=stored.length)return ItemStack.EMPTY;int n=Math.min(64,Math.min(Math.max(0,count),stored[i]));if(n==0)return ItemStack.EMPTY;stored[i]-=n;machine.setChanged();return new ItemStack(requirements().get(i).item,n);}
 private boolean preplacedMatches(Cell c,BlockPos p){var level=machine.getLevel();if(c.part.equals("builder"))return level.getBlockState(p).is(Nexus.block("nexus_builder"));if(!(level.getBlockEntity(p) instanceof HatchBlockEntity h))return false;return h.getHatchType()==switch(c.part){case "builder_input"->HatchTypes.ITEM_INPUT;case "builder_output"->HatchTypes.ITEM_OUTPUT;default->HatchTypes.ENERGY_INPUT;};}
 public int inspectSite(){
  var level=machine.getLevel();var allowed=new HashMap<BlockPos,Cell>();for(int i=0;i<cells().size();i++){var c=cells().get(i);if(c.preplaced()||i<cursor)allowed.put(target(c),c);}
  var box=bounds();for(var p:BlockPos.betweenClosed((int)box.minX,(int)box.minY,(int)box.minZ,(int)box.maxX-1,(int)box.maxY-1,(int)box.maxZ-1)){
   if(level.isOutsideBuildHeight(p)||!level.getWorldBorder().isWithinBounds(p)){problem="Outside world build limits at "+p.toShortString();return scanStatus=OBSTRUCTED;}
   if(!level.hasChunkAt(p)){problem="Build area is not loaded";return scanStatus=UNLOADED;}
   var existing=level.getBlockState(p);var c=allowed.get(p);if(c!=null){if(c.preplaced()?preplacedMatches(c,p):existing.is(c.block))continue;problem="Changed construction block at "+p.toShortString();return scanStatus=OBSTRUCTED;}
   if(!existing.isAir()){problem="Obstructed at "+p.toShortString();return scanStatus=OBSTRUCTED;}
  }problem="Build area clear";return scanStatus=READY;
 }
 public int refresh(boolean scan){
  if(!machine.isShapeValid()){problem="Fit native MI input, output and energy hatches";return status=STRUCTURE;}
  if(cursor>=cells().size()){problem="Construction complete";return status=COMPLETE;}
  if(scan)inspectSite();if(scanStatus!=READY)return status=scanStatus;
  if(stored[0]<1){problem="Insert completed Construction Program";return status=PROGRAM;}
  if(supplied()!=totalRequired()){problem="Supply all blueprint materials";return status=MATERIALS;}
  if(energy<ENERGY_COST-paid()){problem="Charge construction power";return status=POWER;}
  problem=started?"Building "+cursor+" / "+cells().size():"Ready to build";return status=started?BUILDING:READY;
 }
 public boolean start(){if(refresh(true)!=READY||started)return false;started=true;lockedFacing=machine.orientation.facingDirection;machine.setChanged();machine.sync(false);return true;}
 public void tick(){
  if(cursor>=cells().size()){if(status!=COMPLETE){refresh(false);machine.sync(false);}return;}
  if(!machine.isShapeValid()){status=STRUCTURE;return;}
  for(var slot:machine.getMultiblockInventoryComponent().getItemInputs()){var stack=slot.toStack();int n=deposit(stack,false);if(n>0){slot.decrement(n);machine.setChanged();}}
  long needed=ENERGY_COST-paid()-energy;if(needed>0)for(var source:machine.getEnergyComponents()){long n=source.consumeEu(Math.min(needed,262144),Simulation.ACT);energy+=n;needed-=n;if(n>0)machine.setChanged();}
  long time=machine.getLevel().getGameTime();int old=status;refresh(time%20==0);if(old!=status||time%20==0)machine.sync(false);
  if(status!=BUILDING||time%10!=0)return;
  var c=cells().get(cursor);var p=target(c);if(!machine.getLevel().hasChunkAt(p)){scanStatus=UNLOADED;return;}
  if(c.preplaced()){if(preplacedMatches(c,p)){cursor++;machine.setChanged();}else scanStatus=OBSTRUCTED;return;}
  if(!machine.getLevel().getBlockState(p).isAir()){scanStatus=OBSTRUCTED;problem="Obstructed at "+p.toShortString();return;}
  int i=index(c.block.asItem());long cost=ENERGY_COST*(placed()+1)/buildable()-paid();if(i<0||stored[i]<1||energy<cost)return;
  Direction face=c.facing;int turns=Math.floorMod(facing().get2DDataValue()-Direction.NORTH.get2DDataValue(),4);for(int j=0;j<turns;j++)face=face.getClockWise();var state=c.block.defaultBlockState();if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,face);
  if(!machine.getLevel().setBlock(p,state,3))return;
  if(machine.getLevel().getBlockEntity(p) instanceof MachineBlockEntity m){m.orientation.facingDirection=face;m.orientation.outputDirection=face;m.setChanged();m.sync();}
  stored[i]--;used[i]++;energy-=cost;cursor++;machine.setChanged();
 }
 public List<ItemStack> drainDrops(){var result=new ArrayList<ItemStack>();for(int i=0;i<stored.length;i++)while(stored[i]>0)result.add(withdraw(i,64));return result;}
 @Override public void writeNbt(CompoundTag tag,HolderLookup.Provider r){var t=new CompoundTag();t.putIntArray("Stored",stored);t.putIntArray("Used",used);t.putInt("Cursor",cursor);t.putBoolean("Started",started);t.putLong("Energy",energy);t.putString("Facing",lockedFacing.getName());tag.put("NexusConstruction",t);}
 @Override public void readNbt(CompoundTag tag,HolderLookup.Provider r,boolean upgrade){var t=tag.getCompound("NexusConstruction");var a=t.getIntArray("Stored");var b=t.getIntArray("Used");for(int i=0;i<stored.length;i++){used[i]=i==0?0:Math.clamp(i<b.length?b[i]:0,0,requirements().get(i).amount);stored[i]=Math.clamp(i<a.length?a[i]:0,0,requirements().get(i).amount-used[i]);}cursor=Math.clamp(t.getInt("Cursor"),0,cells().size());started=t.getBoolean("Started");energy=Math.clamp(t.getLong("Energy"),0,ENERGY_COST-paid());var f=Direction.byName(t.getString("Facing"));lockedFacing=f!=null&&f.getAxis().isHorizontal()?f:Direction.NORTH;scanStatus=STRUCTURE;}
 public void readClient(int state,String text){status=state;problem=text;}
}
