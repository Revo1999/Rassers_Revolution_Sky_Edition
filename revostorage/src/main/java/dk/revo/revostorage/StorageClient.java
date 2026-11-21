package dk.revo.revostorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
@EventBusSubscriber(modid="revostorage",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class StorageClient {
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(RevoStorage.MENU.get(),StorageScreen::new);}
 @SubscribeEvent public static void springModels(ModelEvent.RegisterAdditional e){e.register(SpringRenderer.COIL);}
 @SubscribeEvent public static void springRenderer(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(RevoStorage.SPRING_ENTITY.get(),SpringRenderer::new);}
}
