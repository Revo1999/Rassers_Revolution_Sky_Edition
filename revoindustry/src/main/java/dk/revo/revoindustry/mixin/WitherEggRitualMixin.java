package dk.revo.revoindustry.mixin;

import dk.revo.revoindustry.hatchery.WitherEggRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullBlock.class)
public abstract class WitherEggRitualMixin {
    @Inject(method="checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V",at=@At("HEAD"),cancellable=true)
    private static void revoEggInsteadOfBoss(Level level,BlockPos pos,SkullBlockEntity skull,CallbackInfo ci){
        if(level instanceof ServerLevel server){WitherEggRitual.convert(server,pos);ci.cancel();}
    }
}
