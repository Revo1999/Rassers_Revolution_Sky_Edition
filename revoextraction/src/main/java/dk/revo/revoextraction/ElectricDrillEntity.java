package dk.revo.revoextraction;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.*;
import dk.revo.revolib.energy.*;
public class ElectricDrillEntity extends BlockEntity implements EuStorage {
 public static final String[] PROGRAMS={"Mixed metals","Silver","Nickel","Lead","Bauxite","Glacial ice","Deep survey","Antimony","Crystalline sludge","Tungsten","Uranium"};
 public int energy,work,cycle,program;
 public boolean working;public float angle,previousAngle;
 private int coolantRemainder;
 private final int[] sides={2,1,0,3,0,0};
 public boolean isDigital(){return getBlockState().is(RevoExtraction.DIGITAL.get());}
 public int sideMode(Direction side){return sides[side.ordinal()];}
 public void cycleSide(Direction side){sides[side.ordinal()]=(sides[side.ordinal()]+1)%4;setChanged();if(level!=null){level.invalidateCapabilities(worldPosition);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 public boolean formed(){return isDigital()||getBlockState().getValue(DrillPart.FORMED);}
 public int waterAmount(){var h=ElectricDrillStructure.hatch(this,-1,-1);return h==null?0:h.getInventory().getFluidStacks().stream().filter(t->t.getResource().getFluid()==net.minecraft.world.level.material.Fluids.WATER).mapToInt(t->(int)t.getAmount()).sum();}
 private boolean coolant(int amount,boolean take){var h=ElectricDrillStructure.hatch(this,-1,-1);if(h==null)return false;for(var t:h.getInventory().getFluidStacks())if(t.getResource().getFluid()==net.minecraft.world.level.material.Fluids.WATER&&t.getAmount()>=amount){if(take){t.decrement(amount);h.setChanged();}return true;}return false;}
 private void transferHatches(){if(!water.isEmpty()){var input=ElectricDrillStructure.hatch(this,-1,-1);int moved=input.getInventory().fluidStorage.fluidHandler.fill(water.getFluid().copy(),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);water.drain(moved,net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);}var h=ElectricDrillStructure.hatch(this,-1,0);if(h!=null){var list=new java.util.ArrayList<aztech.modern_industrialization.machines.components.EnergyComponent>();h.appendEnergyInputs(list);for(var c:list)energy+=(int)c.consumeEu(32000-energy,aztech.modern_industrialization.util.Simulation.ACT);}
  var input=ElectricDrillStructure.hatch(this,0,1);if(input!=null)for(var slot:input.getInventory().getItemStacks())for(int i=0;i<2;i++){var stack=slot.toStack();if(!stack.isEmpty()&&items.isItemValid(i,stack)){var rest=items.insertItem(i,stack,false);slot.decrement(stack.getCount()-rest.getCount());}}
  var output=ElectricDrillStructure.hatch(this,1,0);if(output!=null)for(int i=2;i<11;i++){var stack=items.getStackInSlot(i);if(!stack.isEmpty()&&nativeOutput(stack,true)){nativeOutput(stack,false);items.setStackInSlot(i,ItemStack.EMPTY);}}
 }
 private boolean nativeOutput(ItemStack stack,boolean simulate){var h=ElectricDrillStructure.hatch(this,1,0);if(h==null)return false;int remaining=stack.getCount();var key=aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(stack);for(var slot:h.getInventory().getItemStacks()){if(!slot.isResourceAllowedByLock(key)||!slot.isEmpty()&&!slot.getResource().equals(key))continue;int n=(int)Math.min(remaining,slot.getTotalCapacityFor(stack.getItem())-slot.getAmount());if(n<=0)continue;if(!simulate){slot.setKey(key);slot.increment(n);h.setChanged();}remaining-=n;if(remaining==0)return true;}return false;}
 private boolean canOutput(ItemStack out){if(!isDigital())return nativeOutput(out,true);var copy=new ItemStackHandler(9);for(int i=0;i<9;i++)copy.setStackInSlot(i,items.getStackInSlot(i+2).copy());return ItemHandlerHelper.insertItemStacked(copy,out,false).isEmpty()&&(program==5||ItemHandlerHelper.insertItemStacked(copy,new ItemStack(RevoExtraction.DRY_SEDIMENT.get()),false).isEmpty());}

 public final net.neoforged.neoforge.fluids.capability.templates.FluidTank water=new net.neoforged.neoforge.fluids.capability.templates.FluidTank(8000,f->f.is(net.minecraft.world.level.material.Fluids.WATER)){protected void onContentsChanged(){setChanged();}};
 public Direction facing(){return getBlockState().getValue(ElectricDrill.FACING);}
 public DrillSedimentHatch sedimentHatch(){return ElectricDrillStructure.hatch(this,0,-1) instanceof DrillSedimentHatch h?h:null;}
 public net.minecraft.world.level.material.Fluid sediment(){return BuiltInRegistries.FLUID.get(ResourceLocation.parse("revoindustry:"+(cost()>=128?"excellent_drill_sediment":"good_drill_sediment")));}
 public int sedimentGrade(){return cost()>=128?2:1;}
 private int coolantNeeded(){return (coolantRemainder+100)/duration();}
 /** Glacial Ice remains its existing bucket recipe; all geological drilling is cooled. */
 public int coolingState(){if(isDigital()||program==5)return 0;if(!formed())return 4;var hatch=sedimentHatch();if(hatch==null||hatch.orientation.outputDirection!=facing().getOpposite())return 1;if(!hatch.canAccept(sediment(),Math.max(1,coolantNeeded())))return 2;return !coolant(Math.max(1,coolantNeeded()),false)?3:0;}
 private void activity(boolean active){if(working!=active||level.getGameTime()%20==0){working=active;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 public final ItemStackHandler items=new ItemStackHandler(11){public boolean isItemValid(int slot,ItemStack s){return slot==0?(s.is(RevoExtraction.SURVEY_CORE.get())||s.is(RevoExtraction.TUNGSTEN_CORE.get())||s.is(RevoExtraction.NUCLEAR_CORE.get())):slot==1?s.is(Items.WATER_BUCKET):true;}protected void onContentsChanged(int slot){setChanged();}};
 public ElectricDrillEntity(BlockPos p,BlockState s){super(RevoExtraction.ELECTRIC_ENTITY.get(),p,s);}
 public long receive(long n,boolean sim){int accepted=(int)Math.min(Math.max(0,n),32000-energy);if(!sim&&accepted>0){energy+=accepted;setChanged();}return accepted;}
 public long getAmount(){return energy;}public long getCapacity(){return 32000;}
 public boolean needsSurveyCore(){return program==6||program==8;}
 public int cost(){return isDigital()?512:program==10?512:program==6||program==8||program==9?128:16;}
 public int duration(){return program==6||program==8||program==10?1200:program==9?400:200;}
 public boolean unlocked(){var s=items.getStackInSlot(0);return program==6||program==8?s.is(RevoExtraction.SURVEY_CORE.get()):program==9?s.is(RevoExtraction.TUNGSTEN_CORE.get()):program==10?s.is(RevoExtraction.NUCLEAR_CORE.get()):true;}
 public void select(int next){if(next!=program){getPersistentData().putInt("Cycle"+program,cycle);program=next;cycle=getPersistentData().getInt("Cycle"+program);work=0;coolantRemainder=0;setChanged();}}
 public ItemStack output(){if(program==8)return new ItemStack(RevoExtraction.CRYSTALLINE_SLUDGE.get());if(program>=7){String[] extra={"modern_industrialization:raw_antimony","revoextraction:crystalline_sludge","modern_industrialization:raw_tungsten","modern_industrialization:raw_uranium"};return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(extra[program-7])));}String[] ids={"modern_industrialization:raw_silver","modern_industrialization:raw_nickel","modern_industrialization:raw_lead","modern_industrialization:bauxite_dust","modern_industrialization:raw_antimony"};if(program==5)return new ItemStack(Items.ICE);if(program==6)return new ItemStack(cycle%64==63?Items.ANCIENT_DEBRIS:cycle%4==0?Items.GLOWSTONE_DUST:cycle%3==0?Items.SOUL_SAND:Items.NETHERRACK);return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ids[program==0?Math.floorMod(cycle,ids.length):program-1])));}
 public IItemHandler menuOutput(){if(isDigital())return outputPort();return new IItemHandlerModifiable(){public void setStackInSlot(int slot,ItemStack stack){var h=ElectricDrillStructure.hatch(ElectricDrillEntity.this,1,0);if(h==null||slot>=h.getInventory().getItemStacks().size())return;var target=h.getInventory().getItemStacks().get(slot);target.setKey(aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant.of(stack));target.setAmount(stack.getCount());h.setChanged();}private IItemHandler nativePort(){var h=ElectricDrillStructure.hatch(ElectricDrillEntity.this,1,0);return h==null?null:h.getInventory().itemStorage.itemHandler;}public int getSlots(){return 9;}public ItemStack getStackInSlot(int slot){var h=nativePort();return h==null||slot>=h.getSlots()?ItemStack.EMPTY:h.getStackInSlot(slot);}public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}public ItemStack extractItem(int slot,int amount,boolean simulate){var h=nativePort();return h==null||slot>=h.getSlots()?ItemStack.EMPTY:h.extractItem(slot,amount,simulate);}public int getSlotLimit(int slot){return 64;}public boolean isItemValid(int slot,ItemStack stack){return false;}};}
 public net.neoforged.neoforge.items.IItemHandler outputPort(){return new net.neoforged.neoforge.items.wrapper.RangedWrapper(items,2,11){public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}};}
 public static void tick(Level l,BlockPos p,BlockState s,ElectricDrillEntity e){
  if(l.isClientSide){e.previousAngle=e.angle;if(e.working)e.angle+=24;return;}
  if(!e.isDigital()){if(!ElectricDrillStructure.validate(e)){e.activity(false);return;}e.transferHatches();}
  var out=e.output();var port=new net.neoforged.neoforge.items.wrapper.RangedWrapper(e.items,2,11);
  boolean ready=!out.isEmpty()&&e.energy>=e.cost()&&e.unlocked()&&e.coolingState()==0&&(e.program!=5||e.items.getStackInSlot(1).is(Items.WATER_BUCKET))&&e.canOutput(out);
  e.activity(ready);if(!ready)return;
  if(!e.isDigital()&&e.program!=5){int needed=e.coolantNeeded();e.coolant(needed,true);e.sedimentHatch().acceptCoolant(e.sediment(),needed);e.coolantRemainder=(e.coolantRemainder+100)%e.duration();}
  dk.revo.revolib.MachineFeedback.work(l,p,dk.revo.revolib.MachineFeedback.Voice.ELECTRIC_DRILL);
  dk.revo.revolib.MachineFeedback.particles(l,p,net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,.9);
  e.energy-=e.cost();if(++e.work>=e.duration()){if(e.isDigital()){ItemHandlerHelper.insertItemStacked(port,out,false);if(e.program!=5)ItemHandlerHelper.insertItemStacked(port,new ItemStack(RevoExtraction.DRY_SEDIMENT.get()),false);}else e.nativeOutput(out,false);if(e.program==5)e.items.setStackInSlot(1,new ItemStack(Items.BUCKET));e.work=0;e.cycle++;}e.setChanged();
 }
 public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}
 public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.putIntArray("SideModes",sides);t.putBoolean("Working",working);t.putInt("Energy",energy);t.putInt("Work",work);t.putInt("Cycle",cycle);t.putInt("Program",program);t.put("Inventory",items.serializeNBT(p));t.put("CoolingWater",water.writeToNBT(p,new CompoundTag()));t.putInt("CoolantRemainder",coolantRemainder);}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);if(t.contains("SideModes")){var a=t.getIntArray("SideModes");for(int i=0;i<Math.min(6,a.length);i++)sides[i]=Math.clamp(a[i],0,3);}working=t.getBoolean("Working");energy=Math.clamp(t.getInt("Energy"),0,32000);program=Math.clamp(t.getInt("Program"),0,PROGRAMS.length-1);work=Math.clamp(t.getInt("Work"),0,duration()-1);cycle=t.getInt("Cycle");items.deserializeNBT(p,t.getCompound("Inventory"));water.readFromNBT(p,t.getCompound("CoolingWater"));coolantRemainder=Math.clamp(t.getInt("CoolantRemainder"),0,duration()-1);}
}
