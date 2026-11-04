package dk.revo.revolib;

import java.util.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Vanilla/MI player inventory: 16px items in 18px slots, three rows then a 4px hotbar gap. */
public final class PlayerInventoryLayout {
    public static final int WIDTH=176,X=8;
    private PlayerInventoryLayout(){}
    public static int mainY(int contentHeight){return contentHeight-82;}
    public static int hotbarY(int contentHeight){return contentHeight-24;}
    public static List<Slot> slots(Inventory inventory,int contentHeight){
        List<Slot> slots=new ArrayList<>();
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slots.add(new Slot(inventory,col+row*9+9,X+col*18,mainY(contentHeight)+row*18));
        for(int col=0;col<9;col++)slots.add(new Slot(inventory,col,X+col*18,hotbarY(contentHeight)));
        return slots;
    }
}
