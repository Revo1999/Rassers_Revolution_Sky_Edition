package dk.revo.revocultivation;

import dk.revo.revolib.MachineMenuAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.*;
public class TradingColony extends BaseEntityBlock {
 public TradingColony(){super(Properties.of().strength(3).sound(SoundType.WOOD));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new TradingColony());}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Entity(p,s);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof Entity e)player.openMenu(new SimpleMenuProvider((id,inv,pl)->new Menu(id,inv,e),Component.literal("Retired Trading Colony")));return InteractionResult.sidedSuccess(l.isClientSide);}
 protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(!l.isClientSide&&old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof Entity e)for(int i=0;i<e.items.getSlots();i++)Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),e.items.getStackInSlot(i));super.onRemove(old,l,p,next,moving);}
 public static class Entity extends BlockEntity {
  // Keep the old inventory readable so players can recover items from upgraded saves.
  public final ItemStackHandler items=new ItemStackHandler(27){
   @Override public boolean isItemValid(int slot,ItemStack stack){return false;}
   @Override protected void onContentsChanged(int slot){setChanged();}
  };
  public Entity(BlockPos p,BlockState s){super(RevoCultivation.TRADE_ENTITY.get(),p,s);}
  protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.put("Inventory",items.serializeNBT(p));}
  protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);items.deserializeNBT(p,t.getCompound("Inventory"));}
 }
 public static class ColonyContainer implements net.minecraft.world.Container {
  final Entity e;ColonyContainer(Entity e){this.e=e;}public int getContainerSize(){return 27;}public boolean isEmpty(){for(int i=0;i<27;i++)if(!getItem(i).isEmpty())return false;return true;}public ItemStack getItem(int i){return e.items.getStackInSlot(i);}public ItemStack removeItem(int i,int n){return e.items.extractItem(i,n,false);}public ItemStack removeItemNoUpdate(int i){return removeItem(i,64);}public void setItem(int i,ItemStack s){e.items.setStackInSlot(i,s);}public void setChanged(){e.setChanged();}public boolean stillValid(Player p){return MachineMenuAccess.isUsable(p,e);}public void clearContent(){for(int i=0;i<27;i++)setItem(i,ItemStack.EMPTY);}public boolean canPlaceItem(int i,ItemStack s){return e.items.isItemValid(i,s);}
 }
 public static class Menu extends ChestMenu {
  public Menu(int id,Inventory inv,Entity entity){super(MenuType.GENERIC_9x3,id,inv,new ColonyContainer(entity),3);for(int i=0;i<27;i++){var slot=new Slot(getContainer(),i,8+(i%9)*18,18+(i/9)*18){@Override public boolean mayPlace(ItemStack s){return false;}};slot.index=i;slots.set(i,slot);}}

  @Override
  public ItemStack quickMoveStack(Player player, int slot) {
   if (slot < 0 || slot >= getContainer().getContainerSize()) return ItemStack.EMPTY;
   return super.quickMoveStack(player, slot);
  }
 }
}
