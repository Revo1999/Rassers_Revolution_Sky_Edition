package dk.revo.revoextraction;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
import dk.revo.revolib.PlayerInventoryLayout;

/** A dedicated extraction-only hatch window. No duplicated item storage or save migration. */
public final class DrillHatchMenu extends AbstractContainerMenu {
    public static final int OUTPUT_SLOTS=9;
    public static final int CONTENT_HEIGHT=184,HEIGHT=CONTENT_HEIGHT,OUTPUT_X=62,OUTPUT_Y=27;
    private final ContainerLevelAccess access;
    private final BooleanSupplier connected;

    public DrillHatchMenu(int id,Inventory inventory) {
        this(id,inventory,new ItemStackHandler(OUTPUT_SLOTS),ContainerLevelAccess.NULL,()->true);
    }
    public DrillHatchMenu(int id,Inventory inventory,HatchEntity hatch,DrillMachine machine) {
        this(id,inventory,machine.inventory,ContainerLevelAccess.create(hatch.getLevel(),hatch.getBlockPos()),
            ()->!hatch.isRemoved()&&!machine.isRemoved()&&hatch.machine()==machine);
    }
    private DrillHatchMenu(int id,Inventory playerInventory,IItemHandler output,ContainerLevelAccess access,BooleanSupplier connected) {
        super(RevoExtraction.HATCH_MENU.get(),id);
        this.access=access;this.connected=connected;
        for(int row=0;row<3;row++)for(int col=0;col<3;col++) {
            addSlot(new SlotItemHandler(output,row*3+col,OUTPUT_X+col*18,OUTPUT_Y+row*18) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        PlayerInventoryLayout.slots(playerInventory,CONTENT_HEIGHT).forEach(this::addSlot);
    }
    @Override public boolean stillValid(Player player) {
        return connected.getAsBoolean()&&access.evaluate((level,pos)->player.level()==level && level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof DrillHatch && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64,true);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=OUTPUT_SLOTS)return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),original=stack.copy();
        if(!moveItemStackTo(stack,OUTPUT_SLOTS,slots.size(),true))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);
        return original;
    }
}
