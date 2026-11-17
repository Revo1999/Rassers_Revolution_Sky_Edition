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
 private void activity(boolean active){if(working!=active||level.getGameTime()%20==0){working=active;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 public final ItemStackHandler items=new ItemStackHandler(11){public boolean isItemValid(int slot,ItemStack s){return slot==0?(s.is(RevoExtraction.SURVEY_CORE.get())||s.is(RevoExtraction.TUNGSTEN_CORE.get())||s.is(RevoExtraction.NUCLEAR_CORE.get())):slot==1?s.is(Items.WATER_BUCKET):true;}protected void onContentsChanged(int slot){setChanged();}};
 public ElectricDrillEntity(BlockPos p,BlockState s){super(RevoExtraction.ELECTRIC_ENTITY.get(),p,s);}
 public long receive(long n,boolean sim){int accepted=(int)Math.min(Math.max(0,n),32000-energy);if(!sim&&accepted>0){energy+=accepted;setChanged();}return accepted;}
 public long getAmount(){return energy;}public long getCapacity(){return 32000;}
 public boolean needsSurveyCore(){return program==6||program==8;}
 public int cost(){return program==10?512:program==6||program==8||program==9?128:16;}
 public int duration(){return program==6||program==8||program==10?1200:program==9?400:200;}
 public boolean unlocked(){var s=items.getStackInSlot(0);return program==6||program==8?s.is(RevoExtraction.SURVEY_CORE.get()):program==9?s.is(RevoExtraction.TUNGSTEN_CORE.get()):program==10?s.is(RevoExtraction.NUCLEAR_CORE.get()):true;}
 public void select(int next){if(next!=program){getPersistentData().putInt("Cycle"+program,cycle);program=next;cycle=getPersistentData().getInt("Cycle"+program);work=0;setChanged();}}
 public ItemStack output(){if(program==8)return new ItemStack(RevoExtraction.CRYSTALLINE_SLUDGE.get());if(program>=7){String[] extra={"modern_industrialization:raw_antimony","revoextraction:crystalline_sludge","modern_industrialization:raw_tungsten","modern_industrialization:raw_uranium"};return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(extra[program-7])));}String[] ids={"modern_industrialization:raw_silver","modern_industrialization:raw_nickel","modern_industrialization:raw_lead","modern_industrialization:bauxite_dust","modern_industrialization:raw_antimony"};if(program==5)return new ItemStack(Items.ICE);if(program==6)return new ItemStack(cycle%64==63?Items.ANCIENT_DEBRIS:cycle%4==0?Items.GLOWSTONE_DUST:cycle%3==0?Items.SOUL_SAND:Items.NETHERRACK);return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ids[program==0?Math.floorMod(cycle,ids.length):program-1])));}
 public net.neoforged.neoforge.items.IItemHandler outputPort(){return new net.neoforged.neoforge.items.wrapper.RangedWrapper(items,2,11){public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}};}
 public static void tick(Level l,BlockPos p,BlockState s,ElectricDrillEntity e){
  if(l.isClientSide){e.previousAngle=e.angle;if(e.working)e.angle+=24;return;}
  var out=e.output();var port=new net.neoforged.neoforge.items.wrapper.RangedWrapper(e.items,2,11);
  boolean ready=!out.isEmpty()&&e.energy>=e.cost()&&e.unlocked()&&(e.program!=5||e.items.getStackInSlot(1).is(Items.WATER_BUCKET))&&ItemHandlerHelper.insertItemStacked(port,out,true).isEmpty();
  e.activity(ready);if(!ready)return;
  dk.revo.revolib.MachineFeedback.work(l,p,dk.revo.revolib.MachineFeedback.Voice.ELECTRIC_DRILL);
  dk.revo.revolib.MachineFeedback.particles(l,p,net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,.9);
  e.energy-=e.cost();if(++e.work>=e.duration()){ItemHandlerHelper.insertItemStacked(port,out,false);if(e.program==5)e.items.setStackInSlot(1,new ItemStack(Items.BUCKET));e.work=0;e.cycle++;}e.setChanged();
 }
 public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}
 public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.putBoolean("Working",working);t.putInt("Energy",energy);t.putInt("Work",work);t.putInt("Cycle",cycle);t.putInt("Program",program);t.put("Inventory",items.serializeNBT(p));}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);working=t.getBoolean("Working");energy=Math.clamp(t.getInt("Energy"),0,32000);program=Math.clamp(t.getInt("Program"),0,PROGRAMS.length-1);work=Math.clamp(t.getInt("Work"),0,duration()-1);cycle=t.getInt("Cycle");items.deserializeNBT(p,t.getCompound("Inventory"));}
}
