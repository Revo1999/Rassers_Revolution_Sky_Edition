package dk.revo.revoextraction;

import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;

public final class SluiceMenu extends AbstractContainerMenu {
    public enum Kind { CONTROL, OUTPUT, FEEDER }
    public final Kind kind;
    public final ContainerData data;
    private final SluiceEntity machine;
    private final SluiceFeeder.Entity feeder;
    public SluiceMenu(int id, Inventory inv, Kind kind, SluiceEntity machine, SluiceFeeder.Entity feeder) {
        super(switch(kind) { case CONTROL -> SluiceContent.CONTROL_MENU.get(); case OUTPUT -> SluiceContent.OUTPUT_MENU.get(); case FEEDER -> SluiceContent.FEED_MENU.get(); }, id);
        this.kind = kind; this.machine = machine; this.feeder = feeder;
        data = machine == null && feeder == null ? new SimpleContainerData(7) : new ContainerData() {
            public int get(int i) {
                var f = feeder != null ? feeder : machine == null ? null : machine.feeder();
                return switch(i) {
                    case 0 -> machine == null ? 0 : Math.round(machine.getSpeed());
                    case 1 -> machine == null ? 0 : (int) (machine.work * 100 / SluiceEntity.WORK_PER_ITEM);
                    case 2 -> f == null ? 0 : f.water.getFluidAmount();
                    case 3 -> f == null ? 0 : f.input.getStackInSlot(0).getCount();
                    case 4 -> machine == null ? 0 : machine.status();
                    case 5 -> machine != null && machine.flowing ? 1 : 0;
                    default -> f != null && f.machine() != null ? 1 : 0;
                };
            }
            public void set(int i, int v) {}
            public int getCount() { return 7; }
        };
        addDataSlots(data);
        if (kind == Kind.OUTPUT) {
            var handler = machine == null ? new ItemStackHandler(4) : machine.outputs;
            for (int i = 0; i < 4; i++) addSlot(new SlotItemHandler(handler, i, 53 + i*18, 30) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        } else if (kind == Kind.FEEDER) {
            var handler = feeder == null ? new ItemStackHandler(1) : feeder.input;
            addSlot(new SlotItemHandler(handler, 0, 80, 47));
        }
        if (kind != Kind.CONTROL) PlayerInventoryLayout.slots(inv, 166).forEach(this::addSlot);
    }
    @Override public boolean stillValid(Player player) {
        if (kind == Kind.FEEDER) return feeder == null || !feeder.isRemoved() && player.distanceToSqr(feeder.getBlockPos().getCenter()) <= 64;
        return machine == null || !machine.isRemoved() && player.distanceToSqr((kind == Kind.OUTPUT ? machine.at(2) : machine.getBlockPos()).getCenter()) <= 64 && (kind == Kind.CONTROL || machine.assembled());
    }
    @Override public ItemStack quickMoveStack(Player p, int index) {
        if (kind == Kind.CONTROL || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        int count = kind == Kind.OUTPUT ? 4 : 1;
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem(); var old = stack.copy();
        if (index < count) {
            if (!moveItemStackTo(stack, count, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (kind == Kind.OUTPUT || !moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(p, stack);
        return old;
    }
}
