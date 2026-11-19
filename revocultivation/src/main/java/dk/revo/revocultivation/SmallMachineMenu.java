package dk.revo.revocultivation;

import dk.revo.revolib.MachineMenuAccess;
import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public class SmallMachineMenu extends AbstractContainerMenu {
 public final boolean silk;private final ContainerData data;private final SmallMachineEntity source;
 public SmallMachineMenu(int id,Inventory inv,boolean silk){this(id,inv,silk,new ItemStackHandler(3),new SimpleContainerData(6),null);}
 public SmallMachineMenu(int id,Inventory inv,SmallMachineEntity m){this(id,inv,m.silk(),m.storage,new ContainerData(){public int get(int i){return m.value(i);}public void set(int i,int v){}public int getCount(){return 6;}},m);}
 private SmallMachineMenu(int id,Inventory inv,boolean silk,IItemHandler storage,ContainerData data,SmallMachineEntity source){
  super(SmallMachines.MENU.get(),id);this.silk=silk;this.data=data;this.source=source;addDataSlots(data);
  addSlot(new SlotItemHandler(storage,0,44,32));addSlot(new SlotItemHandler(storage,1,17,32){public boolean isActive(){return silk;}});addSlot(new SlotItemHandler(storage,2,116,32){public boolean mayPlace(ItemStack s){return false;}});
  PlayerInventoryLayout.slots(inv,184).forEach(this::addSlot);
 }
 public int value(int i){return data.get(i);}
 public int duration(){return silk?400:160;}
 public boolean stillValid(Player p){return source==null||MachineMenuAccess.isUsable(p,source);}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(i<3){if(!moveItemStackTo(s,3,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,silk?2:1,false)){if(i<30){if(!moveItemStackTo(s,30,39,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,3,30,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}

