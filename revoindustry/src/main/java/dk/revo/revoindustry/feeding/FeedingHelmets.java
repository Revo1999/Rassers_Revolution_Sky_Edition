package dk.revo.revoindustry.feeding;

import dk.revo.revoindustry.RevoIndustry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;

/** Food travels with the helmet in an immutable, vanilla-synchronized component. */
public final class FeedingHelmets {
    public static final String[] IDS = {"feeding_helmet", "advanced_feeding_helmet", "elite_feeding_helmet"};
    public static final DeferredItem<FeedingHelmetItem> BASIC = RevoIndustry.ITEMS.register(IDS[0], () -> new FeedingHelmetItem(0));
    public static final DeferredItem<FeedingHelmetItem> ADVANCED = RevoIndustry.ITEMS.register(IDS[1], () -> new FeedingHelmetItem(1));
    public static final DeferredItem<FeedingHelmetItem> ELITE = RevoIndustry.ITEMS.register(IDS[2], () -> new FeedingHelmetItem(2));
    public static final DeferredHolder<MenuType<?>, MenuType<FeedingHelmetMenu>> MENU = RevoIndustry.MENUS.register("feeding_helmet",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new FeedingHelmetMenu(id, inv, buf.readVarInt(), buf.readVarInt())));
    private FeedingHelmets() {}
    public static void bootstrap() {}
    public static int width(int tier) { return switch (tier) { case 0 -> 1; case 1 -> 2; case 2 -> 5; default -> throw new IllegalArgumentException("Unknown helmet tier"); }; }
    public static int capacity(int tier) { int width = width(tier); return width * width; }
    public static boolean accepts(ItemStack stack, Player player) {
        var food = stack.getFoodProperties(player);
        return !stack.isEmpty() && !(stack.getItem() instanceof FeedingHelmetItem) && food != null && food.nutrition() > 0;
    }
    public static NonNullList<ItemStack> contents(ItemStack helmet, int tier) {
        var items = NonNullList.withSize(capacity(tier), ItemStack.EMPTY);
        helmet.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }
    public static void save(ItemStack helmet, NonNullList<ItemStack> items) { helmet.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items)); }
    /** Nutrition is in half-drumstick points: 1.5 drumsticks is three points. */
    public static int chooseFood(NonNullList<ItemStack> items, Player player) {
        int missing = 20 - player.getFoodData().getFoodLevel(), best = -1, nutrition = 0;
        float saturation = -1;
        for (int i = 0; i < items.size(); i++) {
            var food = items.get(i).getFoodProperties(player);
            if (food == null || food.nutrition() <= 0 || food.nutrition() > missing) continue;
            if (food.nutrition() > nutrition || food.nutrition() == nutrition && food.saturation() > saturation) {
                best = i; nutrition = food.nutrition(); saturation = food.saturation();
            }
        }
        return best;
    }
    public static boolean feed(ServerPlayer player, ItemStack helmet) {
        if (!(helmet.getItem() instanceof FeedingHelmetItem item) || player.getItemBySlot(EquipmentSlot.HEAD) != helmet
            || player.isSpectator() || player.getAbilities().instabuild || player.isUsingItem()
            || player.containerMenu instanceof FeedingHelmetMenu menu && menu.helmet == helmet) return false;
        var items = contents(helmet, item.tier);
        int slot = chooseFood(items, player);
        if (slot < 0) return false;
        var meal = items.get(slot).copyWithCount(1);
        items.get(slot).shrink(1); save(helmet, items);
        // Normal food consumption retains nutrition, saturation, effects and bowls.
        var remainder = meal.finishUsingItem(player.level(), player);
        if (!remainder.isEmpty()) player.getInventory().placeItemBackInInventory(remainder);
        player.getInventory().setChanged();
        return true;
    }
    public static void open(ServerPlayer player, int inventorySlot) {
        var helmet = player.getInventory().getItem(inventorySlot);
        if (!(helmet.getItem() instanceof FeedingHelmetItem item)) return;
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new FeedingHelmetMenu(id, inv, inventorySlot, item.tier), helmet.getHoverName()),
            buf -> { buf.writeVarInt(inventorySlot); buf.writeVarInt(item.tier); });
    }
}
