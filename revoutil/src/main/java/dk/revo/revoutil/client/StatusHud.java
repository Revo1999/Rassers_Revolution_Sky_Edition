package dk.revo.revoutil.client;

import dk.revo.revoutil.PlaytestStatus;
import dk.revo.revoutil.RevoUtil;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = RevoUtil.MOD_ID, value = Dist.CLIENT)
public final class StatusHud {
    private static final String TITLE = "Revo " + RevoUtil.PACK_VERSION;
    private static final int MARGIN = 7;
    private static final int PADDING = 5;
    private static final int LINE_HEIGHT = 12;
    private static final boolean HAS_MINIMAP = ModList.get().isLoaded("ftbchunks");
    private static String tickTime;

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(RevoUtil.MOD_ID, "status"), StatusHud::render);
        NeoForge.EVENT_BUS.addListener(StatusHud::logout);
    }

    public static void receive(PlaytestStatus.Update update) {
        tickTime = update.enabled() ? String.format(Locale.ROOT, "Server %.1f ms/tick", update.millisecondsPerTick()) : null;
    }

    private static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        tickTime = null;
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        var minecraft = Minecraft.getInstance();
        if (tickTime == null || minecraft.player == null || minecraft.options.hideGui
                || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        var font = minecraft.font;
        String fps = minecraft.getFps() + " FPS";
        int textWidth = Math.max(font.width(TITLE), Math.max(font.width(fps), font.width(tickTime)));
        int width = textWidth + 2 * PADDING;
        int right = graphics.guiWidth() - MARGIN;
        if (HAS_MINIMAP) {
            // Leave room for the pack's default upper-right minimap.
            right -= Math.max(80, graphics.guiWidth() / 6);
        }
        int left = Math.max(MARGIN, right - width);
        int top = MARGIN;
        var toast = minecraft.getToasts().getToast(Toast.class, Toast.NO_TOKEN);
        if (toast != null && left + width > graphics.guiWidth() - toast.width()) {
            // Recipe and tutorial toasts can stack on a fresh island.
            top += Math.max(2 * Toast.SLOT_HEIGHT, toast.height());
        }
        graphics.fill(left, top, left + width, top + 3 * LINE_HEIGHT + 2 * PADDING - 3, 0xA6101010);
        graphics.drawString(font, TITLE, left + PADDING, top + PADDING, 0xFFFFFF);
        graphics.drawString(font, fps, left + PADDING, top + PADDING + LINE_HEIGHT, 0xE0E0E0);
        graphics.drawString(font, tickTime, left + PADDING, top + PADDING + 2 * LINE_HEIGHT, 0xE0E0E0);
    }
}
