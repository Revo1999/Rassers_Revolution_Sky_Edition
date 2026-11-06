package dk.revo.revoindustry.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Multiblock controllers replace Factory Augments, including augments left in existing saves. */
@Mixin(targets="ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase",remap=false)
public abstract class DisableFactoryAugmentMixin {
 @org.spongepowered.asm.mixin.Shadow public int[] currentAugment;
 @Inject(method="isFactory",at=@At("HEAD"),cancellable=true,remap=false)
 private void revoDisableFactory(CallbackInfoReturnable<Boolean> result){result.setReturnValue(false);}
 @Inject(method="isFurnace",at=@At("HEAD"),cancellable=true,remap=false)
 private void revoIgnoreInstalledFactory(CallbackInfoReturnable<Boolean> result){if(currentAugment[2]==1)result.setReturnValue(true);}
}

