package dk.revo.revocultivation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** Full boundary UVs, borderless glass and culled internal faces join in every direction. */
public final class FarmGlass extends FarmPart {
    public FarmGlass(){super(Properties.of().strength(4).noOcclusion().sound(SoundType.GLASS));}
    @Override protected boolean skipRendering(BlockState state,BlockState adjacent,Direction face){return adjacent.is(this)||super.skipRendering(state,adjacent,face);}
}
