package dk.revo.revoindustry.nexus;

import dk.revo.revolib.MachineMenuAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Ghost cells are material counters; vanilla player slots remain ordinary inventory slots. */
public final class NexusBuilderMenu extends AbstractContainerMenu {
 public static final int WIDTH = 240, HEIGHT = 224;
 public static final int INVENTORY_X = (WIDTH - 176) / 2 + 8, INVENTORY_Y = 146;
 public final NexusMachine machine;
 public final BlockPos pos;
 public final ContainerData data;
 public NexusBuilderMenu(int id,Inventory inv,BlockPos pos){this(id,inv,inv.player.level().getBlockEntity(pos) instanceof NexusMachine m?m:null,pos);}
 public NexusBuilderMenu(int id,Inventory inv,NexusMachine machine){this(id,inv,machine,machine.getBlockPos());}
 private NexusBuilderMenu(int id,Inventory inv,NexusMachine machine,BlockPos pos){
  super(Nexus.BUILDER_MENU.get(),id);this.machine=machine;this.pos=pos;int n=NexusConstruction.requirements().size();int size=5+2*n;
  data=machine==null||inv.player.level().isClientSide?new SimpleContainerData(size):new ContainerData(){public int get(int i){var c=machine.construction;return switch(i){case 0->c.cursor();case 1->c.started()?1:0;case 2->c.status();case 3->(int)(c.energy()&65535);case 4->(int)(c.energy()>>>16)&65535;default->i<5+n?c.stored(i-5):c.used(i-5-n);};}public void set(int i,int v){}public int getCount(){return size;}};addDataSlots(data);
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,9+x+9*y,INVENTORY_X+18*x,INVENTORY_Y+18*y));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,INVENTORY_X+18*x,INVENTORY_Y+58));
 }
 public int stored(int i){return data.get(5+i);}public int used(int i){return data.get(5+NexusConstruction.requirements().size()+i);}
 public long energy(){return (data.get(3)&65535L)|((data.get(4)&65535L)<<16);}
 public int supplied(){int n=0;for(int i=0;i<NexusConstruction.requirements().size();i++)n+=stored(i)+used(i);return n;}
 public int required(){return NexusConstruction.requirements().stream().mapToInt(NexusConstruction.Requirement::amount).sum();}
 @Override public boolean clickMenuButton(Player player,int button){
  if(!stillValid(player)||machine==null||machine.getLevel().isClientSide)return false;
  if(button==1)return machine.construction.start();int i=button-200;if(i<0||i>=NexusConstruction.requirements().size())return false;
  var carried=getCarried();if(carried.isEmpty())setCarried(machine.construction.withdraw(i,64));else if(carried.is(NexusConstruction.requirements().get(i).item())){int n=machine.construction.deposit(carried,false);carried.shrink(n);setCarried(carried);}
  machine.construction.refresh(false);broadcastChanges();return true;
 }
 @Override public ItemStack quickMoveStack(Player player,int index){if(machine==null||!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);var stack=slot.getItem();int n=machine.construction.deposit(stack,false);if(n==0)return ItemStack.EMPTY;var moved=stack.copyWithCount(n);stack.shrink(n);slot.setByPlayer(stack);machine.construction.refresh(false);return moved;}
 @Override public boolean stillValid(Player player){return machine!=null&&machine.construction!=null&&MachineMenuAccess.isUsable(player,machine);}
}
