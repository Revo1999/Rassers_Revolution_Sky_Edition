package dk.revo.revocultivation;
import java.util.function.BooleanSupplier;
import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.items.*;
public class FarmHatchMenu extends AbstractContainerMenu {
    public final MachineKind kind;
    private final ContainerLevelAccess access;private final BooleanSupplier connected;private final ContainerData data;
    public FarmHatchMenu(int id,Inventory inv,MachineKind kind){this(id,inv,kind,new ItemStackHandler(2),new ItemStackHandler(9),ContainerLevelAccess.NULL,()->true,new SimpleContainerData(1));}
    public FarmHatchMenu(int id,Inventory inv,FarmHatchEntity hatch,FarmMachine machine){
        this(id,inv,machine.kind(),machine.inputs,machine.outputs,ContainerLevelAccess.create(hatch.getLevel(),hatch.getBlockPos()),()->!hatch.isRemoved()&&!machine.isRemoved()&&hatch.machine()==machine,new ContainerData(){public int get(int i){return machine.water.getFluidAmount();}public void set(int i,int v){}public int getCount(){return 1;}});
    }
    private FarmHatchMenu(int id,Inventory inv,MachineKind kind,IItemHandler inputs,IItemHandler outputs,ContainerLevelAccess access,BooleanSupplier connected,ContainerData data){
        super(RevoCultivation.HATCH_MENU.get(),id);this.kind=kind;this.access=access;this.connected=connected;this.data=data;addDataSlots(data);
        for(int i=0;i<kind.inputSlots();i++){final int index=i;addSlot(new SlotItemHandler(inputs,i,kind.inputSlots()==1?80:62+i*36,25){@Override public boolean mayPlace(ItemStack stack){return index==0?kind.acceptsStarter(stack):stack.is(Items.WATER_BUCKET);}});}
        int y=outputY();for(int row=0;row<3;row++)for(int col=0;col<3;col++)addSlot(new SlotItemHandler(outputs,row*3+col,62+18*col,y+18*row){@Override public boolean mayPlace(ItemStack stack){return false;}});
        PlayerInventoryLayout.slots(inv,kind.hatchHeight()).forEach(this::addSlot);
    }
    public int outputY(){return kind.isHarvester()&&kind!=MachineKind.STEEL_HARVESTER?27:62;}
    public int water(){return data.get(0);}
    @Override public boolean stillValid(Player p){return connected.getAsBoolean()&&stillValid(access,p,kind.block(MachineKind.Part.HATCH));}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();int machine=kind.inputSlots()+9;
        if(index<machine){if(!moveItemStackTo(stack,machine,slots.size(),true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,kind.inputSlots(),false)){
            if(index<machine+27){if(!moveItemStackTo(stack,machine+27,slots.size(),false))return ItemStack.EMPTY;}
            else if(!moveItemStackTo(stack,machine,machine+27,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
