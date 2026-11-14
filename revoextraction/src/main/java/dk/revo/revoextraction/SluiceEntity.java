package dk.revo.revoextraction;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.*;

public final class SluiceEntity extends KineticBlockEntity {
    public static final int WATER_PER_TICK = 5, WORK_PER_ITEM = 3200;
    public float work, clientAngle, previousClientAngle, clientTransport;
    public long cycle;
    public boolean flowing, processing;
    public final ItemStackHandler outputs = new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    public final IItemHandler outputPort = new net.neoforged.neoforge.items.wrapper.RangedWrapper(outputs, 0, 4) {
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
    };
    public SluiceEntity(BlockPos pos, BlockState state) { super(SluiceContent.MACHINE.get(), pos, state); }
    public Direction downstream() { return getBlockState().getValue(SluiceBlock.FACING); }
    public BlockPos at(int segment) { return worldPosition.relative(downstream(), segment); }
    public boolean assembled() {
        if (level == null) return false;
        for (int i = 0; i < 3; i++) {
            if (!level.hasChunkAt(at(i))) return false;
            var s = level.getBlockState(at(i));
            if (!s.is(SluiceContent.SLUICE.get()) || s.getValue(SluiceBlock.PART) != i || s.getValue(SluiceBlock.FACING) != downstream()) return false;
        }
        return true;
    }
    public SluiceFeeder.Entity feeder() {
        if (level == null || !level.hasChunkAt(worldPosition.above())) return null;
        return level.getBlockEntity(worldPosition.above()) instanceof SluiceFeeder.Entity f && f.getBlockState().getValue(SluiceFeeder.FACING) == downstream() ? f : null;
    }
    /** Stable saved sequence. Blocked buffers, relogs and unloading cannot reroll gems. */
    public ItemStack nextOutput() {
        int n = (int) Math.floorMod(cycle, 128);
        return new ItemStack(n == 127 ? Items.DIAMOND : n % 64 == 63 ? Items.EMERALD : n % 16 == 15 ? Items.LAPIS_LAZULI : n % 4 == 3 ? Items.QUARTZ : Items.FLINT);
    }
    public int status() {
        if (!assembled()) return 0;
        var feed = feeder();
        if (feed == null) return 1;
        if (Math.abs(getSpeed()) < 16) return 2;
        if (feed.water.getFluidAmount() < WATER_PER_TICK && !flowing) return 3;
        if (feed.input.getStackInSlot(0).isEmpty()) return 4;
        if (!ItemHandlerHelper.insertItemStacked(outputs, nextOutput(), true).isEmpty()) return 5;
        return 6;
    }
    @Override public float calculateStressApplied() { lastStressApplied = 8; return lastStressApplied; }
    @Override public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            previousClientAngle = clientAngle;
            clientAngle += getSpeed() * .3f;
            if (processing) clientTransport = (clientTransport + Math.min(Math.abs(getSpeed()),128)/WORK_PER_ITEM) % 1;
            // Keep values bounded without interpolating backwards at the loop seam.
            if (Math.abs(clientAngle) > 36000) { clientAngle %= 360; previousClientAngle = clientAngle - getSpeed() * .3f; }
            return;
        }
        tickProcessing();
    }
    /** Server work step, separate from Create's kinetic-network tick for deterministic regression tests. */
    void tickProcessing() {
        if (level == null || level.isClientSide) return;
        boolean oldFlow = flowing, oldProcessing = processing;
        flowing = processing = false;
        var feed = feeder();
        if (assembled()) {
            if (level.getGameTime() % 10 == 0) exportOutput();
            if (feed != null && feed.water.getFluidAmount() >= WATER_PER_TICK) {
                // A running pump can keep water flowing even when the separate shaker drive is off.
                feed.water.drain(WATER_PER_TICK, FluidAction.EXECUTE);
                flowing = true;
                if (Math.abs(getSpeed()) >= 16 && feed.input.getStackInSlot(0).is(Items.GRAVEL) && ItemHandlerHelper.insertItemStacked(outputs, nextOutput(), true).isEmpty()) {
                    processing = true;
                    work += Math.min(Math.abs(getSpeed()), 128);
                    if (work >= WORK_PER_ITEM) {
                        ItemHandlerHelper.insertItemStacked(outputs, nextOutput(), false);
                        feed.input.extractItem(0, 1, false);
                        work -= WORK_PER_ITEM;
                        cycle++;
                        exportOutput();
                    }
                    setChanged();
                }
            }
        }
        if (oldFlow != flowing || oldProcessing != processing || level.getGameTime() % 10 == 0) {
            setChanged(); sendData();
            if (feed != null) feed.sync();
        }
    }
    public void exportOutput() {
        if (level == null || !assembled()) return;
        for (Direction side : new Direction[]{downstream(), downstream().getClockWise(), downstream().getCounterClockWise(), Direction.DOWN}) {
            var target = at(2).relative(side);
            if (!level.hasChunkAt(target)) continue;
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, side.getOpposite());
            if (handler == null) continue;
            for (int i = 0; i < 4; i++) {
                var original = outputs.getStackInSlot(i);
                if (original.isEmpty()) continue;
                var remainder = ItemHandlerHelper.insertItemStacked(handler, original.copy(), false);
                if (remainder.getCount() != original.getCount()) outputs.setStackInSlot(i, remainder);
            }
        }
    }
    public void dropContents() {
        for (int i = 0; i < outputs.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5, outputs.getStackInSlot(i));
            outputs.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("SluiceWork", work); tag.putLong("SluiceCycle", cycle);
        tag.putBoolean("SluiceFlow", flowing); tag.putBoolean("SluiceProcessing", processing);
        tag.put("SluiceOutput", outputs.serializeNBT(registries));
    }
    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        float saved = tag.getFloat("SluiceWork"); work = Float.isFinite(saved) ? Math.clamp(saved, 0, WORK_PER_ITEM-1) : 0;
        cycle = Math.max(0, tag.getLong("SluiceCycle")); flowing = tag.getBoolean("SluiceFlow"); processing = tag.getBoolean("SluiceProcessing");
        outputs.deserializeNBT(registries, tag.getCompound("SluiceOutput"));
    }
}
