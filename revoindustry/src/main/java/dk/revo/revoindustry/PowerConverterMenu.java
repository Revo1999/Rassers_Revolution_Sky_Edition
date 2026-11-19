package dk.revo.revoindustry;

import dk.revo.revolib.MachineMenuAccess;
import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class PowerConverterMenu extends AbstractContainerMenu{
 private final ContainerData data;private final PowerConverterEntity entity;
 public PowerConverterMenu(int id,Inventory inv){this(id,inv,null);}
 public PowerConverterMenu(int id,Inventory inv,PowerConverterEntity be){super(PrimitiveMachines.CONVERTER_MENU.get(),id);entity=be;data=be==null?new SimpleContainerData(4):new ContainerData(){public int get(int i){return be.value(i);}public void set(int i,int v){}public int getCount(){return 4;}};addDataSlots(data);PlayerInventoryLayout.slots(inv,184).forEach(this::addSlot);}
 public int ratio(){return Math.max(1,data.get(0));}public int rateEu(){return data.get(1);}public boolean feToEu(){return data.get(2)==0;}public int tier(){return data.get(3);}
 public boolean stillValid(Player p){return entity==null||MachineMenuAccess.isUsable(p,entity);}
 public boolean clickMenuButton(Player p,int id){if(id!=0||entity==null||!stillValid(p))return false;entity.power.setMode(!entity.power.feToEu());broadcastChanges();return true;}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=36)return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var old=s.copy();if(i<27){if(!moveItemStackTo(s,27,36,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,27,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return old;}
}
