package dk.revo.revocultivation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
public class SmallMachineEntity extends BlockEntity implements MenuProvider {
 public final ItemStackHandler storage=new ItemStackHandler(3){
  protected void onContentsChanged(int slot){setChanged();if(level!=null&&!level.isClientSide){updateVisualState();sendData();}}
  public boolean isItemValid(int slot,ItemStack stack){return slot==0?accepts(stack):slot==1&&silk()&&stack.is(SmallMachines.WORM.get());}
  public int getSlotLimit(int slot){return slot==1?1:64;}
 };
 private final IItemHandler input=new RangedWrapper(storage,0,2);
 private final IItemHandler output=new RangedWrapper(storage,2,3);
 private int work,cycles;
 private float previousFill,animatedFill;
 public SmallMachineEntity(BlockPos p,BlockState s){super(SmallMachines.ENTITY.get(),p,s);}
 public boolean silk(){return ((SmallMachineBlock)getBlockState().getBlock()).silk;}
 public boolean accepts(ItemStack s){return silk()?s.is(ItemTags.LEAVES):!s.is(SmallMachines.COMPOST.get())&&ComposterBlock.COMPOSTABLES.getFloat(s.getItem())>0;}
 public int duration(){return silk()?400:160;}
 public int progress(){return work;}
 public int cycles(){return cycles;}
 public int feedCount(){return silk()?2:4;}
 public boolean canHandFeed(ItemStack stack){if(stack.isEmpty()||!accepts(stack)&&!(silk()&&stack.is(SmallMachines.WORM.get())))return false;int slot=stack.is(SmallMachines.WORM.get())?1:0;return storage.insertItem(slot,stack.copyWithCount(1),true).isEmpty();}
 /** Insert one item through the same validated inventory used by menus and hoppers. */
 public boolean handFeed(ItemStack held,boolean creative){if(level==null||level.isClientSide||held.isEmpty()||!canHandFeed(held))return false;int slot=held.is(SmallMachines.WORM.get())?1:0;var one=held.copyWithCount(1);if(!storage.insertItem(slot,one,true).isEmpty())return false;storage.insertItem(slot,one,false);if(!creative)held.shrink(1);return true;}
 /** 0=empty, 1=loading/waiting, 2=working, 3=finished product awaiting export. */
 public int visualStage(){boolean feed=accepts(storage.getStackInSlot(0))&&!storage.getStackInSlot(0).isEmpty();if(!storage.getStackInSlot(2).isEmpty()&&(!feed||status()!=0))return 3;if(!feed)return 0;return work>0?2:1;}
 public int fillLevel(){if(visualStage()==3)return 8;var feed=storage.getStackInSlot(0);return accepts(feed)?Math.min(8,feed.getCount()*8/feedCount()):0;}
 public float renderedFill(float partial){return net.minecraft.util.Mth.lerp(partial,previousFill,animatedFill);}
 public float processFraction(){return (float)work/duration();}
 public void updateVisualState(){if(level==null||level.isClientSide||isRemoved())return;var state=getBlockState();int fill=fillLevel(),stage=visualStage();if(state.getValue(SmallMachineBlock.FILL)!=fill||state.getValue(SmallMachineBlock.STAGE)!=stage)level.setBlock(worldPosition,state.setValue(SmallMachineBlock.FILL,fill).setValue(SmallMachineBlock.STAGE,stage),2);}
 private ItemStack productTemplate;
 private ItemStack productTemplate(){if(productTemplate==null)productTemplate=new ItemStack(silk()?SmallMachines.COCOON.get():SmallMachines.COMPOST.get(),silk()?2:1);return productTemplate;}
 public ItemStack product(){return productTemplate().copy();}
 public int status(){if(!accepts(storage.getStackInSlot(0))||storage.getStackInSlot(0).getCount()<feedCount())return 1;if(silk()&&!storage.getStackInSlot(1).is(SmallMachines.WORM.get()))return 2;if(!fits())return 3;return 0;}
 private boolean fits(){var out=storage.getStackInSlot(2);var made=productTemplate();return out.isEmpty()||ItemStack.isSameItemSameComponents(out,made)&&out.getCount()+made.getCount()<=Math.min(out.getMaxStackSize(),64);}
 public IItemHandler items(Direction side){return side==null?storage:side==Direction.DOWN||side==getBlockState().getValue(SmallMachineBlock.FACING)?output:input;}
 public void tick(){if(level==null)return;if(level.isClientSide){previousFill=animatedFill;animatedFill=net.minecraft.util.Mth.approach(animatedFill,fillLevel()/8f,.12f);return;}if(level.getGameTime()%20==0)eject();int st=status();if(st==0)advance(1);updateVisualState();if(st==0&&level.getGameTime()%10==0)sendData();}
 private void advance(int n){work=Math.min(duration(),work+n);setChanged();if(work<duration())return;if(!fits())return;storage.extractItem(0,feedCount(),false);var made=product();var out=storage.getStackInSlot(2);if(out.isEmpty())storage.setStackInSlot(2,made);else {out.grow(made.getCount());storage.setStackInSlot(2,out);}work=0;cycles++;eject();sendData();}
 public void eject(){if(level==null||level.isClientSide)return;var face=getBlockState().getValue(SmallMachineBlock.FACING);var target=worldPosition.relative(face);if(!level.hasChunkAt(target))return;var handler=level.getCapability(Capabilities.ItemHandler.BLOCK,target,face.getOpposite());if(handler==null)return;var out=storage.getStackInSlot(2);if(out.isEmpty())return;var remaining=ItemHandlerHelper.insertItemStacked(handler,out.copy(),false);if(remaining.getCount()!=out.getCount())storage.setStackInSlot(2,remaining);}
 @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.put("Storage",storage.serializeNBT(lookup));tag.putInt("Work",work);tag.putInt("Cycles",cycles);}
 @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);storage.deserializeNBT(lookup,tag.getCompound("Storage"));work=Math.clamp(tag.getInt("Work"),0,duration());cycles=Math.max(0,tag.getInt("Cycles"));}
 public void sendData(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 public Component getDisplayName(){return getBlockState().getBlock().getName();}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SmallMachineMenu(id,inv,this);}
 public int value(int index){return switch(index){case 0->work;case 1->status();case 2->duration()/20;case 4->fillLevel();case 5->visualStage();default->cycles;};}
}

