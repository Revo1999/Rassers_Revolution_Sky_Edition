package dk.revo.revostorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid="revostorage",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class StorageClient {@SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoStorage.MENU.get(),StorageScreen::new);}}
