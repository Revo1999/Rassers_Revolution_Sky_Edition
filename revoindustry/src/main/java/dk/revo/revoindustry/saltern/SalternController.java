package dk.revo.revoindustry.saltern;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import dk.revo.revoindustry.hatchery.*;

public final class SalternController extends BlockEntity implements MenuProvider {
    public final FluidTank water=new FluidTank(Saltern.CAPACITY,f->f.is(Fluids.WATER)){protected void onContentsChanged(){setChanged();}};
    public final FluidTank brine=new FluidTank(Saltern.CAPACITY,f->f.is(Saltern.SALT_WATER.get())){protected void onContentsChanged(){setChanged();}};
    private int energy,progress,status,cycles;
    private boolean activeBatch,exporting;
    private long lastTick=Long.MIN_VALUE;
    private String problem="Incomplete structure";
    public SalternController(BlockPos p,BlockState s){super(Saltern.ENTITY.get(),p,s);}
    public Direction facing(){return getBlockState().getValue(HatcheryBlock.FACING);}
    public BlockPos at(int x,int y,int z){return SalternLayout.at(worldPosition,facing(),x,y,z);}
    public int energy(){return energy;}public int progress(){return progress;}public int status(){return status;}public int cycles(){return cycles;}public boolean activeBatch(){return activeBatch;}public String problem(){return problem;}
    public int visibleWater(){return Math.min(1000,water.getFluidAmount()+(activeBatch?1000:0));}
    public boolean validate(){
        if(level==null)return false;
        for(var cell:SalternLayout.cells()){
            var p=SalternLayout.at(worldPosition,facing(),cell.offset());
            if(!level.hasChunkAt(p)){problem="Structure chunk not loaded";return false;}
            var actual=level.getBlockState(p);
            if(cell.block()==net.minecraft.world.level.block.Blocks.AIR?!actual.isAir():!actual.is(cell.block())){problem="Check "+cell.role()+" at "+cell.offset().toShortString();return false;}
            if(!cell.role().equals("casing")&&!cell.role().equals("air")&&!cell.role().equals("core")&&actual.getValue(HatcheryBlock.FACING)!=HatcheryLayout.rotate(cell.face(),facing())){problem="Wrong facing: "+cell.role()+" at "+cell.offset().toShortString();return false;}
        }
        problem="Structure complete";return true;
    }
    public int receiveEu(long amount,boolean simulate){if(amount<=0||!validate())return 0;int n=(int)Math.min(amount,Saltern.ENERGY_CAPACITY-energy);if(!simulate&&n>0){energy+=n;setChanged();}return n;}
    public void tick(){
        if(level==null||level.isClientSide||lastTick==level.getGameTime())return;lastTick=level.getGameTime();
        if(!validate()){status=0;sync();return;}
        export();
        if(brine.getSpace()<Saltern.BATCH_MB){status=3;sync();return;}
        if(energy<Saltern.EU_PER_TICK){status=2;sync();return;}
        if(!activeBatch){if(water.getFluidAmount()<Saltern.BATCH_MB){status=1;sync();return;}water.drain(Saltern.BATCH_MB,FluidAction.EXECUTE);activeBatch=true;progress=0;}
        energy-=Saltern.EU_PER_TICK;progress++;status=4;
        if(progress>=Saltern.DURATION){brine.fill(new FluidStack(Saltern.SALT_WATER.get(),Saltern.BATCH_MB),FluidAction.EXECUTE);progress=0;activeBatch=false;cycles++;export();}
        setChanged();sync();
    }
    public void export(){
        if(exporting||brine.isEmpty()||level==null)return;var pos=at(1,0,1);var face=facing().getClockWise();var dest=pos.relative(face);if(!level.hasChunkAt(dest))return;
        var target=level.getCapability(Capabilities.FluidHandler.BLOCK,dest,face.getOpposite());if(target==null)return;
        exporting=true;try{int offered=target.fill(brine.getFluid().copy(),FluidAction.SIMULATE);if(offered>0){var fluid=brine.drain(offered,FluidAction.EXECUTE);int accepted=target.fill(fluid.copy(),FluidAction.EXECUTE);if(accepted<fluid.getAmount())brine.fill(fluid.copyWithAmount(fluid.getAmount()-accepted),FluidAction.EXECUTE);}}finally{exporting=false;}
    }
    public void reset(){water.setFluid(FluidStack.EMPTY);brine.setFluid(FluidStack.EMPTY);activeBatch=false;progress=energy=0;setChanged();}
    private int syncProgress=-10,syncStatus=-1,syncWater=-1,syncBrine=-1,syncCycles=-1;
    private void sync(){if(syncStatus!=status||Math.abs(syncProgress-progress)>=4||syncWater!=water.getFluidAmount()||syncBrine!=brine.getFluidAmount()||syncCycles!=cycles){syncStatus=status;syncProgress=progress;syncWater=water.getFluidAmount();syncBrine=brine.getFluidAmount();syncCycles=cycles;level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);setChanged();}}
    protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.put("Water",water.writeToNBT(r,new CompoundTag()));t.put("Brine",brine.writeToNBT(r,new CompoundTag()));t.putBoolean("Batch",activeBatch);t.putInt("Progress",progress);t.putInt("Energy",energy);t.putInt("Cycles",cycles);t.putInt("Status",status);t.putString("Problem",problem);}
    protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);water.readFromNBT(r,t.getCompound("Water"));brine.readFromNBT(r,t.getCompound("Brine"));activeBatch=t.getBoolean("Batch");progress=activeBatch?Math.clamp(t.getInt("Progress"),0,Saltern.DURATION-1):0;energy=Math.clamp(t.getInt("Energy"),0,Saltern.ENERGY_CAPACITY);cycles=t.getInt("Cycles");status=t.getInt("Status");problem=t.getString("Problem");}
    public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public Component getDisplayName(){return Component.translatable("block.revoindustry.salt_geode_chamber");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SalternMenu(id,inv,this);}
}
