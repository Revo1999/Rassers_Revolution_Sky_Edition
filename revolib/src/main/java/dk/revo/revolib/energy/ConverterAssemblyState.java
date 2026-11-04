package dk.revo.revolib.energy;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
/** The converter remains a real block/capability while its host multiblock supplies the fitted housing. */
public final class ConverterAssemblyState {
    public static final BooleanProperty INTEGRATED=BooleanProperty.create("integrated");
    private ConverterAssemblyState(){}
}
