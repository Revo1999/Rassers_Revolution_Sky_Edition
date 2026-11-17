package dk.revo.revoindustry.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep vanilla egg identities/icons, but remove their native spawning operations too. */
@Mixin(SpawnEggItem.class)
public abstract class InertSpawnEggMixin {
    @Inject(method="useOn",at=@At("HEAD"),cancellable=true)
    private void revoNoBlockSpawn(UseOnContext context,CallbackInfoReturnable<InteractionResult> ci){ci.setReturnValue(InteractionResult.FAIL);}
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void revoNoFluidSpawn(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci){ci.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));}
    @Inject(method="spawnOffspringFromSpawnEgg",at=@At("HEAD"),cancellable=true)
    private void revoNoBabySpawn(Player player,Mob parent,EntityType<? extends Mob> type,ServerLevel level,Vec3 pos,ItemStack stack,CallbackInfoReturnable<Optional<Mob>> ci){ci.setReturnValue(Optional.empty());}
}
