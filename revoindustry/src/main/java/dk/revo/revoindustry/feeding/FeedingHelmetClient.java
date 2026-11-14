package dk.revo.revoindustry.feeding;

import dk.revo.revoindustry.RevoIndustry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.*;

@EventBusSubscriber(modid=RevoIndustry.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class FeedingHelmetClient {
    private static final ModelLayerLocation[] LAYERS = java.util.stream.IntStream.range(0, 3)
        .mapToObj(i -> new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID, FeedingHelmets.IDS[i]), "main"))
        .toArray(ModelLayerLocation[]::new);
    private static final HumanoidModel<?>[] MODELS = new HumanoidModel<?>[3];
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (int i = 0; i < 3; i++) { final int tier = i; event.registerLayerDefinition(LAYERS[i], () -> FeedingHelmetGeometry.create(tier)); }
    }
    @SubscribeEvent public static void reload(EntityRenderersEvent.AddLayers event) { java.util.Arrays.fill(MODELS, null); }
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event) {
        var items = new FeedingHelmetItem[]{FeedingHelmets.BASIC.get(), FeedingHelmets.ADVANCED.get(), FeedingHelmets.ELITE.get()};
        for (int i = 0; i < 3; i++) {
            final int tier = i;
            event.registerItem(new IClientItemExtensions() {
                @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                    if (MODELS[tier] == null) MODELS[tier] = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYERS[tier]));
                    return MODELS[tier];
                }
            }, items[i]);
        }
    }
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent event) { event.register(FeedingHelmets.MENU.get(), Screen::new); }
    public static final class Screen extends AbstractContainerScreen<FeedingHelmetMenu> {
        public Screen(FeedingHelmetMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title); imageWidth = 176; imageHeight = menu.playerY + 82;
            inventoryLabelY = menu.playerY - 12;
        }
        @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
            int x = leftPos, y = topPos;
            g.fill(x, y, x + imageWidth, y + imageHeight, 0xff353a30);
            g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xffe1dfd4);
            g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xffbfc0b1);
            for (var slot : menu.slots) {
                int sx = x + slot.x, sy = y + slot.y;
                g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xff535b49);
                g.fill(sx, sy, sx + 17, sy + 17, 0xffe3e4d7);
                g.fill(sx, sy, sx + 16, sy + 16, 0xff858d79);
            }
        }
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            super.render(g, mouseX, mouseY, partial); renderTooltip(g, mouseX, mouseY);
        }
    }
}
