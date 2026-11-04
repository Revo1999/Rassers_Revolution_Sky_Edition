package dk.revo.revocultivation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
/** Real block lighting switches with powered production, while the assembled LEDs render full-bright. */
public final class FarmGrowLight extends FarmPart {
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public FarmGrowLight(){super(Properties.of().strength(4).noOcclusion().sound(SoundType.METAL).lightLevel(s->s.getValue(LIT)?12:0));registerDefaultState(defaultBlockState().setValue(LIT,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){super.createBlockStateDefinition(b);b.add(LIT);}
}
