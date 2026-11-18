package dk.revo.revoextraction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
public class ElectricDrill extends BaseEntityBlock {
 public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public ElectricDrill(){super(Properties.of().strength(6).sound(SoundType.METAL));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(DrillPart.FORMED,false));}
 protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){b.add(FACING,DrillPart.FORMED);}
 public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(net.neoforged.neoforge.fluids.FluidUtil.getFluidHandler(stack).isPresent()){if(l.isClientSide)return ItemInteractionResult.SUCCESS;return net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(player,hand,l,pos,hit.getDirection())?ItemInteractionResult.SUCCESS:ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new ElectricDrill());}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ElectricDrillEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,RevoExtraction.ELECTRIC_ENTITY.get(),ElectricDrillEntity::tick);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof ElectricDrillEntity e)player.openMenu(new SimpleMenuProvider((id,inv,pl)->new ElectricDrillMenu(id,inv,e),Component.literal(e.isDigital()?"Digital Geological Drill":"Electric Geological Drill")));return InteractionResult.sidedSuccess(l.isClientSide);}
 protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof ElectricDrillEntity e){if(!e.isDigital())ElectricDrillStructure.setFormation(e,false);for(int i=0;i<e.items.getSlots();i++)Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),e.items.getStackInSlot(i));}super.onRemove(old,l,p,next,moving);}
}
