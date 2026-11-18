package dk.revo.revoindustry.client;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.nexus.Nexus;
import dk.revo.revoindustry.nexus.NexusBuilderMenu;
import dk.revo.revoindustry.nexus.NexusConstruction;
import dk.revo.revolib.client.VanillaMachineGui;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = RevoIndustry.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NexusBuilderScreen extends AbstractContainerScreen<NexusBuilderMenu> {
    private static final int LABEL = 0xff404040;
    private static final int MATERIAL_X = 8;
    private static final int MATERIAL_Y = 17;
    private static final int COLUMNS = 8;
    private static final int CELL_WIDTH = 28;
    private static final int CELL_HEIGHT = 21;
    private Button build;
    private Button preview;

    public NexusBuilderScreen(NexusBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = NexusBuilderMenu.WIDTH;
        imageHeight = NexusBuilderMenu.HEIGHT;
        inventoryLabelX = NexusBuilderMenu.INVENTORY_X;
        inventoryLabelY = NexusBuilderMenu.INVENTORY_Y - 11;
    }

    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent event) {
        event.register(Nexus.BUILDER_MENU.get(), NexusBuilderScreen::new);
    }

    @Override
    protected void init() {
        super.init();
        // Leave room for the recipe viewer's bottom search bar at large GUI scales.
        topPos = Math.max(9, topPos - 4);
        build = addRenderableWidget(Button.builder(Component.literal("Build"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1))
            .bounds(leftPos + imageWidth - 68, topPos + 114, 60, 17).build());
        preview = addRenderableWidget(Button.builder(Component.literal("Box"),
                button -> NexusBuilderOverlay.toggle(menu.pos))
            .bounds(leftPos + imageWidth - 38, topPos + 4, 30, 14)
            .tooltip(Tooltip.create(Component.literal("Toggle the portal bounding box in the world"))).build());
    }

    private int cell(int x, int y) {
        int localX = x - leftPos - MATERIAL_X;
        int localY = y - topPos - MATERIAL_Y;
        if (localX < 0 || localX >= COLUMNS * CELL_WIDTH || localY < 0 || localY >= 4 * CELL_HEIGHT) return -1;
        int index = localY / CELL_HEIGHT * COLUMNS + localX / CELL_WIDTH;
        return index < NexusConstruction.requirements().size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int index = cell((int) x, (int) y);
        if (index >= 0 && (button == 0 || button == 1)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 200 + index);
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        VanillaMachineGui.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (var slot : menu.slots) VanillaMachineGui.slot(graphics, leftPos + slot.x, topPos + slot.y);

        var requirements = NexusConstruction.requirements();
        for (int i = 0; i < requirements.size(); i++) {
            var requirement = requirements.get(i);
            int x = leftPos + MATERIAL_X + i % COLUMNS * CELL_WIDTH + 6;
            int y = topPos + MATERIAL_Y + i / COLUMNS * CELL_HEIGHT + 1;
            int supplied = menu.stored(i) + menu.used(i);
            VanillaMachineGui.slot(graphics, x, y);
            graphics.renderItem(new ItemStack(requirement.item()), x, y);

            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 250);
            if (supplied == 0) graphics.fill(x, y, x + 16, y + 16, 0xaa8b8b8b);
            graphics.pose().translate(x + 8, y + 15, 1);
            graphics.pose().scale(.65f, .65f, 1);
            String count = supplied + "/" + requirement.amount();
            graphics.drawString(font, count, -font.width(count) / 2, 0,
                supplied == 0 ? 0xffdddddd : 0xffffffff, true);
            graphics.pose().popPose();
        }

        int total = menu.required();
        int supplied = menu.supplied();
        int width = imageWidth - 16;
        int x = leftPos + 8;
        int y = topPos + 103;
        graphics.fill(x, y, x + width, y + 10, 0xff373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 9, 0xff777777);
        graphics.fill(x + 1, y + 1, x + 1 + (width - 2) * supplied / total, y + 9, 0xff568542);
        String progress = supplied + " / " + total + "  (" + (100 * supplied / total) + "%)";
        graphics.drawCenteredString(font, progress, leftPos + imageWidth / 2, y + 1, 0xffffffff);
    }

    private String status(int state) {
        return switch (state) {
            case NexusConstruction.OBSTRUCTED -> "Obstructed - cannot build";
            case NexusConstruction.UNLOADED -> "Build area not loaded";
            case NexusConstruction.STRUCTURE -> "Missing MI hatches";
            case NexusConstruction.PROGRAM -> "Needs Construction Program";
            case NexusConstruction.MATERIALS -> "Supply blueprint materials";
            case NexusConstruction.POWER -> "Charge before building";
            case NexusConstruction.BUILDING -> "Building " + (100 * menu.data.get(0) / NexusConstruction.cells().size()) + "%";
            case NexusConstruction.COMPLETE -> "Construction complete";
            default -> "Ready to build";
        };
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, LABEL, false);
        int state = menu.data.get(2);
        graphics.drawString(font, status(state), 8, 116,
            state == NexusConstruction.OBSTRUCTED ? 0xffa12f39 : LABEL, false);
        String power = String.format(Locale.ROOT, "Power %.1f / %.1f M EU",
            menu.energy() / 1_000_000d, NexusConstruction.ENERGY_COST / 1_000_000d);
        graphics.drawString(font, power, 8, 126, LABEL, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partial) {
        build.visible = menu.supplied() == menu.required() && menu.data.get(1) == 0;
        build.active = menu.data.get(2) == NexusConstruction.READY;
        preview.setMessage(Component.literal(NexusBuilderOverlay.shown(menu.pos) ? "Box*" : "Box"));
        super.render(graphics, x, y, partial);
        renderTooltip(graphics, x, y);
        int i = cell(x, y);
        if (i >= 0) {
            var requirement = NexusConstruction.requirements().get(i);
            graphics.renderComponentTooltip(font, List.of(new ItemStack(requirement.item()).getHoverName(),
                Component.literal("Stored " + menu.stored(i) + " / " + requirement.amount() + " | Placed " + menu.used(i)),
                Component.literal("Deposit carried items or click with an empty cursor to withdraw up to 64")), x, y);
        }
    }
}
