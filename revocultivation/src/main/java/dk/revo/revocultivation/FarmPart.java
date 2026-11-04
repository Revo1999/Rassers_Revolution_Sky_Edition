package dk.revo.revocultivation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
public class FarmPart extends Block {
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public FarmPart(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FORMED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FORMED);}
}
