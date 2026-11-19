package dk.revo.revoindustry.hatchery;

import dk.revo.revolib.MachineMenuAccess;

import dk.revo.revolib.PlayerInventoryLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.*;

public final class HatcheryMenu extends AbstractContainerMenu {
    public final boolean hatch;
    public final boolean inputHatch;
    public final BlockPos pos;
    private final BlockEntity source;
    private final ContainerData data;
    public HatcheryMenu(int id,Inventory inv,BlockPos pos,boolean hatch){this(id,inv,pos,hatch,null);}
    public HatcheryMenu(int id,Inventory inv,HatcheryController c){this(id,inv,c.getBlockPos(),false,c);}
    public HatcheryMenu(int id,Inventory inv,HatcheryPort p){this(id,inv,p.getBlockPos(),true,p);}
    private HatcheryMenu(int id,Inventory inv,BlockPos pos,boolean hatch,BlockEntity source){
        super(Hatchery.MENU.get(),id);this.pos=pos;this.hatch=hatch;this.source=source;
        inputHatch=hatch&&(source instanceof HatcheryPort p?p.input():inv.player.level().getBlockState(pos).is(Hatchery.INPUT.get()));
        data=source==null?new SimpleContainerData(6):new ContainerData(){
            public int get(int index){var c=source instanceof HatcheryController controller?controller:((HatcheryPort)source).controller();if(c==null)return 0;return switch(index){case 0->c.stage();case 1->c.progress();case 2->c.status();case 3->c.energy()&65535;case 4->c.energy()>>>16;default->c.stageTicks();};}
            public void set(int index,int value){}public int getCount(){return 6;}
        };addDataSlots(data);
        if(hatch){
            IItemHandler handler=source instanceof HatcheryPort p?p.inventory:new ItemStackHandler(9);
            for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new SlotItemHandler(handler,x+y*3,62+x*18,27+y*18){
                public boolean mayPlace(ItemStack stack){return inputHatch&&stack.is(Hatchery.EGG.get())&&(source==null||source instanceof HatcheryPort p&&p.controller()!=null)&&super.mayPlace(stack);}
                public boolean mayPickup(Player player){return (source==null||source instanceof HatcheryPort p&&p.controller()!=null)&&super.mayPickup(player);}
            });
            PlayerInventoryLayout.slots(inv,184).forEach(this::addSlot);
        }
    }
    public int value(int index){return data.get(index);}
    public int energy(){return (data.get(3)&65535)|(data.get(4)<<16);}
    public boolean stillValid(Player player){
        if(source==null)return true;
        return MachineMenuAccess.isUsable(player,source)&&(!(source instanceof HatcheryPort p)||p.controller()!=null);
    }
    public ItemStack quickMoveStack(Player player,int index){
        if(!hatch||index<0||index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();
        if(index<9){if(!moveItemStackTo(stack,9,45,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,9,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
