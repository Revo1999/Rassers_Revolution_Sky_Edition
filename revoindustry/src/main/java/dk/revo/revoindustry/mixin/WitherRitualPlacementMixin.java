package dk.revo.revoindustry.mixin;

import dk.revo.revoindustry.hatchery.WitherEggRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Also covers placers using setBlock directly, or placing the last soul block after the skulls. */
@Mixin(Level.class)
public abstract class WitherRitualPlacementMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",at=@At("RETURN"))
    private void revoCheckRitual(BlockPos pos,BlockState state,int flags,int recursion,CallbackInfoReturnable<Boolean> ci){
        if(ci.getReturnValueZ()&&(Object)this instanceof ServerLevel server){
            if(state.is(net.minecraft.world.level.block.Blocks.WITHER_SKELETON_SKULL)||state.is(net.minecraft.world.level.block.Blocks.WITHER_SKELETON_WALL_SKULL)||state.is(net.minecraft.tags.BlockTags.WITHER_SUMMON_BASE_BLOCKS))WitherEggRitual.convert(server,pos);
        }
    }
}
