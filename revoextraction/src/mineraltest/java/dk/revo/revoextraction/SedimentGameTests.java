package dk.revo.revoextraction;

import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public final class SedimentGameTests {
    static DrillMachine build(GameTestHelper h,Direction front){
        var l=h.getLevel();var origin=h.absolutePos(new BlockPos(4,2,4));
        l.setBlockAndUpdate(origin,RevoExtraction.STEEL_CONTROLLER.get().defaultBlockState().setValue(DrillController.FACING,front));
        var machine=(DrillMachine)l.getBlockEntity(origin);
        for(int x=-1;x<=1;x++)for(int y=0;y<3;y++)for(int z=-1;z<=1;z++){
            var part=DrillTier.STEEL.part(x,y,z);var state=DrillTier.STEEL.block(part).defaultBlockState();
            if(part==DrillLayout.Part.CONTROLLER)state=state.setValue(DrillController.FACING,front);
            if(part==DrillLayout.Part.INPUT)state=state.setValue(DrillInput.FACING,front.getClockWise());
            if(part==DrillLayout.Part.HATCH)state=state.setValue(DrillHatch.FACING,front.getCounterClockWise());
            l.setBlockAndUpdate(machine.at(x,y,z),state);
        }
        machine.sedimentHatch().orientation.outputDirection=front.getOpposite();
        h.assertTrue(machine.checkStructure(),"Steel drill forms with new rear fluid hatch");
        ((DrillDrive)l.getBlockEntity(machine.at(1,1,0))).setSpeed(100);
        machine.waterTank().water.fill(new FluidStack(Fluids.WATER,8000),FluidAction.EXECUTE);return machine;
    }
    static void step(DrillMachine m,int ticks){for(int i=0;i<ticks;i++)DrillMachine.tick(m.getLevel(),m.getBlockPos(),m.getBlockState(),m);}
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void coolantIsConservedAndBackpressureIsSafe(GameTestHelper h){
        var m=build(h,Direction.NORTH);step(m,60);
        h.assertTrue(m.waterAmount()==7000&&m.sedimentAmount()==1000,"Ten exact cycles: 1000 mB coolant becomes 1000 mB sediment");
        var hatch=m.sedimentHatch();hatch.acceptCoolant(7000);int water=m.waterAmount(),progress=m.progress(),cycles=m.getPersistentData().getInt("cycle");
        step(m,200);h.assertTrue(m.operationalState()==9&&m.speed()==0,"Full fluid output stops rotation and reports backpressure");
        h.assertTrue(m.waterAmount()==water&&m.progress()==progress&&m.getPersistentData().getInt("cycle")==cycles&&m.dryTicks()==0&&!m.broken(),"Blocked output consumes no coolant, work or output and never causes seizure");
        var fluid=hatch.getInventory().fluidStorage.fluidHandler;
        h.assertTrue(fluid.drain(1000,FluidAction.SIMULATE).getAmount()==1000&&m.sedimentAmount()==8000,"Simulated extraction leaves native tank unchanged");
        fluid.drain(1000,FluidAction.EXECUTE);step(m,6);
        h.assertTrue(m.waterAmount()==water-100&&m.sedimentAmount()==7100&&m.getPersistentData().getInt("cycle")==cycles+1,"Draining resumes exactly one paid cycle");h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void nativeHatchFacesAndStorageSurviveReload(GameTestHelper h){
        for(var front:Direction.Plane.HORIZONTAL){
            var m=build(h,front);var hatch=m.sedimentHatch();var out=front.getOpposite();
            h.assertTrue(h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,hatch.getBlockPos(),out)!=null,"Outward native MI fluid port is exposed");
            h.assertTrue(h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,hatch.getBlockPos(),out.getOpposite())==null,"Inward face is closed");
            var fluid=hatch.getInventory().fluidStorage.fluidHandler;h.assertTrue(fluid.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0,"Native output slot rejects external insertion");
            step(m,6);h.assertTrue(m.sedimentAmount()==100,"Every rotation conserves coolant");
            var pos=hatch.getBlockPos();var state=hatch.getBlockState();var tag=hatch.saveWithFullMetadata(h.getLevel().registryAccess());h.getLevel().removeBlockEntity(pos);var restored=(DrillSedimentHatch)BlockEntity.loadStatic(pos,state,tag,h.getLevel().registryAccess());h.getLevel().setBlockEntity(restored);
            h.assertTrue(restored.amount()==100&&restored.orientation.outputDirection==out,"Native MI tank and port orientation persist");
            restored.orientation.outputDirection=out.getClockWise();h.assertTrue(!m.checkStructure()&&m.facingIssue().part()==DrillLayout.Part.FLUID_HATCH,"Wrong-facing fluid hatch invalidates formation");
        }h.succeed();
    }

    @GameTest(template="drill_test",timeoutTicks=100)
    public static void digitalSidesPersistAndBothOutputsAreAtomic(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(4,2,4));l.setBlockAndUpdate(p,RevoExtraction.DIGITAL.get().defaultBlockState());var e=(ElectricDrillEntity)l.getBlockEntity(p);
        h.assertTrue(l.getCapability(Capabilities.ItemHandler.BLOCK,p,Direction.UP)!=null&&l.getCapability(Capabilities.ItemHandler.BLOCK,p,Direction.EAST)==null,"Only configured input/output sides expose items");
        h.assertTrue(l.getCapability(Capabilities.FluidHandler.BLOCK,p,Direction.UP)==null,"Digital drill has no cooling fluid port");

        h.assertTrue(dk.revo.revolib.energy.EnergyInterop.moveEu(l,p.south(),Direction.NORTH,512,true,false)==512&&e.energy==0,"Configured rear accepts simulated native MI EU without mutation");
        h.assertTrue(dk.revo.revolib.energy.EnergyInterop.moveEu(l,p.east(),Direction.WEST,512,false,false)==0,"Unconfigured face rejects native MI EU");
        e.cycleSide(Direction.EAST);e.cycleSide(Direction.EAST);var tag=e.saveWithoutMetadata(l.registryAccess());e.loadWithComponents(tag,l.registryAccess());h.assertTrue(e.sideMode(Direction.EAST)==2,"Configured side persists");
        for(int i=2;i<11;i++)e.items.setStackInSlot(i,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE,64));
        e.items.setStackInSlot(2,net.minecraft.world.item.ItemStack.EMPTY);e.energy=512;ElectricDrillEntity.tick(l,p,e.getBlockState(),e);
        h.assertTrue(e.work==0&&e.energy==512,"One free slot cannot reserve two distinct co-products");
        e.items.setStackInSlot(3,net.minecraft.world.item.ItemStack.EMPTY);e.energy=0;MineralGameTests.step(e,200);
        h.assertTrue(MineralGameTests.count(e.items,RevoExtraction.DRY_SEDIMENT.get())==1&&e.cycle==1&&e.energy==0,"Digital cycle produces dry sediment with exactly 102400 EU and no water");h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void electricGradesAndFluidBackpressure(GameTestHelper h){
        var e=MineralGameTests.electric(h);MineralGameTests.step(e,200);h.assertTrue(e.waterAmount()==7900&&e.sedimentHatch().amount()==100&&e.sedimentGrade()==1,"LV cooling becomes Good Sediment Water");
        e.select(8);e.items.setStackInSlot(0,new net.minecraft.world.item.ItemStack(RevoExtraction.SURVEY_CORE.get()));e.energy=128;ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.work==0&&e.energy==128,"Different grade requires empty output hatch");
        e.sedimentHatch().getInventory().getFluidStacks().getFirst().empty();e.energy=0;MineralGameTests.step(e,1200);h.assertTrue(e.waterAmount()==7800&&e.sedimentHatch().amount()==100&&e.sedimentGrade()==2,"MV cooling becomes Excellent Sediment Water");
        e.sedimentHatch().acceptCoolant(e.sediment(),7900);int water=e.waterAmount(),work=e.work;e.energy=128;ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.energy==128&&e.work==work&&e.waterAmount()==water&&!e.working,"Full fluid output pauses all paid work");h.succeed();
    }

    @GameTest(template="drill_test",timeoutTicks=100)
    public static void nativeMixerProcessesEverySedimentGrade(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(4,2,4));var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("modern_industrialization:electric_mixer"));
        for(int grade=0;grade<3;grade++){
            l.setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(p,block.defaultBlockState());var m=(aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity)l.getBlockEntity(p);var handler=m.getInventory().fluidStorage.fluidHandler;
            if(grade==0){var item=m.getInventory().getItemStacks().getFirst();item.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(RevoExtraction.DRY_SEDIMENT.get()));item.setAmount(1);handler.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE);}else{var fluid=net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.parse("revoindustry:"+(grade==1?"good_drill_sediment":"excellent_drill_sediment")));handler.fill(new FluidStack(fluid,1000),FluidAction.EXECUTE);handler.fill(new FluidStack(Fluids.WATER,grade==1?1000:3000),FluidAction.EXECUTE);}
            for(int i=0;i<450;i++){m.getEnergyComponent().insertEu(32,aztech.modern_industrialization.util.Simulation.ACT);m.tick();}
            var expected=net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.parse("revoindustry:"+(grade==0?"excellent_drill_sediment":"sea_water")));int amount=grade==0?1000:grade==1?2000:4000;h.assertTrue(handler.drain(new FluidStack(expected,amount),FluidAction.EXECUTE).getAmount()==amount,"Actual native Mixer completes grade "+grade+" without salt starter");
        }h.succeed();
    }

    @GameTest(template="drill_test",timeoutTicks=100)
    public static void nativeEnergyAndItemHatchesDriveElectricWork(GameTestHelper h){
        var e=MineralGameTests.electric(h);e.select(8);
        var input=ElectricDrillStructure.hatch(e,0,1).getInventory().getItemStacks().getFirst();input.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(RevoExtraction.SURVEY_CORE.get()));input.setAmount(1);
        var power=(aztech.modern_industrialization.machines.blockentities.hatches.EnergyHatch)ElectricDrillStructure.hatch(e,-1,0);power.getEnergyComponent().insertEu(128,aztech.modern_industrialization.util.Simulation.ACT);ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);
        h.assertTrue(e.work==1&&e.energy==0&&power.getEnergyComponent().getEu()==0&&input.isEmpty()&&e.items.getStackInSlot(0).is(RevoExtraction.SURVEY_CORE.get()),"Native MI energy and core input hatches operate the actual 3x3 controller");
        e.getLevel().setBlockAndUpdate(ElectricDrillStructure.at(e,1,0,1),Blocks.AIR.defaultBlockState());e.energy=128;ElectricDrillEntity.tick(e.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.work==1&&e.energy==128&&!e.formed(),"Breaking the structure stops work immediately");h.succeed();
    }

    @GameTest(template="drill_test",timeoutTicks=100)
    public static void controllerShiftClickExtractsNativeOutputExactlyOnce(GameTestHelper h){
        var e=MineralGameTests.electric(h);MineralGameTests.step(e,200);var item=MineralGameTests.item("modern_industrialization:raw_silver");var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var menu=new ElectricDrillMenu(1,player.getInventory(),e);
        h.assertTrue(MineralGameTests.count(MineralGameTests.output(e),item)==1,"One output exists in native MI storage");menu.quickMoveStack(player,2);menu.quickMoveStack(player,2);
        h.assertTrue(MineralGameTests.count(MineralGameTests.output(e),item)==0&&player.getInventory().countItem(item)==1,"Shift-click commits copied native MI stack exactly once");h.succeed();
    }
}
