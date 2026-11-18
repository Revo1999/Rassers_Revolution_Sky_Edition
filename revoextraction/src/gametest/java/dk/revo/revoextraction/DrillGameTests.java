package dk.revo.revoextraction;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crank.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(RevoExtraction.MOD_ID)
@PrefixGameTestTemplate(false)
public class DrillGameTests {
    private static DrillMachine build(GameTestHelper helper,Direction facing) {
        var level=helper.getLevel();var pos=helper.absolutePos(new BlockPos(4,2,4));
        level.setBlockAndUpdate(pos,RevoExtraction.CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,facing));
        var machine=(DrillMachine)level.getBlockEntity(pos);
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++) {
            var state=switch(DrillLayout.part(x,y,z)) {
                case LOG -> RevoExtraction.SUPPORT.get().defaultBlockState();
                case CASING -> RevoExtraction.CASING.get().defaultBlockState();
                case CORE -> RevoExtraction.CORE.get().defaultBlockState();
                case CONTROLLER -> RevoExtraction.CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,facing);
                case INPUT -> RevoExtraction.INPUT.get().defaultBlockState().setValue(DrillInput.FACING,facing.getClockWise());
                case HATCH -> RevoExtraction.HATCH.get().defaultBlockState().setValue(DrillHatch.FACING,facing.getCounterClockWise());
                case FLUID_HATCH -> RevoExtraction.STEEL_FLUID_HATCH.get().defaultBlockState();
                case AIR -> Blocks.AIR.defaultBlockState();
            };
            level.setBlockAndUpdate(machine.at(x,y,z),state);
        }
        helper.assertTrue(machine.checkStructure(),"Valid drill must assemble");return machine;
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void automaticOutputAtCycleBoundary(GameTestHelper helper) {
        var machine=build(helper,Direction.NORTH);var level=helper.getLevel();
        var direction=machine.facing().getCounterClockWise();var target=machine.at(-1,0,0).relative(direction);
        level.setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());
        var chest=level.getCapability(Capabilities.ItemHandler.BLOCK,target,direction.getOpposite());
        machine.inventory.setStackInSlot(0,new ItemStack(Items.GRAVEL,5));
        ((DrillDrive)level.getBlockEntity(machine.at(1,1,0))).setSpeed(32);
        for(int i=0;i<37;i++)DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(count(chest,Items.GRAVEL)==0&&machine.inventory.getStackInSlot(0).getCount()==5,"Output must not move before the cycle completes");
        DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(count(chest,Items.GRAVEL)==5&&count(chest,Items.COBBLESTONE)==1,"Completed cycle must push old and new output into adjacent chest");
        helper.assertTrue(count(machine.inventory,Items.GRAVEL)==0&&count(machine.inventory,Items.COBBLESTONE)==0,"Transferred output cannot remain duplicated in the hatch");
        helper.succeed();
    }
    private static int count(net.neoforged.neoforge.items.IItemHandler handler,Item item){int n=0;for(int i=0;i<handler.getSlots();i++)if(handler.getStackInSlot(i).is(item))n+=handler.getStackInSlot(i).getCount();return n;}
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void automaticOutputRetainsRemainderAndRetries(GameTestHelper helper) {
        var machine=build(helper,Direction.NORTH);var level=helper.getLevel();var side=machine.facing().getCounterClockWise();
        var pos=machine.at(-1,0,0).relative(side);level.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());
        var chest=level.getCapability(Capabilities.ItemHandler.BLOCK,pos,side.getOpposite());
        var container=(net.minecraft.world.Container)level.getBlockEntity(pos);
        for(int i=0;i<container.getContainerSize();i++)container.setItem(i,new ItemStack(Items.DIRT,64));
        for(int i=0;i<9;i++)machine.inventory.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
        ((DrillDrive)level.getBlockEntity(machine.at(1,1,0))).setSpeed(32);
        DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(machine.progress()==0&&count(machine.inventory,Items.COBBLESTONE)==576,"Full neighbor must retain every output and stop progress");
        container.setItem(0,new ItemStack(Items.COBBLESTONE,60));
        DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(count(chest,Items.COBBLESTONE)==64&&count(machine.inventory,Items.COBBLESTONE)==572,"Only accepted items may leave the buffer");
        helper.assertTrue(machine.progress()==32,"Blocked machine must retry output and resume when capacity appears");
        for(int i=0;i<37;i++)DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(count(machine.inventory,Items.COBBLESTONE)+count(chest,Items.COBBLESTONE)==637,"Partial output plus production must preserve the exact item total");
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void automaticOutputRespectsSidedInventory(GameTestHelper helper) {
        var machine=build(helper,Direction.NORTH);var level=helper.getLevel();
        var pos=machine.at(-1,0,0).relative(machine.facing().getCounterClockWise());
        level.setBlockAndUpdate(pos,Blocks.FURNACE.defaultBlockState());
        ((DrillDrive)level.getBlockEntity(machine.at(1,1,0))).setSpeed(32);
        for(int i=0;i<38;i++)DrillMachine.tick(level,machine.getBlockPos(),machine.getBlockState(),machine);
        var furnace=(net.minecraft.world.Container)level.getBlockEntity(pos);
        helper.assertTrue(count(machine.inventory,Items.COBBLESTONE)==1,"Furnace side rejects non-fuel: output must remain in the hatch");
        helper.assertTrue(furnace.isEmpty(),"Auto-output must use the adjacent face capability rather than bypass furnace sided rules");
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void wrongFacingDiagnosticsInEveryOrientation(GameTestHelper helper) {
        for(var facing:Direction.Plane.HORIZONTAL){
            var machine=build(helper,facing);var level=helper.getLevel();
            var input=machine.at(1,1,0);var hatch=machine.at(-1,0,0);var corner=machine.at(1,2,-1);
            var inputState=level.getBlockState(input);var hatchState=level.getBlockState(hatch);
            level.setBlockAndUpdate(input,inputState.setValue(DrillInput.FACING,facing.getCounterClockWise()));
            helper.assertTrue(!machine.checkStructure()&&machine.operationalState()==5,"Correct blocks with a reversed input must report orientation, not missing blocks");
            helper.assertTrue(machine.facingIssue().part()==DrillLayout.Part.INPUT&&machine.facingIssue().expected()==facing.getClockWise(),"Input diagnostic must report the correct outward face");
            var diagnostics=DrillDiagnostics.capture(machine);helper.assertTrue(diagnostics.state()==5&&diagnostics.part()==DrillLayout.Part.INPUT.ordinal()+1,"Shared menu/Jade snapshot must preserve orientation details");
            level.setBlockAndUpdate(input,inputState);machine.checkStructure();
            level.setBlockAndUpdate(hatch,hatchState.setValue(DrillHatch.FACING,facing.getClockWise()));
            helper.assertTrue(!machine.checkStructure()&&machine.facingIssue().part()==DrillLayout.Part.HATCH,"Wrong hatch facing must be identified");
            helper.assertTrue(DrillMachine.controllerFor(level,hatch)==machine&&DrillMachine.controllerFor(level,corner)==machine,"Jade must resolve misoriented ports and frame blocks");
            level.setBlockAndUpdate(hatch,hatchState);machine.checkStructure();
            level.setBlockAndUpdate(machine.getBlockPos(),machine.getBlockState().setValue(DrillController.FACING,facing.getOpposite()));
            helper.assertTrue(!machine.checkStructure()&&machine.operationalState()==5&&machine.facingIssue().part()==DrillLayout.Part.CONTROLLER,"A backwards controller must be identified from the alternative blueprint front");
            helper.assertTrue(machine.facingIssue().expected()==facing&&!level.getBlockState(corner).getValue(DrillPart.FORMED),"Controller facing diagnostic must name the correct front and reveal all placement blocks");
            level.setBlockAndUpdate(machine.getBlockPos(),machine.getBlockState().setValue(DrillController.FACING,facing));
            helper.assertTrue(machine.checkStructure(),"Fixing orientations must form the machine again");
        }
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void guideMatchesActualFormation(GameTestHelper helper) {
        var machine=build(helper,Direction.SOUTH);
        var guide=DrillGuideDefinition.create();
        helper.assertTrue(guide.blocks()==26&&guide.layers()==3,"Guide must describe all required blocks and layers");
        helper.assertTrue(guide.materials().stream().mapToInt(ItemStack::getCount).sum()==26,"Materials must include the controller and exclude air");
        for(var part:guide.parts()){
            var actual=helper.getLevel().getBlockState(machine.getBlockPos().offset(part.offset()));
            if(actual.hasProperty(DrillPart.FORMED))actual=actual.setValue(DrillPart.FORMED,false);
            helper.assertTrue(actual.equals(part.state()),"Guide block/facing mismatch at "+part.offset());
        }
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void allRotationsAndBrokenCorner(GameTestHelper helper) {
        for(Direction direction:Direction.Plane.HORIZONTAL) {
            var machine=build(helper,direction);
            var hatch=(HatchEntity)helper.getLevel().getBlockEntity(machine.at(-1,0,0));
            helper.assertTrue(hatch.machine()==machine,"Hatch must resolve the correct controller in every orientation");
            helper.getLevel().setBlockAndUpdate(machine.at(-1,0,1),Blocks.AIR.defaultBlockState());
            helper.assertTrue(!machine.checkStructure()&&!machine.formed(),"Broken support must dismantle");
            helper.assertTrue(hatch.machine()==null,"Detached hatch cannot access saved output");
        }
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void blockedOutputExtractionAndSave(GameTestHelper helper) {
        var m=build(helper,Direction.NORTH);var level=helper.getLevel();
        var drive=(DrillDrive)level.getBlockEntity(m.at(1,1,0));drive.setSpeed(32);
        for(int i=0;i<9;i++)m.inventory.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
        for(int i=0;i<50;i++)DrillMachine.tick(level,m.getBlockPos(),m.getBlockState(),m);
        helper.assertTrue(m.speed()==0,"Full output must stop working animation");
        var output=level.getCapability(Capabilities.ItemHandler.BLOCK,m.at(-1,0,0),null);
        helper.assertTrue(output!=null,"Output capability must exist");
        helper.assertTrue(output.insertItem(0,new ItemStack(Items.DIRT),false).getCount()==1,"Hatch must reject insertion");
        helper.assertTrue(output.extractItem(0,64,false).getCount()==64,"Hatch must extract controller items");
        for(int i=0;i<38;i++)DrillMachine.tick(level,m.getBlockPos(),m.getBlockState(),m);
        helper.assertTrue(m.inventory.getStackInSlot(0).getCount()==1,"Drill must resume without voiding its next output");
        var nbt=m.saveWithFullMetadata(level.registryAccess());
        var restored=(DrillMachine)BlockEntity.loadStatic(m.getBlockPos(),m.getBlockState(),nbt,level.registryAccess());
        helper.assertTrue(restored.inventory.getStackInSlot(0).getCount()==1&&restored.inventory.getStackInSlot(1).getCount()==64,"Inventory must survive serialization");
        helper.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=160)
    public static void createCrankPowersAndStops(GameTestHelper helper) {
        var m=build(helper,Direction.NORTH);var level=helper.getLevel();var direction=m.facing().getClockWise();
        var pos=m.at(1,1,0).relative(direction);
        level.setBlockAndUpdate(pos,AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,direction));
        var crank=(HandCrankBlockEntity)level.getBlockEntity(pos);
        int[] ticks={0};
        helper.onEachTick(()->{if(ticks[0]++<90)crank.turn(false);});
        helper.runAtTickTime(120,()->{
            helper.assertTrue(!m.inventory.getStackInSlot(0).isEmpty(),"Real Create crank network must produce resources");
            helper.assertTrue(m.speed()==0,"Letting go of crank must stop drill animation");
            helper.succeed();
        });
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void machineInterfaceTransfersAndDiagnostics(GameTestHelper helper) {
        var machine=build(helper,Direction.NORTH);
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setPos(machine.getBlockPos().getX()+.5,machine.getBlockPos().getY(),machine.getBlockPos().getZ()+.5);
        for(int i=0;i<36;i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var menu=new DrillMenu(1,player.getInventory(),machine);
        helper.assertTrue(menu.slots.isEmpty(),"Controller must contain no inventory slots");
        helper.assertTrue(menu.quickMoveStack(player,0).isEmpty(),"Controller cannot move machine output");
        helper.assertTrue(menu.stillValid(player),"Nearby player must be able to access controller diagnostics");
        helper.assertTrue(menu.state()==2,"Idle assembled machine must report missing rotational drive");
        var hatch=(HatchEntity)helper.getLevel().getBlockEntity(machine.at(-1,0,0));
        var hatchMenu=new DrillHatchMenu(2,player.getInventory(),hatch,machine);
        helper.assertTrue(hatchMenu.slots.size()==45&&hatchMenu.stillValid(player),"Hatch exposes nine output slots and the player inventory");
        machine.inventory.setStackInSlot(0,new ItemStack(Items.GRAVEL,12));
        helper.assertTrue(!hatchMenu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)),"Hatch output slots must reject insertion");
        var moved=hatchMenu.quickMoveStack(player,0);
        helper.assertTrue(moved.getCount()==12&&machine.inventory.getStackInSlot(0).isEmpty(),"Hatch shift-click must extract the real machine output");
        helper.assertTrue(player.getInventory().countItem(Items.GRAVEL)==12,"Transfer must preserve item count");
        player.getInventory().setItem(9,new ItemStack(Items.DIRT,4));
        helper.assertTrue(hatchMenu.quickMoveStack(player,9).isEmpty()&&player.getInventory().getItem(9).getCount()==4,"Player shift-click cannot insert into hatch output storage");
        var drive=(DrillDrive)helper.getLevel().getBlockEntity(machine.at(1,1,0));drive.setSpeed(32);
        for(int i=0;i<20;i++)DrillMachine.tick(helper.getLevel(),machine.getBlockPos(),machine.getBlockState(),machine);
        helper.assertTrue(menu.state()==1&&menu.rpm()==32&&menu.progress()>0,"Controller retains live RPM, status and cycle data");
        player.setPos(machine.getBlockPos().getX()+20,machine.getBlockPos().getY(),machine.getBlockPos().getZ());
        helper.assertTrue(!menu.stillValid(player)&&!hatchMenu.stillValid(player),"Walking away must invalidate either interface");
        player.setPos(machine.getBlockPos().getX()+.5,machine.getBlockPos().getY(),machine.getBlockPos().getZ()+.5);
        helper.getLevel().setBlockAndUpdate(machine.at(1,2,1),Blocks.AIR.defaultBlockState());machine.checkStructure();
        helper.assertTrue(!hatchMenu.stillValid(player),"Dismantling must invalidate an already-open hatch menu");
        helper.assertTrue(menu.stillValid(player)&&menu.state()==0,"Controller still explains incomplete multiblock without exposing storage");
        helper.succeed();
    }

}
