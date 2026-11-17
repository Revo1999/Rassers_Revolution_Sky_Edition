package dk.revo.revoindustry.hatchery;

import dk.revo.revolib.energy.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.*;

public final class HatcheryPort extends BlockEntity implements MenuProvider {
    final ItemStackHandler inventory=new ItemStackHandler(9){protected void onContentsChanged(int slot){setChanged();}public boolean isItemValid(int slot,ItemStack stack){return input()&&stack.is(Hatchery.EGG.get());}};
    private long rateTick=Long.MIN_VALUE;
    private int received;
    private boolean exporting;
    public HatcheryPort(BlockPos pos,BlockState state){super(Hatchery.PORT_ENTITY.get(),pos,state);}
    public Hatchery.Part part(){return ((HatcheryBlock)getBlockState().getBlock()).part;}
    public boolean input(){return part()==Hatchery.Part.INPUT;}
    public Direction facing(){return getBlockState().getValue(HatcheryBlock.FACING);}
    public HatcheryController controller(){
        if(level==null)return null;
        int x=part()==Hatchery.Part.INPUT?-2:part()==Hatchery.Part.OUTPUT?2:0;
        int z=part()==Hatchery.Part.ENERGY?4:2;
        for(var front:Direction.Plane.HORIZONTAL){
            var origin=HatcheryLayout.at(worldPosition,front,-x,0,-z);
            if(!level.hasChunkAt(origin))continue;
            if(level.getBlockEntity(origin) instanceof HatcheryController c&&c.facing()==front&&c.validate()&&HatcheryLayout.rotate(part()==Hatchery.Part.INPUT?Direction.WEST:part()==Hatchery.Part.OUTPUT?Direction.EAST:Direction.SOUTH,front)==facing())return c;
        }
        return null;
    }
    private final IItemHandler external=new IItemHandler(){
        public int getSlots(){return 9;}
        public ItemStack getStackInSlot(int slot){return inventory.getStackInSlot(slot);}
        public ItemStack insertItem(int slot,ItemStack stack,boolean sim){return input()&&controller()!=null?inventory.insertItem(slot,stack,sim):stack;}
        public ItemStack extractItem(int slot,int amount,boolean sim){return controller()!=null?inventory.extractItem(slot,amount,sim):ItemStack.EMPTY;}
        public int getSlotLimit(int slot){return input()?16:64;}
        public boolean isItemValid(int slot,ItemStack stack){return input()&&stack.is(Hatchery.EGG.get())&&controller()!=null;}
    };
    public IItemHandler items(Direction side){return part()==Hatchery.Part.ENERGY?null:external;}
    public boolean hasEgg(){for(int i=0;i<9;i++)if(inventory.getStackInSlot(i).is(Hatchery.EGG.get()))return true;return false;}
    public void takeEgg(){for(int i=0;i<9;i++)if(inventory.getStackInSlot(i).is(Hatchery.EGG.get())){inventory.extractItem(i,1,false);return;}}
    public boolean canAcceptStar(){for(int i=0;i<9;i++){var s=inventory.getStackInSlot(i);if(s.isEmpty()||s.is(Items.NETHER_STAR)&&s.getCount()<s.getMaxStackSize())return true;}return false;}
    public void commitStar(){for(int i=0;i<9;i++){var s=inventory.getStackInSlot(i);if(s.isEmpty()){inventory.setStackInSlot(i,new ItemStack(Items.NETHER_STAR));return;}if(s.is(Items.NETHER_STAR)&&s.getCount()<s.getMaxStackSize()){var next=s.copy();next.grow(1);inventory.setStackInSlot(i,next);return;}}throw new IllegalStateException("Output must be reserved before a lethal hit");}
    private int receive(long amount,boolean sim){
        var c=controller();if(c==null||amount<=0)return 0;
        if(rateTick!=level.getGameTime()){rateTick=level.getGameTime();received=0;}
        int accepted=c.receiveEu(Math.min(amount,Hatchery.EU_PER_TICK*4-received),sim);
        if(!sim)received+=accepted;return accepted;
    }
    private final EuStorage euStorage=new EuStorage(){
        public long receive(long n,boolean sim){return HatcheryPort.this.receive(n,sim);}
        public long getAmount(){var c=controller();return c==null?0:c.energy();}
        public long getCapacity(){return controller()==null?0:Hatchery.BUFFER_EU;}
    };
    public EuStorage eu(Direction side){return part()==Hatchery.Part.ENERGY&&side==facing()?euStorage:null;}
    private final IEnergyStorage feStorage=new IEnergyStorage(){
        public int receiveEnergy(int amount,boolean sim){return receive(Math.max(0,amount)/EnergyInterop.fePerEu(),sim)*EnergyInterop.fePerEu();}
        public int extractEnergy(int n,boolean sim){return 0;}
        public int getEnergyStored(){return (int)euStorage.getAmount()*EnergyInterop.fePerEu();}
        public int getMaxEnergyStored(){return (int)euStorage.getCapacity()*EnergyInterop.fePerEu();}
        public boolean canExtract(){return false;}public boolean canReceive(){return true;}
    };
    public IEnergyStorage fe(Direction side){return part()==Hatchery.Part.ENERGY&&side==facing()?feStorage:null;}
    public void tick(){
        if(level==null||level.isClientSide)return;
        if(part()==Hatchery.Part.ENERGY){
            long available=EnergyInterop.moveEu(level,worldPosition,facing(),Hatchery.EU_PER_TICK*4,true,true);
            int accepted=receive(available,true);
            if(accepted>0){long taken=EnergyInterop.moveEu(level,worldPosition,facing(),accepted,false,true);receive(taken,false);}
        }
        if(part()==Hatchery.Part.OUTPUT&&level.getGameTime()%10==0&&controller()!=null)export();
    }
    public void export(){
        if(exporting||level==null||part()!=Hatchery.Part.OUTPUT)return;
        var dest=worldPosition.relative(facing());if(!level.hasChunkAt(dest))return;
        var target=level.getCapability(Capabilities.ItemHandler.BLOCK,dest,facing().getOpposite());if(target==null)return;
        exporting=true;
        try{for(int i=0;i<9;i++){
            var offered=inventory.getStackInSlot(i).copy();if(offered.isEmpty())continue;
            var rest=ItemHandlerHelper.insertItemStacked(target,offered.copy(),true);int count=offered.getCount()-rest.getCount();if(count<=0)continue;
            var taken=inventory.extractItem(i,count,false);var rejected=ItemHandlerHelper.insertItemStacked(target,taken,false);
            if(!rejected.isEmpty()){var own=inventory.getStackInSlot(i).copy();if(own.isEmpty())inventory.setStackInSlot(i,rejected);else{own.grow(rejected.getCount());inventory.setStackInSlot(i,own);}}
        }}finally{exporting=false;}
    }
    public void dropContents(){if(level==null)return;for(int i=0;i<9;i++){var s=inventory.getStackInSlot(i);inventory.setStackInSlot(i,ItemStack.EMPTY);if(!s.isEmpty())Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),s);}}
    protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.put("Inventory",inventory.serializeNBT(p));}
    protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);inventory.deserializeNBT(p,t.getCompound("Inventory"));}
    public Component getDisplayName(){return getBlockState().getBlock().getName();}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new HatcheryMenu(id,inv,this);}
}
