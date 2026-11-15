package dk.revo.revoindustry.saltern;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class SalternMenu extends AbstractContainerMenu {
    public final BlockPos pos;private final SalternController source;private final ContainerData data;
    public SalternMenu(int id,Inventory inv,BlockPos pos){this(id,inv,pos,null);}
    public SalternMenu(int id,Inventory inv,SalternController c){this(id,inv,c.getBlockPos(),c);}
    private SalternMenu(int id,Inventory inv,BlockPos pos,SalternController c){super(Saltern.MENU.get(),id);this.pos=pos;source=c;data=c==null?new SimpleContainerData(5):new ContainerData(){public int get(int i){return switch(i){case 0->c.status();case 1->c.progress();case 2->c.water.getFluidAmount();case 3->c.brine.getFluidAmount();default->c.energy();};}public void set(int i,int v){}public int getCount(){return 5;}};addDataSlots(data);}
    public int value(int index){return data.get(index);}
    public boolean stillValid(Player p){return source==null||!source.isRemoved()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
    public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
