package dk.revo.revoindustry;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public class PrimitiveVesselBlock extends Block implements EntityBlock{
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final IntegerProperty LEVEL=IntegerProperty.create("fill",0,8);
 public final boolean rain;
 public PrimitiveVesselBlock(boolean rain){super(Properties.of().strength(rain?2:2.5f).sound(rain?SoundType.WOOD:SoundType.STONE).noOcclusion().lightLevel(s->!rain&&s.getValue(LEVEL)>0?8:0));this.rain=rain;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LEVEL,0));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LEVEL);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PrimitiveVesselEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==PrimitiveMachines.ENTITY.get()?(world,pos,state,be)->((PrimitiveVesselEntity)be).tick():null;}
 protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){if(l.getBlockEntity(pos) instanceof PrimitiveVesselEntity m){if(m.isBucket(stack)){if(!l.isClientSide)m.bucket(p,hand);return ItemInteractionResult.sidedSuccess(l.isClientSide);}if(m.canFeed(stack)){if(!l.isClientSide)m.feed(stack,p.getAbilities().instabuild);return ItemInteractionResult.sidedSuccess(l.isClientSide);}}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){if(l.getBlockEntity(pos) instanceof PrimitiveVesselEntity m){if(!l.isClientSide)p.openMenu(m,b->b.writeBoolean(rain));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 protected void onRemove(BlockState s,Level l,BlockPos pos,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(pos) instanceof PrimitiveVesselEntity m)for(int i=0;i<2;i++)Containers.dropItemStack(l,pos.getX(),pos.getY(),pos.getZ(),m.inventory.getStackInSlot(i));super.onRemove(s,l,pos,next,moving);}
}

