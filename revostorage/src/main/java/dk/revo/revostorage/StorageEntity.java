package dk.revo.revostorage;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.*;
/** Every bay owns its inventory. Removing a nonempty chest is refused, never deletes storage. */
public class StorageEntity extends BlockEntity {
 private static long topologyVersion;
 private static final Map<net.minecraft.world.item.Item,Integer> CHEST_SIZES=new HashMap<>();
 public int charge;
 public final NonNullList<ItemStack> craftingItems=NonNullList.withSize(9,ItemStack.EMPTY);
 public final ItemStackHandler[] banks=new ItemStackHandler[9];
 public final ItemStackHandler chests=new ItemStackHandler(9){
  public int getSlotLimit(int slot){return 1;}
  public boolean isItemValid(int slot,ItemStack s){return chestCapacity(s)>0&&emptyBay(slot);}
  public ItemStack extractItem(int slot,int amount,boolean sim){return emptyBay(slot)?super.extractItem(slot,amount,sim):ItemStack.EMPTY;}
  protected void onContentsChanged(int slot){topologyVersion++;setChanged();}
 };
 public StorageEntity(BlockPos p,BlockState s){super(RevoStorage.ENTITY.get(),p,s);for(int i=0;i<9;i++)banks[i]=new ItemStackHandler(108){protected void onContentsChanged(int slot){setChanged();}};}
 public boolean terminal(){return getBlockState().is(RevoStorage.TERMINAL.get());}
 public void wind(){if(level!=null&&!level.isClientSide)level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.COPPER_BULB_TURN_ON,net.minecraft.sounds.SoundSource.BLOCKS,.3f,.8f);charge=Math.min(4096,charge+256);setChanged();}
 public boolean emptyBay(int bay){if(banks[bay]==null)return true;for(int i=0;i<108;i++)if(!banks[bay].getStackInSlot(i).isEmpty())return false;return true;}
 public static int chestCapacity(ItemStack stack){
  if(stack.isEmpty()||!(stack.getItem() instanceof BlockItem item))return 0;
  // Filled portable chests must be emptied before installation; their components are never discarded.
  if(stack.has(DataComponents.BLOCK_ENTITY_DATA)||stack.has(DataComponents.CONTAINER)&&stack.get(DataComponents.CONTAINER).nonEmptyStream().findAny().isPresent())return 0;
  var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
  if(stack.is(Items.CHEST)||stack.is(Items.TRAPPED_CHEST))return 27;
  if(!id.getNamespace().equals("ironchest"))return 0;
  if(CHEST_SIZES.containsKey(stack.getItem()))return CHEST_SIZES.get(stack.getItem());
  if(item.getBlock() instanceof EntityBlock block){var be=block.newBlockEntity(BlockPos.ZERO,item.getBlock().defaultBlockState());if(be instanceof Container container){int n=container.getContainerSize();int size=n>0&&n<=108?n:0;CHEST_SIZES.put(stack.getItem(),size);return size;}}
  return 0;
 }
 public int capacity(){int n=0;for(int i=0;i<9;i++)n+=chestCapacity(chests.getStackInSlot(i));return n;}
 public List<StorageEntity> drives(){
  if(level==null)return List.of();var result=new ArrayList<StorageEntity>();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(worldPosition);seen.add(worldPosition);
  while(!queue.isEmpty()){var pos=queue.remove();if(!level.hasChunkAt(pos)||!(level.getBlockEntity(pos) instanceof StorageEntity e))continue;if(!e.terminal())result.add(e);for(var d:Direction.values()){var next=pos.relative(d);if(seen.size()<128&&!seen.contains(next)&&level.hasChunkAt(next)&&level.getBlockEntity(next) instanceof StorageEntity){seen.add(next);queue.add(next);}}}
  return result;
 }
 private final IItemHandler network=new IItemHandler(){
  private record Address(StorageEntity entity,int bay,int slot){}
  private final List<Address> addresses=new ArrayList<>();private long lastTick=Long.MIN_VALUE,lastVersion=-1;
  private void refresh(){long tick=level==null?0:level.getGameTime();if(lastTick==tick&&lastVersion==topologyVersion)return;lastTick=tick;lastVersion=topologyVersion;addresses.clear();for(var e:drives())for(int b=0;b<9;b++)for(int i=0;i<chestCapacity(e.chests.getStackInSlot(b));i++)addresses.add(new Address(e,b,i));}
  private Address at(int slot){refresh();return slot>=0&&slot<addresses.size()&&!addresses.get(slot).entity.isRemoved()?addresses.get(slot):null;}
  public int getSlots(){refresh();return addresses.size();}
  public ItemStack getStackInSlot(int slot){var a=at(slot);return a==null?ItemStack.EMPTY:a.entity.banks[a.bay].getStackInSlot(a.slot);}
  public ItemStack insertItem(int slot,ItemStack s,boolean sim){var a=at(slot);return a==null?s:a.entity.banks[a.bay].insertItem(a.slot,s,sim);}
  public ItemStack extractItem(int slot,int n,boolean sim){var a=at(slot);return a==null?ItemStack.EMPTY:a.entity.banks[a.bay].extractItem(a.slot,n,sim);}
  public int getSlotLimit(int slot){return 64;}
  public boolean isItemValid(int slot,ItemStack s){return at(slot)!=null;}
 };
 public IItemHandler networkItems(){return network;}
 @Override public void setRemoved(){topologyVersion++;super.setRemoved();}
 @Override public void onLoad(){super.onLoad();topologyVersion++;}
 public ItemStack insert(ItemStack s,boolean simulate){return ItemHandlerHelper.insertItemStacked(networkItems(),s,simulate);}
 public ItemStack take(ItemStack key,int amount,boolean simulate){var h=networkItems();ItemStack result=ItemStack.EMPTY;for(int i=0;i<h.getSlots()&&amount>0;i++){var s=h.getStackInSlot(i);if(ItemStack.isSameItemSameComponents(s,key)){var part=h.extractItem(i,amount,simulate);if(result.isEmpty())result=part;else result.grow(part.getCount());amount-=part.getCount();}}return result;}
 public void dropContents(){if(level==null||level.isClientSide)return;for(var bank:banks)for(int i=0;i<108;i++)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),bank.getStackInSlot(i));for(var stack:craftingItems)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),stack);for(var h:List.of(chests))for(int i=0;i<h.getSlots();i++)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),h.getStackInSlot(i));}
 public static void tick(Level l,BlockPos p,BlockState s,StorageEntity e){if(!l.isClientSide&&e.terminal()&&l.getGameTime()%20==0){for(var d:Direction.values()){var be=l.getBlockEntity(p.relative(d));if(be!=null&&be.getClass().getName().startsWith("com.simibubi.create.")){try{var method=be.getClass().getMethod("getSpeed");if(Math.abs(((Number)method.invoke(be)).floatValue())>=16){if(e.charge<4096)dk.revo.revolib.MachineFeedback.work(l,p,dk.revo.revolib.MachineFeedback.Voice.STORAGE);e.charge=Math.min(4096,e.charge+32);e.setChanged();break;}}catch(ReflectiveOperationException ignored){}}}}}
 protected void saveAdditional(CompoundTag tag,HolderLookup.Provider p){super.saveAdditional(tag,p);tag.putInt("Charge",charge);tag.put("Chests",chests.serializeNBT(p));var craft=new CompoundTag();ContainerHelper.saveAllItems(craft,craftingItems,p);tag.put("Crafting",craft);for(int i=0;i<9;i++)tag.put("Bay"+i,banks[i].serializeNBT(p));}
 protected void loadAdditional(CompoundTag tag,HolderLookup.Provider p){super.loadAdditional(tag,p);charge=Math.clamp(tag.getInt("Charge"),0,4096);chests.deserializeNBT(p,tag.getCompound("Chests"));ContainerHelper.loadAllItems(tag.getCompound("Crafting"),craftingItems,p);for(int i=0;i<9;i++)banks[i].deserializeNBT(p,tag.getCompound("Bay"+i));}
}
