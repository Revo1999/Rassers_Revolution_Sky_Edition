package dk.revo.revolib.energy;
import java.util.function.BiFunction;
import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.config.MIServerConfig;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
final class MiEnergyBridge {
 static int ratio(){return MIServerConfig.INSTANCE.forgeEnergyPerEu.get();}
 static <T extends BlockEntity> void register(RegisterCapabilitiesEvent event,BlockEntityType<T> type,BiFunction<T,Direction,DirectPowerConversion> port){
  event.registerBlockEntity(EnergyApi.SIDED,type,(be,side)->{var b=port.apply(be,side);if(b==null)return EnergyApi.EMPTY;return new MIEnergyStorage(){
   public boolean canConnect(CableTier tier){return true;}
   public long receive(long n,boolean s){return b.receiveEu(n,s);}public long extract(long n,boolean s){return b.extractEu(n,s);}
   public long getAmount(){return 0;}public long getCapacity(){return 0;}
   public boolean canReceive(){return !b.feToEu();}public boolean canExtract(){return b.feToEu();}
  };});
 }
 static <T extends BlockEntity> void registerStorage(RegisterCapabilitiesEvent event,BlockEntityType<T> type,BiFunction<T,Direction,EuStorage> port){
  event.registerBlockEntity(EnergyApi.SIDED,type,(be,side)->{var storage=port.apply(be,side);if(storage==null)return EnergyApi.EMPTY;return new MIEnergyStorage(){
   public boolean canConnect(CableTier tier){return true;}
   public long receive(long n,boolean simulate){return storage.receive(n,simulate);}
   public long extract(long n,boolean simulate){return storage.extract(n,simulate);}
   public long getAmount(){return storage.getAmount();}public long getCapacity(){return storage.getCapacity();}
   public boolean canReceive(){return storage.canReceive();}public boolean canExtract(){return storage.canExtract();}
  };});
 }
 static long move(Level level,BlockPos pos,Direction face,long n,boolean simulate,boolean extract){
  var target=pos.relative(face);if(!level.hasChunkAt(target))return 0;var other=level.getCapability(EnergyApi.SIDED,target,face.getOpposite());
  if(other==null)return 0;return extract?other.canExtract()?other.extract(n,simulate):0:other.canReceive()?other.receive(n,simulate):0;
 }
}
