package dk.revo.revoindustry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Machine fluid transfers follow the same disposable-lava rule as world placement. */
public final class ClayBucketFluidHandler implements IFluidHandlerItem{
 private ItemStack container;
 public ClayBucketFluidHandler(ItemStack stack){container=stack;}
 private int content(){return container.getItem() instanceof ClayBucketItem b?b.content:0;}
 public ItemStack getContainer(){return container;}
 public int getTanks(){return 1;}public int getTankCapacity(int tank){return 1000;}
 public FluidStack getFluidInTank(int tank){return tank!=0||content()==0?FluidStack.EMPTY:new FluidStack(content()==1?Fluids.WATER:Fluids.LAVA,1000);}
 public boolean isFluidValid(int tank,FluidStack s){return tank==0&&(s.is(Fluids.WATER)||s.is(Fluids.LAVA));}
 public int fill(FluidStack s,FluidAction action){if(container.getCount()!=1||content()!=0||s.getAmount()<1000||!isFluidValid(0,s))return 0;if(action.execute())container=new ItemStack(s.is(Fluids.WATER)?PrimitiveMachines.WATER_BUCKET.get():PrimitiveMachines.LAVA_BUCKET.get());return 1000;}
 public FluidStack drain(FluidStack s,FluidAction a){return !getFluidInTank(0).isEmpty()&&FluidStack.isSameFluidSameComponents(getFluidInTank(0),s)?drain(s.getAmount(),a):FluidStack.EMPTY;}
 public FluidStack drain(int n,FluidAction action){if(container.getCount()!=1||content()==0||n<1000)return FluidStack.EMPTY;var out=getFluidInTank(0);if(action.execute())container=content()==2?ItemStack.EMPTY:new ItemStack(PrimitiveMachines.BUCKET.get());return out;}
}
