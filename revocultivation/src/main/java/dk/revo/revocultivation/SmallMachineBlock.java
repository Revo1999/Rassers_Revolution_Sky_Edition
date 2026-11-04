package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public class SmallMachineBlock extends Block implements EntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final IntegerProperty FILL=IntegerProperty.create("fill",0,8);
 public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
 public final boolean silk;
 public SmallMachineBlock(boolean silk){super(Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());this.silk=silk;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FILL,0).setValue(STAGE,0));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,FILL,STAGE);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SmallMachineEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==SmallMachines.ENTITY.get()?(world,pos,state,be)->((SmallMachineEntity)be).tick():null;}
 protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
  if(l.getBlockEntity(pos) instanceof SmallMachineEntity m&&m.canHandFeed(stack)){
   if(!l.isClientSide&&m.handFeed(stack,player.getAbilities().instabuild))l.playSound(null,pos,net.minecraft.sounds.SoundEvents.COMPOSTER_FILL,net.minecraft.sounds.SoundSource.BLOCKS,.7f,1);
   return ItemInteractionResult.sidedSuccess(l.isClientSide);
  }
  return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
 }
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
  if(l.getBlockEntity(p) instanceof SmallMachineEntity m){if(!l.isClientSide){player.openMenu(m,b->b.writeBoolean(silk));}return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;
 }
 public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&l.getBlockEntity(p) instanceof SmallMachineEntity m&&!l.isClientSide){for(int i=0;i<3;i++)Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),m.storage.getStackInSlot(i));}super.onRemove(s,l,p,next,moving);}
}

