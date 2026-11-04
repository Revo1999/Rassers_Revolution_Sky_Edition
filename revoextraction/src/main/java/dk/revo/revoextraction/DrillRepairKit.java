package dk.revo.revoextraction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

/** Repair the controller, core, tank or frame without replacing the machine or its output. */
public final class DrillRepairKit extends Item {
    public DrillRepairKit(){super(new Item.Properties().stacksTo(16));}
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){return useOn(context);}
    @Override public InteractionResult useOn(UseOnContext context){
        var level=context.getLevel();var player=context.getPlayer();
        if(player==null)return InteractionResult.PASS;
        var machine=DrillMachine.controllerFor(level,context.getClickedPos());
        if(machine==null||machine.tier()!=DrillTier.STEEL)return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        String failure=machine.repairFailure();
        if(failure!=null){player.displayClientMessage(Component.translatable("gui.revoextraction.repair."+failure),true);return InteractionResult.CONSUME;}
        machine.repair();if(!player.getAbilities().instabuild)context.getItemInHand().shrink(1);
        level.playSound(null,context.getClickedPos(),net.minecraft.sounds.SoundEvents.ANVIL_USE,net.minecraft.sounds.SoundSource.BLOCKS,.65f,1.3f);
        player.displayClientMessage(Component.translatable("gui.revoextraction.repair.success"),true);
        return InteractionResult.SUCCESS;
    }
}
