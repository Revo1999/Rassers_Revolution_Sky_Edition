package dk.revo.revoindustry.vacuum;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
public final class VacuumEntity extends BlockEntity implements MenuProvider {
 public final ItemStackHandler inventory;
 private int cooldown;
 public VacuumEntity(BlockPos p,BlockState s){super(Vacuums.ENTITY.get(),p,s);inventory=new ItemStackHandler(Vacuums.slots(tier())){protected void onContentsChanged(int slot){setChanged();}};}
 public int tier(){return ((VacuumBlock)getBlockState().getBlock()).tier;}
 public Direction outlet(){return getBlockState().getValue(VacuumBlock.OUTLET);}
 public Vec3 intake(){return Vacuums.mouth(worldPosition,outlet()); }
 public boolean accepts(ItemStack stack){return ItemHandlerHelper.insertItemStacked(inventory,stack,true).getCount()<stack.getCount();}
 public void tick(){
  if(!(level instanceof ServerLevel server))return;
  if(cooldown>0)cooldown--;
  if(cooldown==0){transfer();cooldown=Vacuums.interval(tier());setChanged();}
  if(level.getGameTime()%4!=0)return;
  var centre=Vec3.atCenterOf(worldPosition);var area=Vacuums.area(worldPosition,tier(),outlet());
  var found=level.getEntitiesOfClass(ItemEntity.class,area,e->e.isAlive()&&!e.getItem().isEmpty()&&area.contains(e.position()));
  found.sort(java.util.Comparator.comparingDouble(e->e.position().distanceToSqr(centre)));
  var target=intake();
  int handled=0;
  for(var entity:found){
   if(!accepts(entity.getItem()))continue;
   // Items stay in-world while being pulled; capacity is rechecked on every scan.
   var delta=target.subtract(entity.position());double distance=delta.length();
   if(distance<.8){var old=entity.getItem();var left=ItemHandlerHelper.insertItemStacked(inventory,old.copy(),false);if(left.getCount()!=old.getCount()){if(left.isEmpty())entity.discard();else entity.setItem(left);} }
   else {entity.setDeltaMovement(delta.normalize().scale(Math.min(.42,.12+distance*.04)));entity.hurtMarked=true;}
   if(distance>.2){var velocity=delta.normalize().scale(.16);server.sendParticles(ParticleTypes.END_ROD,entity.getX(),entity.getY()+.1,entity.getZ(),0,velocity.x,velocity.y,velocity.z,1);}
   dk.revo.revolib.MachineFeedback.work(level,worldPosition,tier()==0?dk.revo.revolib.MachineFeedback.Voice.VACUUM:tier()==1?dk.revo.revolib.MachineFeedback.Voice.VACUUM_2:dk.revo.revolib.MachineFeedback.Voice.VACUUM_3);
   if(++handled>=12)break;
  }
 }
 public void transfer(){
  if(level==null)return;var face=outlet();var pos=worldPosition.relative(face);if(!level.hasChunkAt(pos))return;
  var target=level.getCapability(Capabilities.ItemHandler.BLOCK,pos,face.getOpposite());if(target==null||target==inventory)return;
  for(int slot=0;slot<inventory.getSlots();slot++){var stack=inventory.getStackInSlot(slot);if(stack.isEmpty())continue;var left=ItemHandlerHelper.insertItemStacked(target,stack.copyWithCount(1),false);if(left.isEmpty()){inventory.extractItem(slot,1,false);return;}}
 }
 protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.put("Inventory",inventory.serializeNBT(lookup));tag.putInt("Cooldown",cooldown);}
 protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);inventory.deserializeNBT(lookup,tag.getCompound("Inventory"));cooldown=Math.clamp(tag.getInt("Cooldown"),0,Vacuums.interval(tier()));}
 public Component getDisplayName(){return getBlockState().getBlock().getName();}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new VacuumMenu(id,inv,this);}
}
