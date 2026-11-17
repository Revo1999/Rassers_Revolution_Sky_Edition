package dk.revo.revoindustry.mixin;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Spawners are retained harvester programs in this pack, never world mob generators. */
@Mixin(BaseSpawner.class)
public abstract class DisableSpawnerTickMixin {
    @Inject(method="serverTick",at=@At("HEAD"),cancellable=true)
    private void revoDormantSpawner(ServerLevel level,BlockPos pos,CallbackInfo ci){ci.cancel();}
}
