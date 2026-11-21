package dk.revo.revostorage;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
import dk.revo.revolib.MachineFeedback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public final class SpringEntity extends KineticBlockEntity {
    public static final int CAPACITY = 4096;
    public static final int MIN_RPM = 16;
    private static final int CRANK_CHARGE_PER_SECOND = 512;
    private static final int SHAFT_CHARGE_PER_SECOND = 32;
    private static final int TICKS_PER_SECOND = 20;
    private static final Direction[] NEIGHBORS = Direction.values();
    private int charge;
    private int windingRemainder;
    private boolean visualDirty;
    private float compression, previousCompression;
    private float releasePulse;

    SpringEntity(BlockPos pos, BlockState state) {
        super(RevoStorage.SPRING_ENTITY.get(), pos, state);
    }

    public int charge() { return charge; }

    public boolean isShaftWinding() { return Math.abs(getSpeed()) >= MIN_RPM && charge < CAPACITY; }

    boolean spend() {
        if (charge == 0) return false;
        changeCharge(charge - 1);
        return true;
    }

    private void changeCharge(int next) {
        charge = Math.clamp(next, 0, CAPACITY);
        visualDirty = true;
        setChanged();
    }

    @Override public float calculateStressApplied() {
        return lastStressApplied = 2;
    }

    @Override public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            animate();
            return;
        }
        if (charge < CAPACITY && level.getGameTime() % TICKS_PER_SECOND == 0) {
            for (Direction side : NEIGHBORS) {
                var pos = worldPosition.relative(side);
                if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof StorageEntity terminal) || !terminal.terminal()) continue;
                int transferred = terminal.moveLegacyCharge(CAPACITY - charge);
                if (transferred > 0) changeCharge(charge + transferred);
                if (charge == CAPACITY) break;
            }
        }
        if (isShaftWinding()) {
            var input = worldPosition.relative(getBlockState().getValue(StorageBlock.FACING));
            boolean handCrank = level.hasChunkAt(input) && level.getBlockEntity(input) instanceof HandCrankBlockEntity crank
                    && crank.getGeneratedSpeed() != 0;
            int winding = windingRemainder + (handCrank ? CRANK_CHARGE_PER_SECOND : SHAFT_CHARGE_PER_SECOND);
            changeCharge(charge + winding / TICKS_PER_SECOND);
            windingRemainder = charge < CAPACITY ? winding % TICKS_PER_SECOND : 0;
            MachineFeedback.work(level, worldPosition, MachineFeedback.Voice.STORAGE);
        }
        if (visualDirty && level.getGameTime() % 4 == 0) {
            visualDirty = false;
            sendData();
        }
    }

    private void animate() {
        previousCompression = compression;
        float tension = charge / (float) CAPACITY;
        compression += (Math.max(0, tension - releasePulse) - compression) * .38f;
        releasePulse *= .64f;
    }

    public float compression(float partialTick) { return Mth.lerp(partialTick, previousCompression, compression); }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Charge", charge);
        if (!clientPacket) tag.putInt("WindingRemainder", windingRemainder);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        int next = Math.clamp(tag.getInt("Charge"), 0, CAPACITY);
        if (clientPacket && next < charge) releasePulse = Math.min(.16f, Math.max(.09f, releasePulse + .04f));
        if (!clientPacket) windingRemainder = Math.clamp(tag.getInt("WindingRemainder"), 0, TICKS_PER_SECOND - 1);
        charge = next;
    }
}
