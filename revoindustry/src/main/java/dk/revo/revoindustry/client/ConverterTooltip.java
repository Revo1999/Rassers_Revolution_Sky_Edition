package dk.revo.revoindustry.client;
import dk.revo.revoindustry.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class ConverterTooltip{
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){if(e.getItemStack().getItem() instanceof net.minecraft.world.item.BlockItem bi&&bi.getBlock() instanceof PowerConverterBlock b){int eu=new int[]{32,128,512}[b.tier];e.getToolTip().add(Component.translatable("tooltip.revoindustry.converter"));e.getToolTip().add(Component.literal(eu+" EU/t = "+eu*dk.revo.revolib.energy.EnergyInterop.fePerEu()+" FE/t; no storage"));e.getToolTip().add(Component.literal("Wrench rotates ports. FE / MI EU only."));}}
}
