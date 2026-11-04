package dk.revo.revoextraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
public class DrillPart extends Block {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public DrillPart(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(FORMED, false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FORMED); }
}
