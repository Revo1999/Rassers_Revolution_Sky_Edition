package dk.revo.revoextraction;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Three occupied blocks, one inventory owner. +FACING is the water's downstream direction. */
public final class SluiceBlock extends KineticBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);
    public SluiceBlock() {
        super(Properties.of().strength(3.5f).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PART, 0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, PART); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
        var direction = c.getHorizontalDirection();
        for (int i = 1; i <= 2; i++) {
            var p = c.getClickedPos().relative(direction, i);
            if (!c.getLevel().isInWorldBounds(p) || !c.getLevel().getWorldBorder().isWithinBounds(p) || !c.getLevel().getBlockState(p).canBeReplaced(c)) return null;
        }
        return defaultBlockState().setValue(FACING, direction);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) for (int i = 1; i <= 2; i++) level.setBlock(pos.relative(state.getValue(FACING), i), state.setValue(PART, i), 3);
    }
    @Override public Direction.Axis getRotationAxis(BlockState s) { return s.getValue(FACING).getClockWise().getAxis(); }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState s, Direction side) {
        return s.getValue(PART) == 0 && side.getAxis() == getRotationAxis(s);
    }
    // Rotating one segment would desynchronise the occupied footprint. Re-place the whole machine.
    @Override public BlockState rotate(BlockState s, Rotation r) { return s; }
    @Override public BlockState mirror(BlockState s, Mirror m) { return s; }
    @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return s.getValue(PART) == 0 ? new SluiceEntity(p, s) : null; }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> type) {
        return type == SluiceContent.MACHINE.get() ? (world, pos, state, be) -> ((SluiceEntity) be).tick() : null;
    }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        int top = 14 - s.getValue(PART) * 2;
        var bed = box(0, 0, 0, 16, top - 2, 16);
        if (getRotationAxis(s) == Direction.Axis.X) return Shapes.or(bed, box(0, 0, 0, 2, top, 16), box(14, 0, 0, 16, top, 16));
        return Shapes.or(bed, box(0, 0, 0, 16, top, 2), box(0, 0, 14, 16, top, 16));
    }
    public static SluiceEntity controller(Level level, BlockPos pos, BlockState state) {
        if (!state.is(SluiceContent.SLUICE.get())) return null;
        var p = pos.relative(state.getValue(FACING).getOpposite(), state.getValue(PART));
        return level.hasChunkAt(p) && level.getBlockEntity(p) instanceof SluiceEntity e ? e : null;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var machine = controller(level, pos, s);
        if (!level.isClientSide && machine != null) {
            var kind = s.getValue(PART) == 2 ? SluiceMenu.Kind.OUTPUT : SluiceMenu.Kind.CONTROL;
            player.openMenu(new SimpleMenuProvider((id, inv, viewer) -> new SluiceMenu(id, inv, kind, machine, null), Component.translatable("gui.revoextraction.sluice_" + (kind == SluiceMenu.Kind.OUTPUT ? "output" : "control"))));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState old, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (old.getBlock() != next.getBlock() && !level.isClientSide) {
            int part = old.getValue(PART);
            var root = pos.relative(old.getValue(FACING).getOpposite(), part);
            if (part == 0) {
                if (level.getBlockEntity(pos) instanceof SluiceEntity e) e.dropContents();
                for (int i = 1; i <= 2; i++) {
                    var p = pos.relative(old.getValue(FACING), i); var s = level.getBlockState(p);
                    if (s.is(this) && s.getValue(PART) == i && s.getValue(FACING) == old.getValue(FACING)) level.removeBlock(p, false);
                }
            } else if (level.getBlockState(root).is(this) && level.getBlockState(root).getValue(PART) == 0) level.destroyBlock(root, true);
        }
        super.onRemove(old, level, pos, next, moving);
    }
}
