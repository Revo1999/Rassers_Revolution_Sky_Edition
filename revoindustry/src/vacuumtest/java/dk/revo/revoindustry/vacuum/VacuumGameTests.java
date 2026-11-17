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
 static void step(VacuumEntity v,int ticks){var l=(net.minecraft.server.level.ServerLevel)v.getLevel();long original=l.getGameTime();try{for(int i=0;i<ticks;i++){l.getServer().getWorldData().overworldData().setGameTime(original+i+1);v.tick();}}finally{l.getServer().getWorldData().overworldData().setGameTime(original);}}

 static ItemEntity item(GameTestHelper h,Vec3 pos,Item item,int count){var e=new ItemEntity(h.getLevel(),pos.x,pos.y,pos.z,new ItemStack(item,count));e.setNoGravity(true);e.setDeltaMovement(Vec3.ZERO);h.getLevel().addFreshEntity(e);return e;}
 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void tiersSlotsAndSixDirectionalChestTransfer(GameTestHelper h){
  for(int tier=0;tier<3;tier++)for(var face:Direction.values()){
   var v=make(h,tier,face);h.assertTrue(v.intake().subtract(Vec3.atCenterOf(v.getBlockPos())).normalize().dot(Vec3.atLowerCornerOf(face.getNormal()))<-.999,"Intake must be opposite the rear output plate");var target=v.getBlockPos().relative(face);h.getLevel().setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(target);
   h.assertTrue(v.inventory.getSlots()==new int[]{1,3,9}[tier],"Exact buffer slot count");
   v.inventory.insertItem(0,new ItemStack(Items.COBBLESTONE,32),false);step(v,Vacuums.interval(tier)*3);
   h.assertTrue(chest.getItem(0).getCount()==3,"Tier transfer cadence facing "+face+" tier "+tier);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==29,"Transferred only three items");
   chest.clearContent();h.getLevel().setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());
  }h.succeed();
 }
 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void fullDestinationsPartialStacksAndPersistence(GameTestHelper h){
  var v=make(h,0,Direction.DOWN);var p=v.getBlockPos().below();h.getLevel().setBlockAndUpdate(p,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(p);
  for(int i=0;i<27;i++)chest.setItem(i,new ItemStack(Items.STONE,64));v.inventory.insertItem(0,new ItemStack(Items.DIAMOND,12),false);step(v,24);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==12,"Full chest must preserve buffer");
  var tag=v.saveWithoutMetadata(h.getLevel().registryAccess());var restored=new VacuumEntity(v.getBlockPos(),v.getBlockState());restored.loadWithComponents(tag,h.getLevel().registryAccess());h.assertTrue(restored.inventory.getStackInSlot(0).getCount()==12,"Inventory survives save/load");
  v.inventory.setStackInSlot(0,new ItemStack(Items.DIAMOND,63));var entity=item(h,Vec3.atCenterOf(v.getBlockPos()).relative(v.outlet().getOpposite(),.65),Items.DIAMOND,5);step(v,4);h.assertTrue(v.inventory.getStackInSlot(0).getCount()==64&&entity.getItem().getCount()==4&&entity.isAlive(),"Partial pickup preserves all remainder");
  h.assertTrue(!v.accepts(new ItemStack(Items.DIAMOND)),"Full buffer refuses attraction");entity.discard();h.succeed();
 }
 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void pickupCubeBoundariesCornersAndActualCollection(GameTestHelper h){
  for(int tier=0;tier<3;tier++){
   var v=make(h,tier,Direction.SOUTH);var centre=Vacuums.area(v.getBlockPos(),tier,v.outlet()).getCenter();var inside=item(h,centre.add(Vacuums.size(tier)/2.0-.2,0,0),Items.IRON_INGOT,1);var outside=item(h,centre.add(Vacuums.size(tier)/2.0+.2,0,0),Items.GOLD_INGOT,1);
   var area=Vacuums.area(v.getBlockPos(),tier,v.outlet());h.assertTrue(area.getXsize()==new int[]{3,5,9}[tier]&&area.getYsize()==area.getXsize()&&area.getZsize()==area.getXsize(),"Exact block-aligned tier cube");
   double cornerOffset=Vacuums.size(tier)/2.0-.2;var corner=item(h,centre.add(cornerOffset,cornerOffset,cornerOffset),Items.DIAMOND,1);
   step(v,4);h.assertTrue(corner.getDeltaMovement().lengthSqr()>0&&corner.getDeltaMovement().x<0,"Cube corners are included");corner.discard();h.assertTrue(inside.getDeltaMovement().x<0,"In-range item pulled toward nozzle");h.assertTrue(outside.getDeltaMovement().lengthSqr()==0,"Out-of-range item untouched");
   inside.setPos(Vec3.atCenterOf(v.getBlockPos()).relative(v.outlet().getOpposite(),.65));step(v,4);h.assertTrue(!inside.isAlive()&&v.inventory.getStackInSlot(0).is(Items.IRON_INGOT),"Nearby item enters buffer");outside.discard();
  }h.succeed();
 }
 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void fullBufferDoesNotMoveItemsAndBreakingDropsOnce(GameTestHelper h){
  var v=make(h,2,Direction.UP);for(int i=0;i<9;i++)v.inventory.setStackInSlot(i,new ItemStack(Items.STONE,64));var e=item(h,Vec3.atCenterOf(v.getBlockPos()).relative(v.outlet().getOpposite(),2),Items.DIAMOND,4);step(v,4);h.assertTrue(e.getDeltaMovement().lengthSqr()==0&&e.getItem().getCount()==4,"Full buffer leaves items untouched");
  var box=new net.minecraft.world.phys.AABB(v.getBlockPos()).inflate(1);h.getLevel().setBlockAndUpdate(v.getBlockPos(),Blocks.AIR.defaultBlockState());int count=h.getLevel().getEntitiesOfClass(ItemEntity.class,box).stream().filter(x->x.getItem().is(Items.STONE)).mapToInt(x->x.getItem().getCount()).sum();h.assertTrue(count==576,"Breaking drops every buffered item once");e.discard();h.succeed();
 }
 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=120)
 public static void worldTicksActuallyPullAndCollectItems(GameTestHelper h){
  var v=make(h,2,Direction.DOWN);var e=item(h,Vec3.atCenterOf(v.getBlockPos()).relative(v.outlet().getOpposite(),2.5),Items.EMERALD,7);
  h.runAtTickTime(80,()->{h.assertTrue(!e.isAlive(),"Real entity motion must reach nozzle");h.assertTrue(v.inventory.getStackInSlot(0).is(Items.EMERALD)&&v.inventory.getStackInSlot(0).getCount()==7,"Collected all seven without manual teleport");h.succeed();});
 }

 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void forwardCubeAllTiersAndSixOrientations(GameTestHelper h){
  for(int tier=0;tier<3;tier++)for(var outlet:Direction.values()){
   var v=make(h,tier,outlet);var area=Vacuums.area(v.getBlockPos(),tier,outlet);var centre=Vec3.atCenterOf(v.getBlockPos());var front=outlet.getOpposite();int size=Vacuums.size(tier);
   h.assertTrue(!area.contains(centre),"Vacuum must sit outside its forward collection cube");
   h.assertTrue(area.contains(centre.relative(front,1))&&area.contains(centre.relative(front,size)),"First and last blocks in front are included");
   h.assertTrue(!area.contains(centre.relative(front,size+1))&&!area.contains(centre.relative(outlet,1)),"Rear output and beyond-front blocks are excluded");
   var accepted=item(h,centre.relative(front,1),Items.DIAMOND,1);var rejected=item(h,centre.relative(outlet,1),Items.GOLD_INGOT,1);step(v,4);
   h.assertTrue(!accepted.isAlive()&&v.inventory.getStackInSlot(0).is(Items.DIAMOND),"Front item collected in every orientation");
   h.assertTrue(rejected.isAlive()&&rejected.getDeltaMovement().lengthSqr()==0,"Rear item stays untouched");rejected.discard();
  }h.succeed();
 }

 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void floorPlacementUprightAndInventoryMounting(GameTestHelper h){
  var player=h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);var base=h.absolutePos(new BlockPos(4,1,4));
  for(int tier=0;tier<3;tier++)for(var facing:Direction.Plane.HORIZONTAL){
   player.setYRot(facing.toYRot());h.getLevel().setBlockAndUpdate(base,Blocks.STONE.defaultBlockState());
   var hit=new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(base).add(0,.5,0),Direction.UP,base,false);
   var context=new net.minecraft.world.item.context.BlockPlaceContext(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Vacuums.BLOCKS.get(tier).get()),hit);
   h.assertTrue(Vacuums.BLOCKS.get(tier).get().getStateForPlacement(context).getValue(VacuumBlock.OUTLET)==facing,"Floor placement stays upright and faces the player");
   h.getLevel().setBlockAndUpdate(base,Blocks.CHEST.defaultBlockState());
   h.assertTrue(Vacuums.BLOCKS.get(tier).get().getStateForPlacement(context).getValue(VacuumBlock.OUTLET)==Direction.DOWN,"Chest-top mounting aims the rear output into the chest");
  }h.succeed();
 }

 @GameTest(batch="vacuums",template="vacuum_test",timeoutTicks=100)
 public static void collectionStartsAtAuthoredNozzlePlane(GameTestHelper h) throws Exception {
  for(int tier=0;tier<3;tier++){
   String path="assets/revoindustry/models/block/vacuum_"+(tier+1)+".json";
   try(var stream=VacuumGameTests.class.getClassLoader().getResourceAsStream(path)){
    h.assertTrue(stream!=null,"Runtime model is available");var model=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();double nozzleZ=16;
    for(var element:model.getAsJsonArray("elements")){var e=element.getAsJsonObject();if(e.get("name").getAsString().startsWith("flange_"))nozzleZ=Math.min(nozzleZ,e.getAsJsonArray("from").get(2).getAsDouble());}
    h.assertTrue(nozzleZ==0,"Every tier's authored mouth must be flush with its block front");
   }
   for(var outlet:Direction.values()){
    var v=make(h,tier,outlet);var area=Vacuums.area(v.getBlockPos(),tier,outlet);var mouth=v.intake();var front=outlet.getOpposite();var near=mouth.relative(front,.01);var behind=mouth.relative(outlet,.01);
    h.assertTrue(area.contains(near)&&!area.contains(behind),"Pickup starts immediately at the actual nozzle plane, with no gap or rear overlap");
    double size=Vacuums.size(tier);h.assertTrue(area.contains(mouth.relative(front,size-.01))&&!area.contains(mouth.relative(front,size+.01)),"Exactly the requested number of blocks extend out from the nozzle");
    var accepted=item(h,near,Items.DIAMOND,1);var rejected=item(h,behind,Items.GOLD_INGOT,1);step(v,4);h.assertTrue(!accepted.isAlive()&&rejected.isAlive()&&rejected.getDeltaMovement().lengthSqr()==0,"Actual collection matches the nozzle-aligned outline");rejected.discard();
   }
  }h.succeed();
 }

}
