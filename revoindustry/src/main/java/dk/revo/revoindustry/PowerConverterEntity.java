package dk.revo.revoindustry;
import dk.revo.revolib.energy.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.*;
public final class PowerConverterEntity extends BlockEntity implements MenuProvider,DirectConverter{
 public DirectPowerConversion conversion(){return power;}
 public final DirectPowerConversion power=new DirectPowerConversion(EnergyInterop::fePerEu,this::rateEu,()->level==null?0:level.getGameTime(),this::changed,
  (n,s)->moveFe(n,s,true),(n,s)->moveFe(n,s,false),(n,s)->moveEu(n,s,true),(n,s)->moveEu(n,s,false));
 private static final IEnergyStorage CLOSED=new EnergyStorage(0,0,0);
 private final IEnergyStorage fe=new IEnergyStorage(){
  public int receiveEnergy(int n,boolean s){return power.receiveFe(n,s);}public int extractEnergy(int n,boolean s){return power.extractFe(n,s);}
  public int getEnergyStored(){return 0;}public int getMaxEnergyStored(){return 0;}
  public boolean canReceive(){return power.feToEu();}public boolean canExtract(){return !power.feToEu();}
 };
 public PowerConverterEntity(BlockPos p,BlockState s){super(PrimitiveMachines.CONVERTER_ENTITY.get(),p,s);}
 public int tier(){return ((PowerConverterBlock)getBlockState().getBlock()).tier;}public int rateEu(){return new int[]{32,128,512}[tier()];}
 public Direction feFace(){return getBlockState().getValue(PowerConverterBlock.FACING);}public Direction euFace(){return feFace().getOpposite();}
 public IEnergyStorage fePort(Direction side){return side==feFace()?fe:CLOSED;}public DirectPowerConversion euPort(Direction side){return side==euFace()?power:null;}
 private IEnergyStorage neighborFe(){if(level==null)return null;var p=worldPosition.relative(feFace());return level.hasChunkAt(p)?level.getCapability(Capabilities.EnergyStorage.BLOCK,p,feFace().getOpposite()):null;}
 private long moveFe(long n,boolean sim,boolean extract){var other=neighborFe();if(other==null)return 0;int amount=(int)Math.min(Integer.MAX_VALUE,Math.max(0,n));return extract?other.canExtract()?other.extractEnergy(amount,sim):0:other.canReceive()?other.receiveEnergy(amount,sim):0;}
 private long moveEu(long n,boolean sim,boolean extract){return level==null?0:EnergyInterop.moveEu(level,worldPosition,euFace(),n,sim,extract);}
 public void tick(){if(level==null||level.isClientSide)return;
  if(getBlockState().getValue(ConverterAssemblyState.INTEGRATED)){
   boolean host=false,unloaded=false;
   for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
    var p=worldPosition.offset(x,0,z);if(!level.hasChunkAt(p)){unloaded=true;continue;}
    if(level.getBlockEntity(p) instanceof IntegratedPowerHost h&&h.hasIntegratedConverter(worldPosition))host=true;
   }
   if(!host&&!unloaded)level.setBlockAndUpdate(worldPosition,getBlockState().setValue(ConverterAssemblyState.INTEGRATED,false));
  }
  // Only local transient values; no power survives this forwarding operation.
  if(power.feToEu()){
   long available=moveFe((long)rateEu()*power.ratio(),true,true);int accepted=power.receiveFe((int)available,true);
   if(accepted>0){dk.revo.revolib.MachineFeedback.work(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.CONVERTER);int taken=(int)moveFe(accepted,false,true);power.receiveFe(taken,false);}
  }else{
   long available=moveEu(rateEu(),true,true);long accepted=power.receiveEu(available,true);
   if(accepted>0){dk.revo.revolib.MachineFeedback.work(level,worldPosition,dk.revo.revolib.MachineFeedback.Voice.CONVERTER);long taken=moveEu(accepted,false,true);power.receiveEu(taken,false);}
  }
 }
 private void changed(){setChanged();if(level!=null&&!level.isClientSide){level.invalidateCapabilities(worldPosition);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.putBoolean("EuToFe",!power.feToEu());}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);power.setMode(!t.getBoolean("EuToFe"));}
 public Component getDisplayName(){return getBlockState().getBlock().getName();}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new PowerConverterMenu(id,inv,this);}
 public int value(int i){return switch(i){case 0->power.ratio();case 1->rateEu();case 2->power.feToEu()?0:1;default->tier();};}
}
