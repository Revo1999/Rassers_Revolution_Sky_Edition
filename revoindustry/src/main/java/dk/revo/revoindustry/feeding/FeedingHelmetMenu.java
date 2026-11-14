package dk.revo.revoindustry.feeding;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Locks the bound helmet against movement, hotbar swaps and recursive storage. */
public final class FeedingHelmetMenu extends AbstractContainerMenu {
    public final ItemStack helmet;
    public final int tier, boundSlot, width, foodSlots, playerY;
    private final Inventory inventory;
    private final SimpleContainer food;
    public FeedingHelmetMenu(int id, Inventory inventory, int boundSlot, int tier) {
        super(FeedingHelmets.MENU.get(), id);
        if (boundSlot < 0 || boundSlot >= inventory.getContainerSize()) throw new IllegalArgumentException("Invalid helmet slot");
        this.inventory = inventory; this.boundSlot = boundSlot; this.tier = tier;
        this.helmet = inventory.getItem(boundSlot); width = FeedingHelmets.width(tier); foodSlots = FeedingHelmets.capacity(tier); playerY = 42 + width * 18;
        food = new SimpleContainer(foodSlots);
        var initial = FeedingHelmets.contents(helmet, tier);
        for (int i = 0; i < foodSlots; i++) food.setItem(i, initial.get(i));
        food.addListener(c -> {
            if (!inventory.player.level().isClientSide && stillValid(inventory.player)) {
                var items = NonNullList.withSize(foodSlots, ItemStack.EMPTY);
                for (int i = 0; i < foodSlots; i++) items.set(i, food.getItem(i).copy());
                FeedingHelmets.save(helmet, items); inventory.setChanged();
            }
        });
        int startX = (176 - width * 18) / 2;
        for (int y = 0; y < width; y++) for (int x = 0; x < width; x++) addSlot(new Slot(food, x + y * width, startX + x * 18, 24 + y * 18) {
            @Override public boolean mayPlace(ItemStack stack) { return FeedingHelmets.accepts(stack, inventory.player); }
        });
        for (int y = 0; y < 3; y++) for (int x = 0; x < 9; x++) addPlayerSlot(inventory, 9 + y * 9 + x, 8 + x * 18, playerY + y * 18);
        for (int x = 0; x < 9; x++) addPlayerSlot(inventory, x, 8 + x * 18, playerY + 58);
    }
    private void addPlayerSlot(Inventory inv, int inventoryIndex, int x, int y) {
        addSlot(new Slot(inv, inventoryIndex, x, y) {
            @Override public boolean mayPickup(Player player) { return inventoryIndex != boundSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return inventoryIndex != boundSlot; }
        });
    }
    @Override public boolean stillValid(Player player) {
        return player.isAlive() && inventory.getItem(boundSlot) == helmet && helmet.getItem() instanceof FeedingHelmetItem item && item.tier == tier;
    }
    @Override public void clicked(int index, int button, ClickType type, Player player) {
        if (type == ClickType.SWAP && button == boundSlot) return;
        super.clicked(index, button, type, player);
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot.container == inventory && slot.getContainerSlot() == boundSlot) && super.canTakeItemForPickAll(stack, slot);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack = slot.getItem(); var original = stack.copy();
        if (index < foodSlots) {
            if (!moveItemStackTo(stack, foodSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!FeedingHelmets.accepts(stack, player) || !moveItemStackTo(stack, 0, foodSlots, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
