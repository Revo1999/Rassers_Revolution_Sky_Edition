package dk.revo.revoindustry.furnace;
import dk.revo.revoindustry.RevoIndustry;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;
@EventBusSubscriber(modid=RevoIndustry.MOD_ID)
public final class FurnaceEvents {
 @SubscribeEvent public static void noFactoryAugment(PlayerInteractEvent.RightClickBlock event){if(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString().equals("ironfurnaces:augment_factory"))event.setCanceled(true);}
 @EventBusSubscriber(modid=RevoIndustry.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Setup {
  @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e){e.enqueueWork(()->{for(var tier:FurnaceTier.values()){var machine=(FurnaceController)Furnaces.CONTROLLERS.get(tier).get().getBlockEntityInstance();ReiMachineRecipes.registerMultiblockShape(Furnaces.id(tier.id+"_multiblock_furnace"),machine.getActiveShape());}});}
 }
}
