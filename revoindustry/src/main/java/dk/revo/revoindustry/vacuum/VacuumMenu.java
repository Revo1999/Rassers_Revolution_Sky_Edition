package dk.revo.revoindustry.vacuum;
import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public final class VacuumMenu extends AbstractContainerMenu {
 public final int tier;
 private final ContainerLevelAccess access;
 public VacuumMenu(int id,Inventory inv,int tier){this(id,inv,tier,new ItemStackHandler(Vacuums.slots(tier)),ContainerLevelAccess.NULL);}
 public VacuumMenu(int id,Inventory inv,VacuumEntity v){this(id,inv,v.tier(),v.inventory,ContainerLevelAccess.create(v.getLevel(),v.getBlockPos()));}
 public static int x(int tier,int slot){return tier==0?80:tier==1?62+slot*18:62+(slot%3)*18;}
 public static int y(int tier,int slot){return tier==2?28+(slot/3)*18:46;}
 private VacuumMenu(int id,Inventory inv,int tier,IItemHandler items,ContainerLevelAccess access){super(Vacuums.MENU.get(),id);this.tier=tier;this.access=access;for(int i=0;i<Vacuums.slots(tier);i++)addSlot(new SlotItemHandler(items,i,x(tier,i),y(tier,i)));PlayerInventoryLayout.slots(inv,216).forEach(this::addSlot);}
 public boolean stillValid(Player p){return stillValid(access,p,Vacuums.BLOCKS.get(tier).get());}
 public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();int n=Vacuums.slots(tier);if(!moveItemStackTo(stack,index<n?n:0,index<n?slots.size():n,index<n))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return original;}
}
