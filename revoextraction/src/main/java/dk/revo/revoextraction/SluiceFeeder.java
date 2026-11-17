package dk.revo.revoextraction;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

/** An ordinary top item port plus a rear fluid capability: Create pumps use the real tank. */
public final class SluiceFeeder extends BaseEntityBlock {
    public static final MapCodec<SluiceFeeder> CODEC = simpleCodec(p -> new SluiceFeeder());
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public SluiceFeeder() {
        super(Properties.of().strength(3).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var below = context.getLevel().getBlockState(context.getClickedPos().below());
        var facing = below.is(SluiceContent.SLUICE.get()) && below.getValue(SluiceBlock.PART) == 0 ? below.getValue(SluiceBlock.FACING) : context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing);
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Entity(pos, state); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState s, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            return FluidUtil.interactWithFluidHandler(player, hand, level, pos, s.getValue(FACING).getOpposite()) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof Entity feeder)
            player.openMenu(new SimpleMenuProvider((id, inv, viewer) -> new SluiceMenu(id, inv, SluiceMenu.Kind.FEEDER, feeder.machine(), feeder), Component.translatable("gui.revoextraction.sluice_feeder")));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState old, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (old.getBlock() != next.getBlock() && !level.isClientSide && level.getBlockEntity(pos) instanceof Entity f) {
            Containers.dropItemStack(level, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, f.input.getStackInSlot(0));
            f.input.setStackInSlot(0, ItemStack.EMPTY);
        }
        super.onRemove(old, level, pos, next, moving);
    }
    public static final class Entity extends BlockEntity {
        public static final int CAPACITY = 4000;
        public final ItemStackHandler input = new ItemStackHandler(1) {
            @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.is(Items.GRAVEL); }
            @Override protected void onContentsChanged(int slot) { setChanged(); sync(); }
        };
        public final FluidTank water = new FluidTank(CAPACITY, f -> f.is(Fluids.WATER)) {
            @Override protected void onContentsChanged() { setChanged(); }
        };
        public Entity(BlockPos pos, BlockState state) { super(SluiceContent.FEEDER_ENTITY.get(), pos, state); }
        public SluiceEntity machine() {
            return level != null && level.getBlockEntity(worldPosition.below()) instanceof SluiceEntity e && e.downstream() == getBlockState().getValue(FACING) ? e : null;
        }
        public void sync() { if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
        @Override protected void saveAdditional(CompoundTag t, HolderLookup.Provider p) {
            super.saveAdditional(t, p); t.put("Input", input.serializeNBT(p)); t.put("Water", water.writeToNBT(p, new CompoundTag()));
        }
        @Override protected void loadAdditional(CompoundTag t, HolderLookup.Provider p) {
            super.loadAdditional(t, p); input.deserializeNBT(p, t.getCompound("Input")); water.readFromNBT(p, t.getCompound("Water"));
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider p) { return saveWithoutMetadata(p); }
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    }
}
