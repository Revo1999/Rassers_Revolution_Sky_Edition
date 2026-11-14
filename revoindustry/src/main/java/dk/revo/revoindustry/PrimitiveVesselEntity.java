package dk.revo.revoindustry;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.*;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
public class PrimitiveVesselEntity extends BlockEntity implements MenuProvider{
 public static final TagKey<Item> DUST=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","dusts/stone"));
 public final ItemStackHandler inventory=new ItemStackHandler(2){public boolean isItemValid(int slot,ItemStack stack){return slot==0&&accepts(stack);}protected void onContentsChanged(int slot){changed();}};
 public final FluidTank tank=new FluidTank(1000,f->f.is(rain()?Fluids.WATER:Fluids.LAVA)){protected void onContentsChanged(){changed();}};
 private final IItemHandler input=new RangedWrapper(inventory,0,1),output=new RangedWrapper(inventory,1,2);
 private int work;
 public PrimitiveVesselEntity(BlockPos p,BlockState s){super(PrimitiveMachines.ENTITY.get(),p,s);}
 public boolean rain(){return ((PrimitiveVesselBlock)getBlockState().getBlock()).rain;}
 public boolean accepts(ItemStack stack){return rain()?stack.is(DUST):stack.is(Items.COBBLESTONE);}
 public boolean canFeed(ItemStack stack){return accepts(stack)&&inventory.insertItem(0,stack.copyWithCount(1),true).isEmpty();}
 public boolean feed(ItemStack held,boolean creative){if(level==null||level.isClientSide||!canFeed(held))return false;inventory.insertItem(0,held.copyWithCount(1),false);if(!creative)held.shrink(1);if(rain())mix();return true;}
 public IItemHandler items(Direction side){return side==null?inventory:side==Direction.DOWN||side==getBlockState().getValue(PrimitiveVesselBlock.FACING)?output:input;}
 public IFluidHandler fluids(){return tank;}
 public int progress(){return work;}
 public int heat(){if(level==null)return 0;var s=level.getBlockState(worldPosition.below());if(s.hasProperty(BlockStateProperties.LIT)&&!s.getValue(BlockStateProperties.LIT))return 0;if(s.is(Blocks.LAVA))return 4;if(s.is(Blocks.FIRE)||s.is(Blocks.SOUL_FIRE))return 3;if(s.is(Blocks.CAMPFIRE)||s.is(Blocks.SOUL_CAMPFIRE)||s.is(Blocks.FURNACE)||s.is(Blocks.BLAST_FURNACE)||s.is(Blocks.SMOKER))return 2;if(s.is(Blocks.TORCH)||s.is(Blocks.SOUL_TORCH)||s.is(Blocks.MAGMA_BLOCK))return 1;return 0;}
 public int status(){if(rain()){if(tank.getFluidAmount()==1000)return 5;if(!level.canSeeSky(worldPosition.above()))return 2;return level.isRainingAt(worldPosition.above())?0:1;}if(tank.getFluidAmount()>750)return 5;if(inventory.getStackInSlot(0).isEmpty())return 3;return heat()==0?4:0;}
 public void tick(){if(level==null||level.isClientSide)return;if(status()==0){dk.revo.revolib.MachineFeedback.work(level,worldPosition,rain()?dk.revo.revolib.MachineFeedback.Voice.RAIN:dk.revo.revolib.MachineFeedback.Voice.CRUCIBLE);dk.revo.revolib.MachineFeedback.particles(level,worldPosition,rain()?net.minecraft.core.particles.ParticleTypes.RAIN:net.minecraft.core.particles.ParticleTypes.SMOKE,.95);}if(rain()){if(level.canSeeSky(worldPosition.above())&&level.isRainingAt(worldPosition.above()))tank.fill(new FluidStack(Fluids.WATER,5),IFluidHandler.FluidAction.EXECUTE);mix();}else if(status()==0){work+=heat();setChanged();if(work>=200){work-=200;inventory.extractItem(0,1,false);tank.fill(new FluidStack(Fluids.LAVA,250),IFluidHandler.FluidAction.EXECUTE);changed();}}if(level.getGameTime()%20==0){eject();changed();}}
 public boolean mix(){if(!rain()||tank.getFluidAmount()!=1000||!inventory.getStackInSlot(0).is(DUST))return false;var out=inventory.getStackInSlot(1);if(!out.isEmpty()&&(!out.is(Items.CLAY)||out.getCount()>=64))return false;dk.revo.revolib.MachineFeedback.work(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.RAIN);inventory.extractItem(0,1,false);tank.drain(1000,IFluidHandler.FluidAction.EXECUTE);if(out.isEmpty())inventory.setStackInSlot(1,new ItemStack(Items.CLAY));else {out.grow(1);inventory.setStackInSlot(1,out);}return true;}
 public void eject(){if(inventory.getStackInSlot(1).isEmpty()||level==null)return;var face=getBlockState().getValue(PrimitiveVesselBlock.FACING);var pos=worldPosition.relative(face);if(!level.hasChunkAt(pos))return;var target=level.getCapability(Capabilities.ItemHandler.BLOCK,pos,face.getOpposite());if(target!=null){var out=inventory.getStackInSlot(1);var left=ItemHandlerHelper.insertItemStacked(target,out.copy(),false);if(left.getCount()!=out.getCount())inventory.setStackInSlot(1,left);}}
 public boolean isBucket(ItemStack s){return s.is(Items.BUCKET)||s.is(Items.WATER_BUCKET)||s.is(Items.LAVA_BUCKET)||s.getItem() instanceof ClayBucketItem;}
 public boolean bucket(Player p,net.minecraft.world.InteractionHand hand){var s=p.getItemInHand(hand);boolean clay=s.getItem() instanceof ClayBucketItem;int type=clay?((ClayBucketItem)s.getItem()).content:s.is(Items.WATER_BUCKET)?1:s.is(Items.LAVA_BUCKET)?2:0;if(type==0){if(tank.getFluidAmount()!=1000)return false;dk.revo.revolib.MachineFeedback.cue(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.POUR);boolean water=tank.getFluid().is(Fluids.WATER);tank.drain(1000,IFluidHandler.FluidAction.EXECUTE);ClayBucketItem.exchange(p,hand,new ItemStack(clay?(water?PrimitiveMachines.WATER_BUCKET.get():PrimitiveMachines.LAVA_BUCKET.get()):(water?Items.WATER_BUCKET:Items.LAVA_BUCKET)));return true;}var fluid=new FluidStack(type==1?Fluids.WATER:Fluids.LAVA,1000);if(tank.fill(fluid,IFluidHandler.FluidAction.SIMULATE)!=1000)return false;tank.fill(fluid,IFluidHandler.FluidAction.EXECUTE);dk.revo.revolib.MachineFeedback.cue(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.POUR);ClayBucketItem.exchange(p,hand,clay&&type==2?ItemStack.EMPTY:new ItemStack(clay?PrimitiveMachines.BUCKET.get():Items.BUCKET));if(rain())mix();return true;}
 private void changed(){setChanged();if(level!=null&&!level.isClientSide&&!isRemoved()){var state=getBlockState();int fill=(tank.getFluidAmount()*8+999)/1000;if(state.getValue(PrimitiveVesselBlock.LEVEL)!=fill)level.setBlock(worldPosition,state.setValue(PrimitiveVesselBlock.LEVEL,fill),2);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.put("Inventory",inventory.serializeNBT(lookup));tag.put("Tank",tank.writeToNBT(lookup,new CompoundTag()));tag.putInt("Work",work);}
 protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);inventory.deserializeNBT(lookup,tag.getCompound("Inventory"));tank.readFromNBT(lookup,tag.getCompound("Tank"));work=Math.clamp(tag.getInt("Work"),0,199);}
 public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
 public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 public Component getDisplayName(){return getBlockState().getBlock().getName();}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new PrimitiveVesselMenu(id,inv,this);}
 public int value(int i){return switch(i){case 0->tank.getFluidAmount();case 1->work;case 2->status();default->heat();};}
}

