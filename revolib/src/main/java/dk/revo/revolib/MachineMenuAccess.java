package dk.revo.revolib;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineMenuAccess {
    private MachineMenuAccess() {}

    public static boolean isUsable(Player player, BlockEntity entity) {
        return isUsable(player, entity, entity.getBlockPos());
    }

    public static boolean isUsable(Player player, BlockEntity entity, BlockPos accessPos) {
        var level = entity.getLevel();
        var pos = entity.getBlockPos();
        return level != null && player.level() == level && !entity.isRemoved()
                && player.distanceToSqr(accessPos.getCenter()) <= 64 && level.hasChunkAt(pos)
                && level.getBlockEntity(pos) == entity;
    }
}
