package dk.revo.revoextraction;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public class ElectricDrillMenu extends AbstractContainerMenu {
 public final ElectricDrillEntity entity;public final ContainerData data;
 public ElectricDrillMenu(int id,Inventory inv){this(id,inv,null);}
 public ElectricDrillMenu(int id,Inventory inv,ElectricDrillEntity e){super(RevoExtraction.ELECTRIC_MENU.get(),id);entity=e;data=e==null?new SimpleContainerData(5):new ContainerData(){public int get(int i){return switch(i){case 0->e.energy;case 1->e.work;case 2->e.duration();case 3->e.program;case 4->e.cost();default->0;};}public void set(int i,int v){}public int getCount(){return 5;}};addDataSlots(data);var h=e==null?new ItemStackHandler(11):e.items;addSlot(new SlotItemHandler(h,0,8,57));addSlot(new SlotItemHandler(h,1,26,57));for(int i=0;i<9;i++)addSlot(new SlotItemHandler(h,2+i,8+i*18,88){public boolean mayPlace(ItemStack s){return false;}});for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,128+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,186));}
 public boolean clickMenuButton(Player p,int id){if(entity==null||id<0||id>6)return false;entity.program=id;entity.work=0;entity.setChanged();return true;}
 public boolean stillValid(Player p){return entity==null||!entity.isRemoved()&&p.distanceToSqr(entity.getBlockPos().getCenter())<=64;}
 public ItemStack quickMoveStack(Player p,int index){var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var old=s.copy();if(index<11){if(!moveItemStackTo(s,11,47,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,2,false))return ItemStack.EMPTY;slot.setChanged();return old;}
}
