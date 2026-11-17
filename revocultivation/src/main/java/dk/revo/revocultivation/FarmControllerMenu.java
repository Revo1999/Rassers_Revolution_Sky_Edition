package dk.revo.revocultivation;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public class FarmControllerMenu extends AbstractContainerMenu {
    private final ContainerData data;private final ContainerLevelAccess access;
    public final boolean harvester;
    public FarmControllerMenu(int id,Inventory inv){this(id,inv,false);}
    public FarmControllerMenu(int id,Inventory inv,boolean harvester){this(id,inv,new SimpleContainerData(FarmMachine.DATA_SIZE),ContainerLevelAccess.NULL,new ItemStackHandler(1),harvester);}
    public FarmControllerMenu(int id,Inventory inv,FarmMachine machine){this(id,inv,new ContainerData(){public int get(int i){return machine.data(i);}public void set(int i,int v){}public int getCount(){return FarmMachine.DATA_SIZE;}},ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),machine.programSpawner,machine.kind().isHarvester());}
    private FarmControllerMenu(int id,Inventory inv,ContainerData data,ContainerLevelAccess access,IItemHandler program,boolean harvester){
        super(RevoCultivation.CONTROLLER_MENU.get(),id);this.data=data;this.access=access;this.harvester=harvester;addDataSlots(data);
        if(harvester){
            addSlot(new SlotItemHandler(program,0,116,166){@Override public boolean mayPlace(ItemStack stack){return MobSpawnerPrograms.accepts(kind(),stack);}});
            for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,35+col*18,212+row*18));
            for(int col=0;col<9;col++)addSlot(new Slot(inv,col,35+col*18,270));
        }
    }
    public int value(int i){return data.get(i);}
    public MachineKind kind(){return MachineKind.values()[Math.clamp(value(0),0,MachineKind.values().length-1)];}
    @Override public boolean stillValid(Player p){return access.evaluate((l,pos)->l.getBlockState(pos).getBlock() instanceof FarmController&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64,true);}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(!harvester||index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,slots.size(),true))return ItemStack.EMPTY;}
        else if(MobSpawnerPrograms.accepts(kind(),stack)){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        else if(index<28){if(!moveItemStackTo(stack,28,37,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,1,28,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return before;
    }
}
