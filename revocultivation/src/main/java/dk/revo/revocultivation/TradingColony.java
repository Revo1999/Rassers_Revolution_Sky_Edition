package dk.revo.revocultivation;
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
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,RevoCultivation.TRADE_ENTITY.get(),Entity::tick);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof Entity e)player.openMenu(new SimpleMenuProvider((id,inv,pl)->new Menu(id,inv,e),Component.literal("Trading Colony: 16 crops = 1 emerald")));return InteractionResult.sidedSuccess(l.isClientSide);}
 protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof Entity e)for(int i=0;i<27;i++)Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),e.items.getStackInSlot(i));super.onRemove(old,l,p,next,moving);}
 public static class Entity extends BlockEntity {
  public int work;
  public final ItemStackHandler items=new ItemStackHandler(27){public boolean isItemValid(int slot,ItemStack stack){return slot<26&&(stack.is(Items.WHEAT)||stack.is(Items.PAPER));}protected void onContentsChanged(int slot){setChanged();}};
  public Entity(BlockPos p,BlockState s){super(RevoCultivation.TRADE_ENTITY.get(),p,s);}
  public static void tick(Level l,BlockPos p,BlockState s,Entity e){if(l.isClientSide)return;int input=-1;for(int i=0;i<26;i++)if(e.items.getStackInSlot(i).getCount()>=16){input=i;break;}if(input<0||!e.items.getStackInSlot(26).isEmpty()&&(!e.items.getStackInSlot(26).is(Items.EMERALD)||e.items.getStackInSlot(26).getCount()>=64))return;dk.revo.revolib.MachineFeedback.work(l,p,dk.revo.revolib.MachineFeedback.Voice.TRADE);if(++e.work>=200){e.items.extractItem(input,16,false);var output=e.items.getStackInSlot(26);e.items.setStackInSlot(26,output.isEmpty()?new ItemStack(Items.EMERALD):output.copyWithCount(output.getCount()+1));e.work=0;}e.setChanged();}
  protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.put("Inventory",items.serializeNBT(p));t.putInt("Work",work);}
  protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);items.deserializeNBT(p,t.getCompound("Inventory"));work=Math.clamp(t.getInt("Work"),0,199);}
 }
 public static class ColonyContainer implements net.minecraft.world.Container {
  final Entity e;ColonyContainer(Entity e){this.e=e;}public int getContainerSize(){return 27;}public boolean isEmpty(){for(int i=0;i<27;i++)if(!getItem(i).isEmpty())return false;return true;}public ItemStack getItem(int i){return e.items.getStackInSlot(i);}public ItemStack removeItem(int i,int n){return e.items.extractItem(i,n,false);}public ItemStack removeItemNoUpdate(int i){return removeItem(i,64);}public void setItem(int i,ItemStack s){e.items.setStackInSlot(i,s);}public void setChanged(){e.setChanged();}public boolean stillValid(Player p){return !e.isRemoved()&&p.distanceToSqr(e.getBlockPos().getCenter())<=64;}public void clearContent(){for(int i=0;i<27;i++)setItem(i,ItemStack.EMPTY);}public boolean canPlaceItem(int i,ItemStack s){return e.items.isItemValid(i,s);}
 }
 public static class Menu extends ChestMenu {
  public Menu(int id,Inventory inv,Entity entity){super(MenuType.GENERIC_9x3,id,inv,new ColonyContainer(entity),3);for(int i=0;i<27;i++){final int n=i;var slot=new Slot(getContainer(),i,8+(i%9)*18,18+(i/9)*18){public boolean mayPlace(ItemStack s){return n<26&&(s.is(Items.WHEAT)||s.is(Items.PAPER));}};slot.index=i;slots.set(i,slot);}}
 }
}
