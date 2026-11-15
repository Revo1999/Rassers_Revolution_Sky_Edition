package dk.revo.revoextraction;

import dk.revo.revocultivation.*;
import dk.revo.revostorage.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public final class OptimizationGameTests {
 static StorageEntity drive(GameTestHelper h){var p=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(p,RevoStorage.DRIVE.get().defaultBlockState());return (StorageEntity)h.getLevel().getBlockEntity(p);}

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void everyFarmTierFormsAndCachesPortsInEveryRotation(GameTestHelper h){
  var level=h.getLevel();var origin=h.absolutePos(new BlockPos(6,2,6));
  for(var kind:MachineKind.values())for(var facing:Direction.Plane.HORIZONTAL){
   for(int x=-5;x<=5;x++)for(int y=0;y<4;y++)for(int z=-5;z<=5;z++)level.setBlockAndUpdate(origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
   level.setBlockAndUpdate(origin,kind.controller().defaultBlockState().setValue(FarmController.FACING,facing));var m=(FarmMachine)level.getBlockEntity(origin);
   for(int x=-kind.radius();x<=kind.radius();x++)for(int y=0;y<kind.height;y++)for(int z=-kind.radius();z<=kind.radius();z++){
    var part=kind.part(x,y,z);var state=kind.block(part).defaultBlockState();
    if(part==MachineKind.Part.CONTROLLER)state=state.setValue(FarmController.FACING,facing);
    if(part==MachineKind.Part.HATCH)state=state.setValue(FarmHatch.FACING,facing.getClockWise());
    if(part==MachineKind.Part.FILTER)state=state.setValue(FarmFilter.FACING,facing);
    if(part==MachineKind.Part.POWER)state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,facing.getOpposite());
    level.setBlockAndUpdate(m.at(x,y,z),state);
   }
   h.assertTrue(m.checkStructure(),"Actual full-width blueprint forms "+kind+" / "+facing);
   var port=(FarmHatchEntity)level.getBlockEntity(m.at(kind.radius(),0,kind.hatchZ()));h.assertTrue(port.machine()==m&&port.machine()==m,"Correct cached item port in every tier/rotation");
   if(kind.attachedTank()){var tank=(FarmWaterTankEntity)level.getBlockEntity(m.at(-kind.radius(),0,0));h.assertTrue(tank.machine()==m&&tank.machine()==m,"Correct cached fluid port in every wide-machine rotation");}
  }h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void largeStorageSpanBoundariesAndTopology(GameTestHelper h){
  var drive=drive(h);var diamond=ProgressionBridgeTests.item("ironchest:diamond_chest");
  for(int b=0;b<9;b++)drive.chests.setStackInSlot(b,new ItemStack(diamond));
  var network=drive.networkItems();h.assertTrue(network.getSlots()==972,"Nine native diamond chests retain all 972 slots");
  for(int slot:new int[]{0,107,108,215,216,863,864,971})h.assertTrue(network.insertItem(slot,new ItemStack(Items.PAPER,slot%63+1),false).isEmpty(),"Insert across chest span boundary "+slot);
  for(int slot:new int[]{0,107,108,215,216,863,864,971})h.assertTrue(drive.banks[slot/108].getStackInSlot(slot%108).getCount()==slot%63+1,"Exact underlying bay/slot mapping "+slot);
  h.assertTrue(network.getStackInSlot(-1).isEmpty()&&network.getStackInSlot(972).isEmpty(),"Out-of-range reads remain empty");
  h.assertTrue(!drive.chests.extractItem(4,1,false).isEmpty()&&network.getSlots()==864,"Same-tick chest removal invalidates slot map");
  h.assertTrue(network.getStackInSlot(756).getCount()==864%63+1,"Later bays shift correctly after removing an empty chest");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void storageAggregationRetainsComponentsAndInvalidates(GameTestHelper h){
  var drive=drive(h);drive.chests.setStackInSlot(0,new ItemStack(Items.CHEST));
  var pos=drive.getBlockPos().east();h.getLevel().setBlockAndUpdate(pos,RevoStorage.TERMINAL.get().defaultBlockState());var terminal=(StorageEntity)h.getLevel().getBlockEntity(pos);
  var named=new ItemStack(Items.DIAMOND,3);named.set(DataComponents.CUSTOM_NAME,Component.literal("Special Diamond"));drive.banks[0].setStackInSlot(0,named);drive.banks[0].setStackInSlot(1,new ItemStack(Items.DIAMOND,2));drive.banks[0].setStackInSlot(2,new ItemStack(Items.DIAMOND,5));
  var menu=new StorageMenu(7,h.makeMockPlayer(GameType.CREATIVE).getInventory(),terminal);var entries=menu.entries();
  h.assertTrue(entries.size()==2&&entries.stream().anyMatch(s->!s.has(DataComponents.CUSTOM_NAME)&&s.getCount()==7),"Hash aggregation respects component identity and totals");
  h.assertTrue(entries==menu.entries(),"Repeated same-tick menu requests reuse aggregation");
  terminal.insert(new ItemStack(Items.DIAMOND,4),false);h.assertTrue(menu.entries()!=entries&&menu.entries().stream().anyMatch(s->!s.has(DataComponents.CUSTOM_NAME)&&s.getCount()==11),"Same-tick inventory mutations invalidate cached totals");
  menu.search="special";h.assertTrue(menu.entries().size()==1&&menu.entries().getFirst().getCount()==3,"Search changes invalidate the cached view");
  menu.search="@minecraft";h.assertTrue(menu.entries().size()==2,"Mod filter still works");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void farmCapacityCacheUnblocksWithoutLosingProducts(GameTestHelper h){
  var m=ProgressionBridgeTests.farm(h,MachineKind.HARVESTER,3);
  for(int i=0;i<9;i++)m.outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
  ProgressionBridgeTests.ticks(m,MachineKind.HARVESTER.ticks);h.assertTrue(m.progress()==0&&m.state()==5,"Blocked production remains paused");
  m.outputs.setStackInSlot(0,ItemStack.EMPTY);m.outputs.setStackInSlot(1,ItemStack.EMPTY);ProgressionBridgeTests.ticks(m,MachineKind.HARVESTER.ticks);
  h.assertTrue(ProgressionBridgeTests.count(m.outputs,Items.LEATHER)==1&&ProgressionBridgeTests.count(m.outputs,Items.BEEF)==2,"Capacity cache invalidates on output change and retains both products");h.succeed();
 }

 @GameTest(template="drill_test",timeoutTicks=100)
 public static void cachedFarmPortRejectsRemovedHostAndRepairsFormation(GameTestHelper h){
  var m=ProgressionBridgeTests.farm(h,MachineKind.HARVESTER,3);var level=h.getLevel();var hatch=(FarmHatchEntity)level.getBlockEntity(m.at(1,0,m.kind().hatchZ()));
  h.assertTrue(hatch.machine()==m&&hatch.machine()==m,"Port resolves and reuses the correct live host");
  var corner=m.at(-1,0,-1);var part=m.kind().part(-1,0,-1);level.setBlockAndUpdate(corner,m.kind().block(part).defaultBlockState());
  h.assertTrue(m.checkStructure()&&level.getBlockState(corner).getValue(FarmPart.FORMED),"Replacing an unformed part repairs formation flags");
  var cached=hatch.items();m.outputs.setStackInSlot(0,new ItemStack(Items.DIAMOND));level.setBlockAndUpdate(m.getBlockPos(),Blocks.AIR.defaultBlockState());
  h.assertTrue(hatch.machine()==null&&cached.extractItem(2,1,false).isEmpty(),"Cached capability cannot retain a removed controller inventory");h.succeed();
 }
}
