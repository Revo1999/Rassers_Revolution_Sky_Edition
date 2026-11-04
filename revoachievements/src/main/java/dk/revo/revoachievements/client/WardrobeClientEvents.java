package dk.revo.revoachievements.client;

import dk.revo.revoachievements.RevoAchievements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=RevoAchievements.MOD_ID,value=Dist.CLIENT)
public final class WardrobeClientEvents {
    public static final KeyMapping OPEN=new KeyMapping("key.revoachievements.wardrobe",GLFW.GLFW_KEY_K,"key.categories.revoachievements");
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){while(OPEN.consumeClick())if(Minecraft.getInstance().player!=null&&Minecraft.getInstance().screen==null)WardrobeClient.request(0,"");}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){WardrobeClient.clear();}
    @SubscribeEvent public static void inventory(ScreenEvent.Init.Post e){
        if(e.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
                &&(screen instanceof InventoryScreen||screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen)){
            // Match the small inventory-side controls used by quest/cosmetic mods.
            e.addListener(Button.builder(Component.literal("R"),b->WardrobeClient.request(0,"")).bounds(screen.getGuiLeft()-22,screen.getGuiTop()+26,20,20)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Revo Wardrobe (K)"))).build());
        }
    }
    @EventBusSubscriber(modid=RevoAchievements.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(OPEN);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins())if(e.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer renderer)renderer.addLayer(new CosmeticHatLayer(renderer));}
    }
}
