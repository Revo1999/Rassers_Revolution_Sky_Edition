package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
/** A dedicated port onto the controller's single persistent tank, never a duplicate reservoir. */
public final class FarmWaterTankEntity extends BlockEntity {
    public FarmWaterTankEntity(BlockPos p,BlockState s){super(RevoCultivation.TANK_ENTITY.get(),p,s);}
    public FarmMachine machine(){if(level==null||!getBlockState().getValue(FarmPart.FORMED))return null;var m=FarmMachine.find(level,worldPosition);return m!=null&&m.formed()&&m.kind().attachedTank()?m:null;}
    public IFluidHandler fluids(){return new IFluidHandler(){
        public int getTanks(){return 1;}
        public FluidStack getFluidInTank(int tank){var m=machine();return m==null?FluidStack.EMPTY:m.water.getFluid().copy();}
        public int getTankCapacity(int tank){var m=machine();return m==null?16000:m.kind().waterCapacity();}
        public boolean isFluidValid(int tank,FluidStack fluid){return fluid.is(net.minecraft.world.level.material.Fluids.WATER);}
        public int fill(FluidStack fluid,FluidAction action){var m=machine();return m==null?0:m.water.fill(fluid,action);}
        public FluidStack drain(int amount,FluidAction action){var m=machine();return m==null?FluidStack.EMPTY:m.water.drain(amount,action);}
        public FluidStack drain(FluidStack fluid,FluidAction action){var m=machine();return m==null?FluidStack.EMPTY:m.water.drain(fluid,action);}
    };}
}
