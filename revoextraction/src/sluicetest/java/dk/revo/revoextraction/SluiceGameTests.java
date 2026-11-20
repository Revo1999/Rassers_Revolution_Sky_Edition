package dk.revo.revoextraction;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crank.*;
import com.simibubi.create.content.fluids.pump.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("revoextraction") @PrefixGameTestTemplate(false)
public final class SluiceGameTests {
    static SluiceEntity build(GameTestHelper h, Direction facing) {
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(4,2,4));
        var state=SluiceContent.SLUICE.get().defaultBlockState().setValue(SluiceBlock.FACING,facing);
        l.setBlockAndUpdate(pos,state);
        // Use the real placement path, including its two occupied extension blocks.
        SluiceContent.SLUICE.get().setPlacedBy(l,pos,state,null,new ItemStack(SluiceContent.SLUICE.get()));
        l.setBlockAndUpdate(pos.above(),SluiceContent.FEEDER.get().defaultBlockState().setValue(SluiceFeeder.FACING,facing));
        var e=(SluiceEntity)l.getBlockEntity(pos);
        e.feeder().input.setStackInSlot(0,new ItemStack(Items.GRAVEL,64));
        return e;
    }
    static void water(SluiceEntity e,int amount) { e.feeder().water.fill(new FluidStack(Fluids.WATER,amount),FluidAction.EXECUTE); }
    static void check(GameTestHelper h,boolean condition,String message) { h.assertTrue(condition,message); }
    static int count(SluiceEntity e,Item item) { int n=0;for(int i=0;i<4;i++)if(e.outputs.getStackInSlot(i).is(item))n+=e.outputs.getStackInSlot(i).getCount();return n; }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void portsAndAssemblyInEveryDirection(GameTestHelper h) {
        for(var facing:Direction.Plane.HORIZONTAL) {
            var e=build(h,facing);var l=h.getLevel();var pos=e.getBlockPos();
            check(h,e.assembled(),"Automatic three-block assembly "+facing);
            check(h,e.getBlockState().getPistonPushReaction()==net.minecraft.world.level.material.PushReaction.BLOCK,"Pistons cannot split the occupied machine footprint");
            check(h,e.getBlockState().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.parse("create:non_movable"))),"Create contraptions cannot split the channel");
            for(var side:Direction.values())check(h,SluiceContent.SLUICE.get().hasShaftTowards(l,pos,e.getBlockState(),side)==(side.getAxis()==facing.getClockWise().getAxis()),"Only transverse shaft faces");
            var waterPort=l.getCapability(Capabilities.FluidHandler.BLOCK,pos.above(),facing.getOpposite());
            check(h,waterPort!=null&&waterPort.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==1000&&e.feeder().water.getFluidAmount()==1000,"Rear water port fills the feeder tank");
            check(h,l.getCapability(Capabilities.FluidHandler.BLOCK,pos.above(),Direction.UP)==null,"Top is not a fluid port");
            var gravelPort=l.getCapability(Capabilities.ItemHandler.BLOCK,pos.above(),Direction.UP);
            check(h,gravelPort!=null&&gravelPort.extractItem(0,1,false).is(Items.GRAVEL)&&e.feeder().input.getStackInSlot(0).getCount()==63,"Top gravel port accesses the feeder inventory");
            check(h,e.feeder().water.fill(new FluidStack(Fluids.LAVA,1000),FluidAction.EXECUTE)==0,"Water only");
            check(h,e.feeder().input.insertItem(0,new ItemStack(Items.DIRT),false).is(Items.DIRT),"Gravel only");
            var output=l.getCapability(Capabilities.ItemHandler.BLOCK,e.at(2),facing);
            check(h,output!=null&&output.insertItem(0,new ItemStack(Items.DIAMOND),false).is(Items.DIAMOND),"Final segment output is extraction only");
            l.removeBlock(pos.above(),false);l.removeBlock(pos,false);
        }
        h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void rotationWaterAndOnePaidCycle(GameTestHelper h) {
        var e=build(h,Direction.SOUTH);e.setSpeed(32);
        for(int i=0;i<100;i++)e.tickProcessing();
        check(h,e.work==0&&!e.flowing&&e.cycle==0,"Dry shaft cannot process");
        e.setSpeed(0);water(e,4000);e.tickProcessing();
        check(h,e.flowing&&e.work==0&&e.feeder().water.getFluidAmount()==3995,"Independent water flow without shaft power");
        e.setSpeed(32);for(int i=0;i<100;i++)e.tickProcessing();
        check(h,e.cycle==1&&count(e,Items.FLINT)==1&&e.feeder().input.getStackInSlot(0).getCount()==63,"Exactly one gravel yields one result");
        check(h,e.feeder().water.getFluidAmount()==3495,"Real 500mB water stream for 100 ticks");
        h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void blockedOutputCannotRerollOrConsumeGravel(GameTestHelper h) {
        var e=build(h,Direction.SOUTH);water(e,4000);e.setSpeed(-32);e.cycle=127;
        for(int i=0;i<4;i++)e.outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
        for(int i=0;i<50;i++)e.tickProcessing();
        check(h,e.work==0&&e.cycle==127&&e.feeder().input.getStackInSlot(0).getCount()==64,"Full output pauses work and input");
        check(h,e.flowing&&!e.processing&&e.feeder().water.getFluidAmount()==3750,"Water keeps flowing while output is blocked");
        e.outputs.setStackInSlot(0,ItemStack.EMPTY);for(int i=0;i<100;i++)e.tickProcessing();
        check(h,e.cycle==128&&count(e,Items.RAW_COPPER)==1,"Reverse rotation resumes the same saved copper result");
        for (int cycle = 0; cycle < 128; cycle++) {
            e.cycle = cycle;
            var output = e.nextOutput();
            check(h, !output.is(Items.DIAMOND) && !output.is(Items.EMERALD) && !output.is(Items.LAPIS_LAZULI), "All gravel cycles stay below the gem gate");
        }
        h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void saveLoadAndMenuTransfers(GameTestHelper h) {
        var e=build(h,Direction.SOUTH);water(e,2345);e.work=1234;e.cycle=127;e.outputs.setStackInSlot(0,new ItemStack(Items.LAPIS_LAZULI,5));
        var l=h.getLevel();var saved=e.saveWithFullMetadata(l.registryAccess());
        var restored=(SluiceEntity)BlockEntity.loadStatic(e.getBlockPos(),e.getBlockState(),saved,l.registryAccess());
        check(h,restored.work==1234&&restored.cycle==127&&restored.nextOutput().is(Items.RAW_COPPER)&&count(restored,Items.LAPIS_LAZULI)==5,"Work, cursor and legacy inventory persist");
        var feed=e.feeder();var feedSave=feed.saveWithFullMetadata(l.registryAccess());
        var restoredFeed=(SluiceFeeder.Entity)BlockEntity.loadStatic(feed.getBlockPos(),feed.getBlockState(),feedSave,l.registryAccess());
        check(h,restoredFeed.water.getFluidAmount()==2345&&restoredFeed.input.getStackInSlot(0).getCount()==64,"Feeder tank and gravel persist");
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(l);
        player.setPos(e.at(2).getX()+.5,e.at(2).getY(),e.at(2).getZ()+.5);
        for(int i=0;i<36;i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var control=new SluiceMenu(1,player.getInventory(),SluiceMenu.Kind.CONTROL,e,null);
        check(h,control.slots.isEmpty(),"Diagnostics controller has no inventory");
        var output=new SluiceMenu(2,player.getInventory(),SluiceMenu.Kind.OUTPUT,e,null);
        check(h,output.stillValid(player)&&!output.getSlot(0).mayPlace(new ItemStack(Items.DIRT)),"Output menu validity and insertion restriction");
        output.quickMoveStack(player,0);
        check(h,count(e,Items.LAPIS_LAZULI)==0&&player.getInventory().countItem(Items.LAPIS_LAZULI)==5,"Shift click transfers real buffer");
        l.removeBlock(e.at(1),false);
        check(h,!output.stillValid(player)&&l.getBlockState(e.getBlockPos()).isAir()&&l.getBlockState(e.at(2)).isAir(),"Breaking a segment removes assembly and closes output");
        check(h,!feed.isRemoved()&&feed.water.getFluidAmount()==2345,"Independent feeder survives channel removal");
        h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=100)
    public static void outputRetriesAndRecipesLoad(GameTestHelper h) {
        var e=build(h,Direction.SOUTH);var l=h.getLevel();var pos=e.at(2).east();
        l.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());
        var chest=(net.minecraft.world.Container)l.getBlockEntity(pos);
        for(int i=0;i<chest.getContainerSize();i++)chest.setItem(i,new ItemStack(Items.DIRT,64));
        e.outputs.setStackInSlot(0,new ItemStack(Items.FLINT,10));e.exportOutput();
        check(h,count(e,Items.FLINT)==10,"Blocked neighbor retains output");
        chest.setItem(0,new ItemStack(Items.FLINT,60));e.exportOutput();
        check(h,count(e,Items.FLINT)==6&&chest.getItem(0).getCount()==64,"Only accepted portion leaves buffer");
        for(var id:new String[]{"mechanical_sluice","gravel_feeder"})check(h,l.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("revoextraction:"+id)).isPresent(),"Crafting recipe loads "+id);
        h.succeed();
    }
    @GameTest(template="drill_test",timeoutTicks=240)
    public static void realCreatePumpAndHandCrank(GameTestHelper h) {
        var e=build(h,Direction.SOUTH);var l=h.getLevel();var feeder=e.getBlockPos().above();
        for(int i:new int[]{1,3})l.setBlockAndUpdate(feeder.north(i),AllBlocks.FLUID_PIPE.getDefaultState());
        var pumpPos=feeder.north(2);
        l.setBlockAndUpdate(pumpPos,AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.SOUTH));
        var pump=(PumpBlockEntity)l.getBlockEntity(pumpPos);
        var pumpCogPos=pumpPos.above();
        l.setBlockAndUpdate(pumpCogPos,AllBlocks.COGWHEEL.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.Z));
        var pumpCrankPos=pumpCogPos.south();
        l.setBlockAndUpdate(pumpCrankPos,AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,Direction.SOUTH));
        var pumpCrank=(HandCrankBlockEntity)l.getBlockEntity(pumpCrankPos);
        var source=feeder.north(4);l.setBlockAndUpdate(source,AllBlocks.FLUID_TANK.getDefaultState());
        var tank=l.getCapability(Capabilities.FluidHandler.BLOCK,source,Direction.SOUTH);
        check(h,tank!=null&&tank.fill(new FluidStack(Fluids.WATER,8000),FluidAction.EXECUTE)>0,"Real Create source tank");
        var crankPos=e.getBlockPos().east();
        l.setBlockAndUpdate(crankPos,AllBlocks.HAND_CRANK.getDefaultState().setValue(HandCrankBlock.FACING,Direction.EAST));
        var crank=(HandCrankBlockEntity)l.getBlockEntity(crankPos);
        h.onEachTick(()->{crank.turn(false);pumpCrank.turn(false);});
        h.runAtTickTime(200,()->{
            check(h,e.feeder().water.getFluidAmount()>0,"Actual pump filled rear capability; pump="+pump.getSpeed()+" sluice="+e.getSpeed()+" source="+tank.getFluidInTank(0).getAmount()+" water="+e.feeder().water.getFluidAmount());
            check(h,Math.abs(e.getSpeed())>=16&&e.cycle>0,"Actual crank network powered paid gravel processing");
            h.succeed();
        });
    }
}
