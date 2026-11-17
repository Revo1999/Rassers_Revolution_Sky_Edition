package dk.revo.revoindustry.nexus;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Ordinary placed structure blocks, with vanilla placement and rotation. */
public final class NexusBlock extends Block {
    public NexusBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return simpleCodec(NexusBlock::new);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(BlockStateProperties.HORIZONTAL_FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(BlockStateProperties.HORIZONTAL_FACING,rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));}
}
