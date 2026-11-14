package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder("revoindustry") @PrefixGameTestTemplate(false)
public final class FurnaceGameTests {
 static Block block(String id){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));}
 static BlockPos at(FurnaceController c,int x,int y,int z){return ShapeMatcher.toWorldPos(c.getBlockPos(),c.orientation.facingDirection,new BlockPos(x,y,z));}
 static void step(FurnaceController c,int ticks){var level=(net.minecraft.server.level.ServerLevel)c.getLevel();for(int i=0;i<ticks;i++){level.getServer().getWorldData().overworldData().setGameTime(level.getGameTime()+1);c.tick();}}
 static FurnaceController build(GameTestHelper h,FurnaceTier tier,Direction facing,int grade,FurnaceFuelHatch.Kind kind,int fuelGrade){
  var level=h.getLevel();var origin=h.absolutePos(new BlockPos(4,2,2));
  // Clear old controllers first so their native MI matcher releases exclusive hatch ownership.
  for(int x=0;x<9;x++)for(int y=0;y<7;y++)for(int z=0;z<9;z++)level.setBlockAndUpdate(h.absolutePos(new BlockPos(x,y,z)),Blocks.AIR.defaultBlockState());
  level.setBlockAndUpdate(origin,Furnaces.CONTROLLERS.get(tier).get().defaultBlockState());var c=(FurnaceController)level.getBlockEntity(origin);c.orientation.facingDirection=facing;
  for(var e:c.getActiveShape().simpleMembers.entrySet())level.setBlockAndUpdate(ShapeMatcher.toWorldPos(origin,facing,e.getKey()),e.getValue().getPreviewState());
  String prefix=new String[]{"bronze","steel","advanced"}[grade];
  level.setBlockAndUpdate(at(c,-1,0,0),Furnaces.ITEM_HATCHES.get(grade*2).get().defaultBlockState());
  level.setBlockAndUpdate(at(c,1,0,0),Furnaces.ITEM_HATCHES.get(grade*2+1).get().defaultBlockState());
  level.setBlockAndUpdate(at(c,0,0,2),Furnaces.FUEL_HATCHES.get(fuelGrade*3+kind.ordinal()).get().defaultBlockState());
  step(c,1);return c;
 }
 static MIInventory items(FurnaceController c,boolean input){return ((MachineBlockEntity)c.getLevel().getBlockEntity(at(c,input?-1:1,0,0))).getInventory();}
 static FurnaceFuelHatch fuel(FurnaceController c){return (FurnaceFuelHatch)c.getLevel().getBlockEntity(at(c,0,0,2));}
 static void insert(MIInventory inventory,int slot,Item item,int count){var s=inventory.getItemStacks().get(slot);s.setKey(ItemVariant.of(item));s.setAmount(count);}
 static int count(MIInventory inventory,Item item){int n=0;for(var s:inventory.getItemStacks())if(s.toStack().is(item))n+=s.getAmount();return n;}
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void allHatchGradesHaveOneWrenchSelectedPortAndPersist(GameTestHelper h){
  var level=h.getLevel();var pos=h.absolutePos(new BlockPos(4,2,4));var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);player.setShiftKeyDown(true);
  var blocks=new ArrayList<net.neoforged.neoforge.registries.DeferredBlock<MachineBlock>>();blocks.addAll(Furnaces.ITEM_HATCHES);blocks.addAll(Furnaces.FUEL_HATCHES);
  for(var block:blocks){
   level.setBlockAndUpdate(pos,block.get().defaultBlockState());var be=(MachineBlockEntity)level.getBlockEntity(pos);
   for(var face:Direction.values()){
    h.assertTrue(be.orientation.useWrench(player,net.minecraft.world.InteractionHand.MAIN_HAND,face),"MI shift-wrench selects hatch port");
    h.assertTrue(be.getMachineModelData().frontDirection==face,"Visible port agrees with connection direction");
    boolean fluid=be instanceof FurnaceFuelHatch fuel&&fuel.kind!=FurnaceFuelHatch.Kind.SOLID;
    for(var side:Direction.values()){
     var items=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,pos,side);
     var fluids=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,pos,side);
     h.assertTrue((items!=null)==(!fluid&&side==face),"Exactly one item port: "+block.getId()+" "+face+" tested "+side);
     h.assertTrue((fluids!=null)==(fluid&&side==face),"Exactly one fluid port: "+block.getId()+" "+face+" tested "+side);
    }
   }
   if(be instanceof FurnaceItemHatch hatch)hatch.getInventory().itemStorage.itemHandler.insertItem(0,new ItemStack(Items.RAW_IRON),false);
   else if(be instanceof FurnaceFuelHatch hatch&&hatch.kind==FurnaceFuelHatch.Kind.SOLID)hatch.items().insertItem(0,new ItemStack(Items.COAL),false);
   var saved=be.saveWithoutMetadata(level.registryAccess());be.loadWithComponents(saved,level.registryAccess());
   h.assertTrue(be.orientation.outputDirection==Direction.EAST&&be.getMachineModelData().frontDirection==Direction.EAST,"Port survives reload");
  }player.setShiftKeyDown(false);h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=200)
 public static void allTenTiersAllRotationsAndFactoryParallelism(GameTestHelper h){
  for(var tier:FurnaceTier.values())for(var facing:Direction.Plane.HORIZONTAL){
   var c=build(h,tier,facing,tier.factoryTier,FurnaceFuelHatch.Kind.SOLID,2);
   h.assertTrue(c.isShapeValid(),tier+" must form facing "+facing);
   fuel(c).items().insertItem(0,new ItemStack(Items.COAL,64),false);
   insert(items(c,true),0,Items.RAW_IRON,tier==FurnaceTier.RAINBOW?64:12);
   step(c,1);h.assertTrue(c.runningJobs()==(tier==FurnaceTier.RAINBOW?1:tier.lanes()),"Factory lanes must start without augments: "+tier);
   step(c,tier.duration(200));
   h.assertTrue(count(items(c,false),Items.IRON_INGOT)>=(tier==FurnaceTier.RAINBOW?64:tier.lanes()),"Parallel/batch smelting produced expected outputs for "+tier);
   var vent=(FurnaceVent)c.getLevel().getBlockEntity(at(c,0,2,1));h.assertTrue(vent.isMatched(),"Vent must acquire the native casing link");
  }h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void shapeRequiresEveryPortCentreAirAndTier(GameTestHelper h){
  var c=build(h,FurnaceTier.IRON,Direction.NORTH,0,FurnaceFuelHatch.Kind.SOLID,0);h.assertTrue(c.isShapeValid(),"Valid iron furnace forms");
  var level=h.getLevel();var vent=at(c,0,2,1);level.setBlockAndUpdate(vent,FurnaceTier.IRON.casingBlock().defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid()&&!fuel(c).isMatched(),"Missing vent must stop and unlink fuel hatches");
  level.setBlockAndUpdate(vent,Furnaces.VENT_BLOCK.get().defaultBlockState());step(c,1);h.assertTrue(c.isShapeValid(),"Restoring vent reforms");
  level.setBlockAndUpdate(at(c,0,1,1),Blocks.STONE.defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid(),"Centre must remain air");
  level.setBlockAndUpdate(at(c,0,1,1),Furnaces.FUEL_HATCHES.get(0).get().defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid(),"The hollow centre cannot be replaced by a hatch");
  c=build(h,FurnaceTier.DIAMOND,Direction.NORTH,0,FurnaceFuelHatch.Kind.SOLID,2);h.assertTrue(!c.isShapeValid(),"Diamond rejects low-grade item hatches");
  c=build(h,FurnaceTier.IRON,Direction.NORTH,0,FurnaceFuelHatch.Kind.SOLID,0);level.setBlockAndUpdate(at(c,-1,0,0),FurnaceTier.IRON.casingBlock().defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid(),"Item input is required");
  c=build(h,FurnaceTier.IRON,Direction.NORTH,0,FurnaceFuelHatch.Kind.SOLID,0);level.setBlockAndUpdate(at(c,1,0,0),FurnaceTier.IRON.casingBlock().defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid(),"Item output is required");
  c=build(h,FurnaceTier.IRON,Direction.NORTH,0,FurnaceFuelHatch.Kind.SOLID,0);level.setBlockAndUpdate(at(c,0,0,2),FurnaceTier.IRON.casingBlock().defaultBlockState());step(c,1);h.assertTrue(!c.isShapeValid(),"Fuel input is required");h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void solidLiquidGasFiltersAndThroughputGrades(GameTestHelper h){
  for(int grade=0;grade<3;grade++)for(var kind:FurnaceFuelHatch.Kind.values()){
   var c=build(h,FurnaceTier.IRON,Direction.NORTH,0,kind,grade);var fuel=fuel(c);h.assertTrue(c.isShapeValid(),"Fuel type/grade forms");
   if(kind==FurnaceFuelHatch.Kind.SOLID){h.assertTrue(fuel.items().insertItem(0,new ItemStack(Items.STONE),false).is(Items.STONE),"Solid port rejects stone");fuel.items().insertItem(0,new ItemStack(Items.COAL,64),false);}
   else{
    h.assertTrue(fuel.fluids().fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0&&!fuel.getInventory().getFluidStacks().getFirst().isResourceAllowedByLock(Fluids.WATER),"Fuel port and GUI reject water");
    var hydrogen=BuiltInRegistries.FLUID.get(ResourceLocation.parse("modern_industrialization:hydrogen"));
    var wrong=kind==FurnaceFuelHatch.Kind.GAS?Fluids.LAVA:hydrogen;
    h.assertTrue(fuel.fluids().fill(new FluidStack(wrong,1000),FluidAction.EXECUTE)==0,"Liquid and gas ports reject the wrong phase");
    var valid=kind==FurnaceFuelHatch.Kind.GAS?hydrogen:Fluids.LAVA;
    h.assertTrue(fuel.fluids().fill(new FluidStack(valid,64000),FluidAction.EXECUTE)>0,"Fuel port accepts correct fuel");
   }
   long moved=fuel.takeHeat(10000);h.assertTrue(moved==fuel.rate(),"Grade throughput must cap heat transfer: "+kind+" "+grade+" got "+moved);
   h.assertTrue(fuel.takeHeat(10000)==0,"Repeated calls cannot exceed one tick's fuel rate");
  }h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void savedJobsOutputBackpressureAndBreakSafety(GameTestHelper h){
  var c=build(h,FurnaceTier.IRON,Direction.NORTH,1,FurnaceFuelHatch.Kind.SOLID,2);fuel(c).items().insertItem(0,new ItemStack(Items.COAL,64),false);insert(items(c,true),0,Items.RAW_IRON,2);step(c,10);h.assertTrue(c.runningJobs()==2,"Both items must be held in progress");
  var tag=c.saveWithFullMetadata(h.getLevel().registryAccess());var pos=c.getBlockPos();var state=c.getBlockState();c.setRemoved();h.getLevel().removeBlockEntity(pos);
  var restored=(FurnaceController)BlockEntity.loadStatic(pos,state,tag,h.getLevel().registryAccess());h.getLevel().setBlockEntity(restored);step(restored,1);h.assertTrue(restored.runningJobs()==2,"Saved jobs survive reload");
  for(var slot:items(restored,false).getItemStacks()){slot.setKey(ItemVariant.of(Items.STONE));slot.setAmount(64);}long before=fuel(restored).heat();step(restored,30);h.assertTrue(fuel(restored).heat()==before&&restored.heatConsumedLastTick()==0,"Blocked output consumes no fuel");
  for(var slot:items(restored,false).getItemStacks())slot.empty();step(restored,FurnaceTier.IRON.duration(200)+3);h.assertTrue(count(items(restored,false),Items.IRON_INGOT)==2,"Reloaded jobs complete exactly once");
  insert(items(restored,true),0,Items.RAW_GOLD,2);step(restored,1);var drops=restored.dropExtra();h.assertTrue(drops.stream().mapToInt(s->s.is(Items.RAW_GOLD)?s.getCount():0).sum()==2,"Breaking returns escrowed ingredients");h.assertTrue(restored.dropExtra().isEmpty(),"Escrow cannot be dropped twice");h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void differentRecipesSmeltConcurrently(GameTestHelper h){
  var c=build(h,FurnaceTier.IRON,Direction.NORTH,1,FurnaceFuelHatch.Kind.SOLID,2);fuel(c).items().insertItem(0,new ItemStack(Items.COAL,64),false);
  insert(items(c,true),0,Items.RAW_IRON,1);insert(items(c,true),1,Items.RAW_GOLD,1);step(c,1);h.assertTrue(c.runningJobs()==2,"Different recipes must run in parallel");
  step(c,FurnaceTier.IRON.duration(200)+1);h.assertTrue(count(items(c,false),Items.IRON_INGOT)==1&&count(items(c,false),Items.GOLD_INGOT)==1,"Concurrent recipes retain their own results");h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void underpoweredFactorySharesFuelAcrossLanes(GameTestHelper h){
  var c=build(h,FurnaceTier.RAINBOW,Direction.NORTH,2,FurnaceFuelHatch.Kind.SOLID,0);fuel(c).items().insertItem(0,new ItemStack(Items.COAL,64),false);
  for(int i=0;i<4;i++)insert(items(c,true),i,Items.RAW_IRON,64);step(c,100);
  var jobs=c.saveWithFullMetadata(h.getLevel().registryAccess()).getList("FurnaceJobs",10);h.assertTrue(jobs.size()==4,"Four batch jobs must remain in progress");
  for(int i=0;i<jobs.size();i++)h.assertTrue(jobs.getCompound(i).getInt("Progress")>0,"Round-robin heat sharing must prevent lane starvation");h.succeed();
 }
 @GameTest(template="furnace_test",timeoutTicks=100)
 public static void factoryAugmentDisabledForExistingFurnacesAndRecipes(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(4,2,4));h.getLevel().setBlockAndUpdate(pos,block("ironfurnaces:iron_furnace").defaultBlockState());
  var furnace=(ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase)h.getLevel().getBlockEntity(pos);furnace.currentAugment[2]=1;
  h.assertTrue(!furnace.isFactory()&&furnace.isFurnace(),"Existing Factory Augment must no longer activate factory mode or brick normal smelting");
  h.assertTrue(h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("ironfurnaces:augments/augment_factory")).isEmpty(),"Factory Augment recipe must be disabled");h.succeed();
 }
}
