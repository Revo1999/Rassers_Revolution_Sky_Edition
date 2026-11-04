package dk.revo.revocultivation;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public class FarmControllerMenu extends AbstractContainerMenu {
    private final ContainerData data;private final ContainerLevelAccess access;
    public FarmControllerMenu(int id,Inventory inv){this(id,new SimpleContainerData(FarmMachine.DATA_SIZE),ContainerLevelAccess.NULL);}
    public FarmControllerMenu(int id,Inventory inv,FarmMachine machine){this(id,new ContainerData(){public int get(int i){return machine.data(i);}public void set(int i,int v){}public int getCount(){return FarmMachine.DATA_SIZE;}},ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()));}
    private FarmControllerMenu(int id,ContainerData data,ContainerLevelAccess access){super(RevoCultivation.CONTROLLER_MENU.get(),id);this.data=data;this.access=access;addDataSlots(data);}
    public int value(int i){return data.get(i);}
    public MachineKind kind(){return MachineKind.values()[Math.clamp(value(0),0,MachineKind.values().length-1)];}
    @Override public boolean stillValid(Player p){return access.evaluate((l,pos)->l.getBlockState(pos).getBlock() instanceof FarmController&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64,true);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
