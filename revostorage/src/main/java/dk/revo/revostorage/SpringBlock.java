package dk.revo.revostorage;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpringBlock extends KineticBlock implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 13.3, 16);

    public SpringBlock() {
        super(Properties.of().strength(4).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(StorageBlock.FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> states) {
        states.add(StorageBlock.FACING);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(StorageBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(StorageBlock.FACING, rotation.rotate(state.getValue(StorageBlock.FACING)));
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(StorageBlock.FACING)));
    }

    @Override public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(StorageBlock.FACING).getAxis();
    }

    @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction side) {
        return side == state.getValue(StorageBlock.FACING);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpringEntity(pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == RevoStorage.SPRING_ENTITY.get() ? (l, p, s, entity) -> ((SpringEntity) entity).tick() : null;
    }

}
