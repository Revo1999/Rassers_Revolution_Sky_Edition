package dk.revo.revoextraction;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Server-synchronized diagnostics only. Item storage is accessed exclusively through the output hatch. */
public final class DrillMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData diagnostics;

    public DrillMenu(int id, Inventory playerInventory) {
        this(id, new SimpleContainerData((DrillDiagnostics.SIZE+3)), ContainerLevelAccess.NULL);
    }

    public DrillMenu(int id, Inventory playerInventory, DrillMachine machine) {
        this(id, new ContainerData() {
            @Override public int get(int index) {
                return switch(index){case 8->machine.tier().ordinal();case 9->machine.waterAmount();case 10->machine.dryTicks();default->DrillDiagnostics.capture(machine).get(index);};
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return (DrillDiagnostics.SIZE+3); }
        }, ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()));
    }

    private DrillMenu(int id, ContainerData data, ContainerLevelAccess access) {
        super(RevoExtraction.DRILL_MENU.get(), id);
        this.access=access;
        this.diagnostics=data;
        checkContainerDataCount(data,(DrillDiagnostics.SIZE+3));
        addDataSlots(data);

    }

    public boolean waterCooled(){return diagnostics.get(8)==DrillTier.STEEL.ordinal();}
    public int water(){return diagnostics.get(9);}
    public int dryTicks(){return diagnostics.get(10);}
    public int progress() { return Math.clamp(diagnostics.get(0),0,1200); }
    public int rpm() { return diagnostics.get(1); }
    public int state() { return diagnostics.get(2); }
    public DrillDiagnostics snapshot(){return DrillDiagnostics.read(diagnostics::get);}

    @Override public boolean stillValid(Player player) {
        return access.evaluate((level,pos)->level.getBlockState(pos).getBlock() instanceof DrillController && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64,true);
    }

    @Override public ItemStack quickMoveStack(Player player,int index) {
        return ItemStack.EMPTY; // The controller is a diagnostics panel, with no inventory slots.
    }
}
