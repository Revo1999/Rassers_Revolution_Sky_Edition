package dk.revo.revolib.energy;
import java.util.function.BiFunction;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Optional MI boundary: other Revo mods do not need to link MI implementation types. */
public final class EnergyInterop {
 private EnergyInterop(){}
 public static boolean hasMi(){return ModList.get().isLoaded("modern_industrialization");}
 public static int fePerEu(){return hasMi()?MiEnergyBridge.ratio():10;}
 public static <T extends BlockEntity> void registerEu(RegisterCapabilitiesEvent event,BlockEntityType<T> type,BiFunction<T,Direction,DirectPowerConversion> port){if(hasMi())MiEnergyBridge.register(event,type,port);}
 public static <T extends BlockEntity> void registerEuStorage(RegisterCapabilitiesEvent event,BlockEntityType<T> type,BiFunction<T,Direction,EuStorage> port){if(hasMi())MiEnergyBridge.registerStorage(event,type,port);}
 public static long moveEu(Level level,BlockPos pos,Direction face,long n,boolean simulate,boolean extract){return hasMi()?MiEnergyBridge.move(level,pos,face,n,simulate,extract):0;}
}
