package dk.revo.revoindustry.saltern;

import dk.revo.revoindustry.hatchery.HatcheryBlock;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class SalternBlock extends Block implements EntityBlock {
    public final Saltern.Part part;
    public SalternBlock(Saltern.Part part){super(Properties.of().strength(5,6).sound(SoundType.METAL).noOcclusion());this.part=part;registerDefaultState(stateDefinition.any().setValue(HatcheryBlock.FACING,Direction.NORTH));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HatcheryBlock.FACING);}
    public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(HatcheryBlock.FACING,c.getHorizontalDirection().getOpposite());}
    public BlockState rotate(BlockState s,Rotation r){return s.setValue(HatcheryBlock.FACING,r.rotate(s.getValue(HatcheryBlock.FACING)));}
    public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(HatcheryBlock.FACING)));}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return part==Saltern.Part.CONTROLLER?new SalternController(p,s):part==Saltern.Part.CORE?null:new SalternPort(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){if(level.isClientSide)return null;if(type==Saltern.ENTITY.get())return (l,p,s,be)->((SalternController)be).tick();return type==Saltern.PORT.get()?(l,p,s,be)->((SalternPort)be).tick():null;}
    protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){if(level.getBlockEntity(pos) instanceof SalternPort port&&part!=Saltern.Part.ENERGY){var fluids=port.fluids(null);if(fluids!=null&&net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(p,hand,fluids))return ItemInteractionResult.sidedSuccess(level.isClientSide);}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
    protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){var be=level.getBlockEntity(pos);var c=be instanceof SalternController controller?controller:be instanceof SalternPort port?port.controller():null;if(c==null)return InteractionResult.PASS;if(!level.isClientSide)player.openMenu(c,b->b.writeBlockPos(c.getBlockPos()));return InteractionResult.sidedSuccess(level.isClientSide);}
    protected void onRemove(BlockState old,Level level,BlockPos pos,BlockState next,boolean moving){if(!old.is(next.getBlock())&&level.getBlockEntity(pos) instanceof SalternController c)c.reset();super.onRemove(old,level,pos,next,moving);}
}
