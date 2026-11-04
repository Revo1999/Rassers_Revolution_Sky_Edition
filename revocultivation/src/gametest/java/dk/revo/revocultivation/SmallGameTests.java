package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.capabilities.Capabilities;
@GameTestHolder(RevoCultivation.MOD_ID)
@PrefixGameTestTemplate(false)
public class SmallGameTests{
 static SmallMachineEntity make(GameTestHelper h,boolean silk){var p=h.absolutePos(new BlockPos(3,2,3));h.getLevel().setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(p,(silk?SmallMachines.CULTIVATOR.get():SmallMachines.COMPOSTER.get()).defaultBlockState());return (SmallMachineEntity)h.getLevel().getBlockEntity(p);}
 static void tick(SmallMachineEntity m,int n){for(int i=0;i<n;i++)m.tick();}
 @GameTest(template="farm_test",timeoutTicks=100) public static void biologicalCycleRetainsWormAndConsumesLeaves(GameTestHelper h){
  var m=make(h,true);m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,4));m.storage.setStackInSlot(1,new ItemStack(SmallMachines.WORM.get()));tick(m,399);h.assertTrue(m.storage.getStackInSlot(2).isEmpty(),"Must wait full 20-second cycle");tick(m,1);h.assertTrue(m.cycles()==1&&m.storage.getStackInSlot(2).getCount()==2&&m.storage.getStackInSlot(0).getCount()==2&&m.storage.getStackInSlot(1).getCount()==1,"Retain worm, consume exactly 2 leaves, produce 2 cocoons");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void composterRunsOnFixedTimerWithoutAnyPower(GameTestHelper h){
  var m=make(h,false);m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,8));tick(m,159);h.assertTrue(m.progress()==159&&m.storage.getStackInSlot(2).isEmpty(),"Full Composter waits exactly 160 ticks without power");tick(m,1);h.assertTrue(m.cycles()==1&&m.storage.getStackInSlot(2).is(SmallMachines.COMPOST.get())&&m.storage.getStackInSlot(0).getCount()==4,"160 ticks automatically consume four plants and produce one compost");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void legacyKineticMetadataCannotAccelerateTheTimer(GameTestHelper h){
  var m=make(h,false);m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,4));tick(m,79);var tag=m.saveWithFullMetadata(h.getLevel().registryAccess());tag.putFloat("Speed",256);tag.putFloat("GeneratedSpeed",512);var restored=(SmallMachineEntity)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(restored!=null,"Load existing machine");restored.setLevel(h.getLevel());tick(restored,80);h.assertTrue(restored.progress()==159&&restored.storage.getStackInSlot(2).isEmpty(),"Old speed data has no effect after reload");tick(restored,1);h.assertTrue(restored.cycles()==1&&restored.storage.getStackInSlot(2).is(SmallMachines.COMPOST.get()),"Saved work resumes at one work unit per tick");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void fullOutputAndMissingWormPauseWithoutLoss(GameTestHelper h){
  var m=make(h,true);m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,8));tick(m,450);h.assertTrue(m.status()==2&&m.progress()==0,"Needs retained worm");m.storage.setStackInSlot(1,new ItemStack(SmallMachines.WORM.get()));m.storage.setStackInSlot(2,new ItemStack(SmallMachines.COCOON.get(),63));tick(m,450);h.assertTrue(m.status()==3&&m.progress()==0&&m.storage.getStackInSlot(0).getCount()==8,"Full output pauses without consuming input");m.storage.setStackInSlot(2,ItemStack.EMPTY);tick(m,400);h.assertTrue(m.cycles()==1,"Freed buffer resumes");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void capabilitiesAndFrontAutoEjectPreserveRemainders(GameTestHelper h){
  var m=make(h,true);var pos=m.getBlockPos().north();h.getLevel().setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(pos);for(int i=0;i<27;i++)chest.setItem(i,new ItemStack(Items.COBBLESTONE,64));chest.setItem(0,new ItemStack(SmallMachines.COCOON.get(),63));m.storage.setStackInSlot(2,new ItemStack(SmallMachines.COCOON.get(),2));m.eject();h.assertTrue(chest.getItem(0).getCount()==64&&m.storage.getStackInSlot(2).getCount()==1,"Only accepted items leave buffer");var out=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,m.getBlockPos(),Direction.DOWN);h.assertTrue(out.getSlots()==1&&!out.insertItem(0,new ItemStack(Items.DIRT),false).isEmpty(),"Bottom output exposes one slot and rejects insertion");var in=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,m.getBlockPos(),Direction.UP);h.assertTrue(in.getSlots()==2&&in.insertItem(0,new ItemStack(Items.DIRT),false).getCount()==1,"Input rejects non-leaves");h.assertTrue(in.insertItem(1,new ItemStack(SmallMachines.WORM.get(),2),false).getCount()==1,"Only one retained worm");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void saveReloadKeepsRealInventoryAndProgress(GameTestHelper h){
  var m=make(h,true);m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,8));m.storage.setStackInSlot(1,new ItemStack(SmallMachines.WORM.get()));tick(m,123);var tag=m.saveWithFullMetadata(h.getLevel().registryAccess());var restored=(SmallMachineEntity)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(restored!=null&&restored.progress()==123&&restored.storage.getStackInSlot(0).getCount()==8&&restored.storage.getStackInSlot(1).is(SmallMachines.WORM.get()),"NBT preserves progress and inputs");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void handFeedingRaisesContentsAndConsumesExactlyOne(GameTestHelper h){
  var m=make(h,false);var held=new ItemStack(Items.OAK_LEAVES,5);
  for(int i=1;i<=4;i++){h.assertTrue(m.handFeed(held,false),"Held leaf must feed the real input");h.assertTrue(m.fillLevel()==i*2&&m.getBlockState().getValue(SmallMachineBlock.FILL)==i*2,"Quarter-batch fill must update actual block state immediately");}
  h.assertTrue(held.getCount()==1&&m.storage.getStackInSlot(0).getCount()==4,"Each interaction consumes one leaf");h.assertTrue(!m.handFeed(new ItemStack(Items.DIRT),false),"Reject invalid hand feed");var silk=make(h,true);var worm=new ItemStack(SmallMachines.WORM.get(),2);h.assertTrue(silk.handFeed(worm,false)&&worm.getCount()==1&&!silk.handFeed(worm,false),"Exactly one starter worm fits");var leaves=new ItemStack(Items.OAK_LEAVES);h.assertTrue(silk.handFeed(leaves,true)&&leaves.getCount()==1&&silk.fillLevel()==4,"Creative feeding does not consume held items");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void contentsFollowLoadingWorkingReadyAndEmpty(GameTestHelper h){
  var m=make(h,true);h.assertTrue(m.fillLevel()==0&&m.visualStage()==0,"Empty machine must render empty");m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES));h.assertTrue(m.fillLevel()==4&&m.visualStage()==1,"One leaf gives partial visible fill");m.storage.setStackInSlot(1,new ItemStack(SmallMachines.WORM.get()));m.storage.setStackInSlot(0,new ItemStack(Items.OAK_LEAVES,2));tick(m,123);h.assertTrue(m.visualStage()==2&&m.getBlockState().getValue(SmallMachineBlock.STAGE)==2&&m.fillLevel()==8,"Working batch updates synced state");var tag=m.saveWithFullMetadata(h.getLevel().registryAccess());var saved=(SmallMachineEntity)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),tag,h.getLevel().registryAccess());h.assertTrue(saved!=null&&saved.progress()==123&&saved.visualStage()==2&&saved.fillLevel()==8,"Fill and work reconstruct on reload");tick(m,277);h.assertTrue(m.visualStage()==3&&m.fillLevel()==8&&m.getBlockState().getValue(SmallMachineBlock.STAGE)==3,"Ready product remains visibly full");m.storage.extractItem(2,64,false);h.assertTrue(m.visualStage()==0&&m.fillLevel()==0&&m.getBlockState().getValue(SmallMachineBlock.FILL)==0,"Collecting finished product empties the visual state");h.succeed();
 }
 @GameTest(template="farm_test",timeoutTicks=100) public static void automationDrivesVisibleFillAndClearsAfterExport(GameTestHelper h){
  var m=make(h,true);var input=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,m.getBlockPos(),Direction.UP);
  input.insertItem(0,new ItemStack(Items.OAK_LEAVES),false);h.assertTrue(m.getBlockState().getValue(SmallMachineBlock.FILL)==4,"Automation raises visible fill immediately");input.insertItem(0,new ItemStack(Items.OAK_LEAVES),false);input.insertItem(1,new ItemStack(SmallMachines.WORM.get()),false);
  var pos=m.getBlockPos().north();h.getLevel().setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());tick(m,400);var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(pos);h.assertTrue(chest.getItem(0).is(SmallMachines.COCOON.get())&&chest.getItem(0).getCount()==2,"Export real completed output");h.assertTrue(m.visualStage()==0&&m.getBlockState().getValue(SmallMachineBlock.FILL)==0&&m.storage.getStackInSlot(1).getCount()==1,"Auto-export clears visual contents but retains starter");h.succeed();
 }

 @GameTest(template="farm_test",timeoutTicks=100) public static void passiveBlocksRetainOnlyTheirItemPortFacing(GameTestHelper h){
  h.assertTrue(SmallMachineBlock.class.getSuperclass()==Block.class&&SmallMachineEntity.class.getSuperclass()==BlockEntity.class,"Both machines must be ordinary blocks/entities without kinetic inheritance");
  for(boolean silk:new boolean[]{false,true})for(var face:Direction.Plane.HORIZONTAL){var m=make(h,silk);h.getLevel().setBlockAndUpdate(m.getBlockPos(),m.getBlockState().setValue(SmallMachineBlock.FACING,face));var out=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,m.getBlockPos(),face);var input=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,m.getBlockPos(),face.getOpposite());h.assertTrue(out.getSlots()==1&&input.getSlots()==2,"Facing determines item automation, not rotation");}h.succeed();
 }
}
