package dk.revo.revolib.energy;
import net.minecraft.core.BlockPos;
/** Lets an embedded converter recover its standalone model when its host no longer exists. */
public interface IntegratedPowerHost { boolean hasIntegratedConverter(BlockPos converter); }
