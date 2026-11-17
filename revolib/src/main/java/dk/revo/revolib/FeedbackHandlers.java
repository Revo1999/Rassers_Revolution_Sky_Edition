package dk.revo.revolib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Transfer foley follows committed movement. Capability probes/simulations are silent. */
public final class FeedbackHandlers {
    private FeedbackHandlers(){}
    private static void cue(BlockEntity be,MachineFeedback.Voice voice){MachineFeedback.cue(be.getLevel(),be.getBlockPos(),voice);}
    public static IItemHandler items(BlockEntity be,IItemHandler delegate){
        if(delegate==null)return null;
        return new IItemHandler(){
            public int getSlots(){return delegate.getSlots();}
            public ItemStack getStackInSlot(int i){return delegate.getStackInSlot(i);}
            public ItemStack insertItem(int i,ItemStack stack,boolean simulate){var left=delegate.insertItem(i,stack,simulate);if(!simulate&&left.getCount()<stack.getCount())cue(be,MachineFeedback.Voice.TRANSFER);return left;}
            public ItemStack extractItem(int i,int count,boolean simulate){var out=delegate.extractItem(i,count,simulate);if(!simulate&&!out.isEmpty())cue(be,MachineFeedback.Voice.TRANSFER);return out;}
            public int getSlotLimit(int i){return delegate.getSlotLimit(i);}
            public boolean isItemValid(int i,ItemStack stack){return delegate.isItemValid(i,stack);}
        };
    }
    public static IFluidHandler fluids(BlockEntity be,IFluidHandler delegate){
        if(delegate==null)return null;
        return new IFluidHandler(){
            public int getTanks(){return delegate.getTanks();}
            public FluidStack getFluidInTank(int i){return delegate.getFluidInTank(i);}
            public int getTankCapacity(int i){return delegate.getTankCapacity(i);}
            public boolean isFluidValid(int i,FluidStack stack){return delegate.isFluidValid(i,stack);}
            public int fill(FluidStack stack,FluidAction action){int moved=delegate.fill(stack,action);if(action.execute()&&moved>0)cue(be,MachineFeedback.Voice.VALVE);return moved;}
            public FluidStack drain(FluidStack stack,FluidAction action){var moved=delegate.drain(stack,action);if(action.execute()&&!moved.isEmpty())cue(be,MachineFeedback.Voice.VALVE);return moved;}
            public FluidStack drain(int count,FluidAction action){var moved=delegate.drain(count,action);if(action.execute()&&!moved.isEmpty())cue(be,MachineFeedback.Voice.VALVE);return moved;}
        };
    }
}
