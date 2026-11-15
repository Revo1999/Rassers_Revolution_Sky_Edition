package dk.revo.revoindustry.saltern;

import dk.revo.revoindustry.hatchery.*;
import dk.revo.revolib.energy.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class SalternPort extends BlockEntity {
    private long rateTick=Long.MIN_VALUE;private int received;
    public SalternPort(BlockPos p,BlockState s){super(Saltern.PORT.get(),p,s);}
    public Saltern.Part part(){return ((SalternBlock)getBlockState().getBlock()).part;}
    public Direction facing(){return getBlockState().getValue(HatcheryBlock.FACING);}
    public SalternController controller(){
        if(level==null)return null;int x=part()==Saltern.Part.WATER?-1:part()==Saltern.Part.BRINE?1:0,z=part()==Saltern.Part.ENERGY?2:1;
        for(var front:Direction.Plane.HORIZONTAL){var origin=SalternLayout.at(worldPosition,front,-x,0,-z);if(!level.hasChunkAt(origin))continue;if(level.getBlockEntity(origin) instanceof SalternController c&&c.facing()==front&&c.validate()&&facing()==HatcheryLayout.rotate(part()==Saltern.Part.WATER?Direction.WEST:part()==Saltern.Part.BRINE?Direction.EAST:Direction.SOUTH,front))return c;}
        return null;
    }
    private final IFluidHandler fluidPort=new IFluidHandler(){
        public int getTanks(){return 1;}
        public FluidStack getFluidInTank(int tank){var c=controller();return c==null?FluidStack.EMPTY:(part()==Saltern.Part.WATER?c.water:c.brine).getFluid().copy();}
        public int getTankCapacity(int tank){return Saltern.CAPACITY;}
        public boolean isFluidValid(int tank,FluidStack fluid){return part()==Saltern.Part.WATER&&fluid.is(Fluids.WATER)&&controller()!=null;}
        public int fill(FluidStack fluid,FluidAction action){var c=controller();return c!=null&&part()==Saltern.Part.WATER?c.water.fill(fluid,action):0;}
        public FluidStack drain(FluidStack fluid,FluidAction action){var c=controller();return c!=null&&part()==Saltern.Part.BRINE?c.brine.drain(fluid,action):FluidStack.EMPTY;}
        public FluidStack drain(int amount,FluidAction action){var c=controller();return c!=null&&part()==Saltern.Part.BRINE?c.brine.drain(amount,action):FluidStack.EMPTY;}
    };
    public IFluidHandler fluids(Direction side){return part()!=Saltern.Part.ENERGY&&(side==null||side==facing())?fluidPort:null;}
    private int receive(long n,boolean sim){var c=controller();if(c==null||n<=0)return 0;if(rateTick!=level.getGameTime()){rateTick=level.getGameTime();received=0;}int accepted=c.receiveEu(Math.min(n,64-received),sim);if(!sim)received+=accepted;return accepted;}
    private final EuStorage storage=new EuStorage(){public long receive(long n,boolean sim){return SalternPort.this.receive(n,sim);}public long getAmount(){var c=controller();return c==null?0:c.energy();}public long getCapacity(){return controller()==null?0:Saltern.ENERGY_CAPACITY;}};
    private final IEnergyStorage feStorage=new IEnergyStorage(){
        public int receiveEnergy(int n,boolean sim){int ratio=EnergyInterop.fePerEu();return receive(Math.max(0,n)/ratio,sim)*ratio;}public int extractEnergy(int n,boolean sim){return 0;}
        public int getEnergyStored(){return (int)storage.getAmount()*EnergyInterop.fePerEu();}public int getMaxEnergyStored(){return Saltern.ENERGY_CAPACITY*EnergyInterop.fePerEu();}public boolean canReceive(){return true;}public boolean canExtract(){return false;}
    };
    public EuStorage eu(Direction side){return part()==Saltern.Part.ENERGY&&side==facing()?storage:null;}
    public IEnergyStorage fe(Direction side){return part()==Saltern.Part.ENERGY&&side==facing()?feStorage:null;}
    public void tick(){if(level==null||level.isClientSide||part()!=Saltern.Part.ENERGY)return;long available=EnergyInterop.moveEu(level,worldPosition,facing(),64,true,true);int accepted=receive(available,true);if(accepted>0){long taken=EnergyInterop.moveEu(level,worldPosition,facing(),accepted,false,true);receive(taken,false);}}
}
