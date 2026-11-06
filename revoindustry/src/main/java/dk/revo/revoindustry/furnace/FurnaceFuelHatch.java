package dk.revo.revoindustry.furnace;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
public final class FurnaceFuelHatch extends HatchBlockEntity {
 public enum Kind{SOLID,LIQUID,GAS}
 public final Kind kind;public final int grade;private final MIInventory inventory;
 private long heat,drawn,tick=Long.MIN_VALUE;
 public FurnaceFuelHatch(BEP bep,Kind kind,int grade){
  super(bep,new MachineGuiParameters.Builder(Furnaces.id(kind.name().toLowerCase(Locale.ROOT)+"_fuel_hatch_"+(grade+1)),true).build(),OrientationComponent.Params.noFacingNoOutput());this.kind=kind;this.grade=grade;
  var slots=new ArrayList<ConfigurableItemStack>();if(kind==Kind.SOLID)for(int i=0;i<new int[]{1,4,9}[grade];i++)slots.add(ConfigurableItemStack.standardIOSlot(true));
  var tanks=kind==Kind.SOLID?List.<ConfigurableFluidStack>of():List.of(ConfigurableFluidStack.standardInputSlot(new long[]{4000,16000,64000}[grade]));
  var positions=new SlotPositions.Builder();for(int i=0;i<slots.size();i++)positions.addSlot(62+(i%3)*18,22+(i/3)*18);
  inventory=new MIInventory(slots,tanks,positions.build(),tanks.isEmpty()?SlotPositions.empty():new SlotPositions.Builder().addSlot(80,40).build());
  installFilters();
  registerComponents(inventory,new MachineComponent(){public void writeNbt(CompoundTag n,HolderLookup.Provider r){n.putLong("FurnaceHeat",heat);}public void readNbt(CompoundTag n,HolderLookup.Provider r,boolean upgrade){heat=Math.max(0,n.getLong("FurnaceHeat"));installFilters();}});
 }
 private void installFilters(){
  for(int i=0;i<inventory.getItemStacks().size();i++){var previous=inventory.getItemStacks().get(i);inventory.getItemStacks().set(i,new ConfigurableItemStack(previous){
   @Override public boolean isValid(ItemStack stack){return super.isValid(stack)&&solidHeat(stack)>0;}
   @Override public boolean isResourceAllowedByLock(Item item){return super.isResourceAllowedByLock(item)&&solidHeat(new ItemStack(item))>0;}
  });}
  for(int i=0;i<inventory.getFluidStacks().size();i++){var previous=inventory.getFluidStacks().get(i);inventory.getFluidStacks().set(i,new ConfigurableFluidStack(previous){
   @Override public boolean isResourceAllowedByLock(Fluid fluid){return super.isResourceAllowedByLock(fluid)&&fluidHeat(fluid)>0;}
  });}
 }
 public HatchType getHatchType(){return Furnaces.FUEL;}public boolean upgradesToSteel(){return grade>0;}public MIInventory getInventory(){return inventory;}
 public long rate(){return new long[]{64,512,4096}[grade];}public long heat(){return heat;}
 public static int solidHeat(ItemStack stack){return stack.isEmpty()||stack.getCapability(Capabilities.FluidHandler.ITEM)!=null?0:stack.getBurnTime(RecipeType.SMELTING);}
 public int fluidHeat(Fluid fluid){if(kind==Kind.SOLID||fluid==Fluids.EMPTY)return 0;boolean gas=fluid.builtInRegistryHolder().is(Tags.Fluids.GASEOUS);if(gas!=(kind==Kind.GAS))return 0;return fluid==Fluids.LAVA?20:Math.max(0,FluidFuel.getEu(fluid)/2);}
 public long takeHeat(long wanted){
  if(level==null||level.isClientSide||!isMatched()||wanted<=0)return 0;
  if(tick!=level.getGameTime()){tick=level.getGameTime();drawn=0;}
  long request=Math.min(wanted,rate()-drawn);if(request<=0)return 0;
  while(heat<request){
   boolean refilled=false;
   if(kind==Kind.SOLID){for(var stack:inventory.getItemStacks()){int burn=solidHeat(stack.toStack());if(burn>0&&ItemStackHelper.consumeFuel(stack,false)){heat+=burn;refilled=true;break;}}}
   else {var stack=inventory.getFluidStacks().getFirst();int burn=fluidHeat(stack.getResource().getFluid());if(burn>0&&stack.getAmount()>0){long amount=Math.min(stack.getAmount(),Math.max(1,(request-heat+burn-1)/burn));stack.decrement(amount);heat+=amount*burn;refilled=true;}}
   if(!refilled)break;
  }
  long moved=Math.min(heat,request);heat-=moved;drawn+=moved;setChanged();return moved;
 }
 public IItemHandler items(){if(kind!=Kind.SOLID)return null;var delegate=inventory.itemStorage.itemHandler;return new IItemHandler(){public int getSlots(){return delegate.getSlots();}public ItemStack getStackInSlot(int i){return delegate.getStackInSlot(i);}public ItemStack insertItem(int i,ItemStack s,boolean sim){return solidHeat(s)>0?delegate.insertItem(i,s,sim):s;}public ItemStack extractItem(int i,int n,boolean sim){return delegate.extractItem(i,n,sim);}public int getSlotLimit(int i){return delegate.getSlotLimit(i);}public boolean isItemValid(int i,ItemStack s){return solidHeat(s)>0;}};}
 public IFluidHandler fluids(){if(kind==Kind.SOLID)return null;var delegate=inventory.fluidStorage.fluidHandler;return new IFluidHandler(){public int getTanks(){return delegate.getTanks();}public FluidStack getFluidInTank(int i){return delegate.getFluidInTank(i);}public int getTankCapacity(int i){return delegate.getTankCapacity(i);}public boolean isFluidValid(int i,FluidStack s){return fluidHeat(s.getFluid())>0;}public int fill(FluidStack s,FluidAction action){return fluidHeat(s.getFluid())>0?delegate.fill(s,action):0;}public FluidStack drain(FluidStack s,FluidAction a){return FluidStack.EMPTY;}public FluidStack drain(int n,FluidAction a){return FluidStack.EMPTY;}};}
}
