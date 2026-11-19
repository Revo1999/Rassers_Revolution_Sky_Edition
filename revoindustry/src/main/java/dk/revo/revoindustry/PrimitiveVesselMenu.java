package dk.revo.revoindustry;

import dk.revo.revolib.MachineMenuAccess;
import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public class PrimitiveVesselMenu extends AbstractContainerMenu{
 public final boolean rain;private final PrimitiveVesselEntity source;private final ContainerData data;
 public PrimitiveVesselMenu(int id,Inventory inv,boolean rain){this(id,inv,rain,new ItemStackHandler(2),null,new SimpleContainerData(4));}
 public PrimitiveVesselMenu(int id,Inventory inv,PrimitiveVesselEntity m){this(id,inv,m.rain(),m.inventory,m,new ContainerData(){public int get(int i){return m.value(i);}public void set(int i,int v){}public int getCount(){return 4;}});}
 private PrimitiveVesselMenu(int id,Inventory inv,boolean rain,IItemHandler items,PrimitiveVesselEntity source,ContainerData data){super(PrimitiveMachines.MENU.get(),id);this.rain=rain;this.source=source;this.data=data;addDataSlots(data);addSlot(new SlotItemHandler(items,0,44,32));addSlot(new SlotItemHandler(items,1,116,32){public boolean mayPlace(ItemStack s){return false;}public boolean isActive(){return rain;}});PlayerInventoryLayout.slots(inv,184).forEach(this::addSlot);}
 public int value(int i){return data.get(i);}
 public boolean stillValid(Player p){return source==null||MachineMenuAccess.isUsable(p,source);}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(i<2){if(!moveItemStackTo(s,2,38,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,1,false)){if(i<29){if(!moveItemStackTo(s,29,38,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,2,29,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}

