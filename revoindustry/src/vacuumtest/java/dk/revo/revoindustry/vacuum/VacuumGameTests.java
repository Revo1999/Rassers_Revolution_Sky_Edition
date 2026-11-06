package dk.revo.revoindustry.vacuum;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class VacuumGameTests {
 static VacuumEntity make(GameTestHelper h,int tier,Direction facing){var pos=h.absolutePos(new BlockPos(4,3,4));if(h.getLevel().getBlockEntity(pos) instanceof VacuumEntity old)for(int i=0;i<old.inventory.getSlots();i++)old.inventory.setStackInSlot(i,ItemStack.EMPTY);h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Vacuums.BLOCKS.get(tier).get().defaultBlockState().setValue(VacuumBlock.OUTLET,facing));return (VacuumEntity)h.getLevel().getBlockEntity(pos);}
 static void step(VacuumEntity v,int ticks){var l=(net.minecraft.server.level.ServerLevel)v.getLevel();for(int i=0;i<ticks;i++){l.getServer().getWorldData().overworldData().setGameTime(l.getGameTime()+1);v.tick();}}
 static ItemEntity item(GameTestHelper h,Vec3 pos,Item item,int count){var e=new ItemEntity(h.getLevel(),pos.x,pos.y,pos.z,new ItemStack(item,count));e.setNoGravity(true);e.setDeltaMovement(Vec3.ZERO);h.getLevel().addFreshEntity(e);return e;}
 @GameTest(template="vacuum_test",timeoutTicks=100)
 public static void tiersSlotsAndSixDirectionalChestTransfer(GameTestHelper h){
  for(int tier=0;tier<3;tier++)for(var face:Direction.values()){
   var v=make(h,tier,face);var target=v.getBlockPos().relative(face);h.getLevel().setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(target);
   h.assertTrue(v.inventory.getSlots()==new int[]{1,3,9}[tier],"Exact buffer slot count");
   v.inventory.insertItem(0,new ItemStack(Items.COBBLESTONE,32),false);step(v,Vacuums.interval(tier)*3);
   h.assertTrue(chest.getItem(0).getCount()==3,"Tier transfer cadence facing "+face+" tier "+tier);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==29,"Transferred only three items");
   chest.clearContent();h.getLevel().setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());
  }h.succeed();
 }
 @GameTest(template="vacuum_test",timeoutTicks=100)
 public static void fullDestinationsPartialStacksAndPersistence(GameTestHelper h){
  var v=make(h,0,Direction.DOWN);var p=v.getBlockPos().below();h.getLevel().setBlockAndUpdate(p,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(p);
  for(int i=0;i<27;i++)chest.setItem(i,new ItemStack(Items.STONE,64));v.inventory.insertItem(0,new ItemStack(Items.DIAMOND,12),false);step(v,24);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==12,"Full chest must preserve buffer");
  var tag=v.saveWithoutMetadata(h.getLevel().registryAccess());var restored=new VacuumEntity(v.getBlockPos(),v.getBlockState());restored.loadWithComponents(tag,h.getLevel().registryAccess());h.assertTrue(restored.inventory.getStackInSlot(0).getCount()==12,"Inventory survives save/load");
  v.inventory.setStackInSlot(0,new ItemStack(Items.DIAMOND,63));var entity=item(h,v.intake(),Items.DIAMOND,5);step(v,4);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==64&&entity.getItem().getCount()==4&&entity.isAlive(),"Partial pickup preserves all remainder");
  h.assertTrue(!v.accepts(new ItemStack(Items.DIAMOND)),"Full buffer refuses attraction");entity.discard();h.succeed();
 }
 @GameTest(template="vacuum_test",timeoutTicks=100)
 public static void pickupRadiusPullDirectionAndActualCollection(GameTestHelper h){
  for(int tier=0;tier<3;tier++){
   var v=make(h,tier,Direction.DOWN);var centre=Vec3.atCenterOf(v.getBlockPos());var inside=item(h,centre.add(Vacuums.radius(tier)-.2,0,0),Items.IRON_INGOT,1);var outside=item(h,centre.add(Vacuums.radius(tier)+.2,0,0),Items.GOLD_INGOT,1);
   step(v,4);h.assertTrue(inside.getDeltaMovement().x<0,"In-range item pulled toward nozzle");h.assertTrue(outside.getDeltaMovement().lengthSqr()==0,"Out-of-range item untouched");
   inside.setPos(v.intake());step(v,4);h.assertTrue(!inside.isAlive()&&v.inventory.getStackInSlot(0).is(Items.IRON_INGOT),"Nearby item enters buffer");outside.discard();
  }h.succeed();
 }
 @GameTest(template="vacuum_test",timeoutTicks=100)
 public static void fullBufferDoesNotMoveItemsAndBreakingDropsOnce(GameTestHelper h){
  var v=make(h,2,Direction.UP);for(int i=0;i<9;i++)v.inventory.setStackInSlot(i,new ItemStack(Items.STONE,64));var e=item(h,Vec3.atCenterOf(v.getBlockPos()).add(2,0,0),Items.DIAMOND,4);step(v,4);h.assertTrue(e.getDeltaMovement().lengthSqr()==0&&e.getItem().getCount()==4,"Full buffer leaves items untouched");
  var box=new net.minecraft.world.phys.AABB(v.getBlockPos()).inflate(1);h.getLevel().setBlockAndUpdate(v.getBlockPos(),Blocks.AIR.defaultBlockState());int count=h.getLevel().getEntitiesOfClass(ItemEntity.class,box).stream().filter(x->x.getItem().is(Items.STONE)).mapToInt(x->x.getItem().getCount()).sum();h.assertTrue(count==576,"Breaking drops every buffered item once");e.discard();h.succeed();
 }
 @GameTest(template="vacuum_test",timeoutTicks=120)
 public static void worldTicksActuallyPullAndCollectItems(GameTestHelper h){
  var v=make(h,2,Direction.DOWN);var e=item(h,v.intake().add(0,0,-2.5),Items.EMERALD,7);
  h.runAtTickTime(80,()->{h.assertTrue(!e.isAlive(),"Real entity motion must reach nozzle");h.assertTrue(v.inventory.getStackInSlot(0).is(Items.EMERALD)&&v.inventory.getStackInSlot(0).getCount()==7,"Collected all seven without manual teleport");h.succeed();});
 }

}
