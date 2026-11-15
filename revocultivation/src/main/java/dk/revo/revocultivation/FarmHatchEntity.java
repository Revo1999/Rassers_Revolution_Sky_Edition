package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
public class FarmHatchEntity extends BlockEntity {
    private boolean exporting;
    public FarmHatchEntity(BlockPos p,BlockState s){super(RevoCultivation.HATCH_ENTITY.get(),p,s);}
    private FarmMachine cachedController;
    public FarmMachine machine(){
        if(level==null||isRemoved()||!getBlockState().getValue(FarmPart.FORMED))return null;
        var m=cachedController;
        if(m==null||m.isRemoved()||!level.hasChunkAt(m.getBlockPos())||level.getBlockEntity(m.getBlockPos())!=m||!m.formed()||!m.at(m.kind().radius(),0,m.kind().hatchZ()).equals(worldPosition))m=FarmMachine.find(level,worldPosition);
        return cachedController=m!=null&&m.formed()&&m.at(m.kind().radius(),0,m.kind().hatchZ()).equals(worldPosition)?m:null;
    }
    public void eject(){
        if(level==null||level.isClientSide||exporting)return;var m=machine();if(m==null)return;
        var dir=getBlockState().getValue(FarmHatch.FACING);var targetPos=worldPosition.relative(dir);if(!level.hasChunkAt(targetPos))return;
        var target=level.getCapability(Capabilities.ItemHandler.BLOCK,targetPos,dir.getOpposite());if(target==null)return;
        exporting=true;try{for(int i=0;i<9;i++){
            var stack=m.outputs.extractItem(i,64,true);if(stack.isEmpty())continue;
            int accepted=stack.getCount()-ItemHandlerHelper.insertItemStacked(target,stack,true).getCount();if(accepted==0)continue;
            var rest=ItemHandlerHelper.insertItemStacked(target,m.outputs.extractItem(i,accepted,false),false);if(!rest.isEmpty())m.outputs.insertItem(i,rest,false);
        }}finally{exporting=false;}
    }
    public IItemHandler items(){return new IItemHandler(){
        public int getSlots(){return 11;}
        public ItemStack getStackInSlot(int slot){var m=machine();return m==null?ItemStack.EMPTY:(slot<2?m.inputs:m.outputs).getStackInSlot(slot<2?slot:slot-2).copy();}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){var m=machine();return m==null||slot>=2?stack:m.inputs.insertItem(slot,stack,simulate);}
        public ItemStack extractItem(int slot,int amount,boolean simulate){var m=machine();if(m==null)return ItemStack.EMPTY;return (slot<2?m.inputs:m.outputs).extractItem(slot<2?slot:slot-2,amount,simulate);}
        public int getSlotLimit(int slot){return slot<2?1:64;}
        public boolean isItemValid(int slot,ItemStack stack){var m=machine();return m!=null&&slot<2&&m.inputs.isItemValid(slot,stack);}
    };}
    public IFluidHandler fluids(){return new IFluidHandler(){
        private FarmMachine connected(){var m=machine();return m!=null&&m.kind().needsWater()?m:null;}
        public int getTanks(){return 1;}
        public FluidStack getFluidInTank(int tank){var m=connected();return m==null?FluidStack.EMPTY:m.water.getFluid().copy();}
        public int getTankCapacity(int tank){var m=connected();return m==null?0:m.kind().waterCapacity();}
        public boolean isFluidValid(int tank,FluidStack fluid){var m=connected();return m!=null&&m.water.isFluidValid(fluid);}
        public int fill(FluidStack resource,FluidAction action){var m=connected();return m==null?0:m.water.fill(resource,action);}
        public FluidStack drain(FluidStack stack,FluidAction action){return FluidStack.EMPTY;}
        public FluidStack drain(int amount,FluidAction action){return FluidStack.EMPTY;}
    };}
    public dk.revo.revolib.energy.EuStorage euInput(){return new dk.revo.revolib.energy.EuStorage(){
        private FarmMachine connected(){var m=machine();return m!=null&&m.kind().electric()?m:null;}
        public long receive(long amount,boolean simulate){var m=connected();return m==null?0:m.receiveEu(amount,simulate);}
        public long getAmount(){var m=connected();return m==null?0:m.energy();}
        public long getCapacity(){var m=connected();return m==null?0:m.energyCapacity();}
        public boolean canReceive(){return connected()!=null;}
    };}
}
