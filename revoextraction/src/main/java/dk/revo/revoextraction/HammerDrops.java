package dk.revo.revoextraction;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
@EventBusSubscriber(modid=RevoExtraction.MOD_ID)
public class HammerDrops{
 @SubscribeEvent public static void drops(BlockDropsEvent e){if(!(e.getTool().getItem() instanceof HammerItem))return;var product=HammerItem.result(e.getState());if(product.isEmpty())return;e.getDrops().clear();var p=e.getPos();e.getDrops().add(new ItemEntity(e.getLevel(),p.getX()+.5,p.getY()+.5,p.getZ()+.5,product));e.setDroppedExperience(0);}
}

