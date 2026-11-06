package dk.revo.revoindustry.client;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.vacuum.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID,value=Dist.CLIENT)
public final class VacuumTooltip {
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){if(e.getItemStack().getItem() instanceof net.minecraft.world.item.BlockItem item&&item.getBlock() instanceof VacuumBlock v){
  e.getToolTip().add(Component.translatable("tooltip.revoindustry.vacuum.stats",Vacuums.size(v.tier),Vacuums.slots(v.tier),20f/Vacuums.interval(v.tier)).withStyle(ChatFormatting.GRAY));
  e.getToolTip().add(Component.translatable("tooltip.revoindustry.vacuum.outlet").withStyle(ChatFormatting.GRAY));
  e.getToolTip().add(Component.translatable("tooltip.revoindustry.vacuum.wrench").withStyle(ChatFormatting.DARK_GRAY));
 }}
}
