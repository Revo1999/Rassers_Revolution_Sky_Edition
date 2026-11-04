package dk.revo.revoextraction;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class DrillDrive extends KineticBlockEntity {
    public DrillDrive(BlockPos p,BlockState s) { super(RevoExtraction.DRIVE.get(),p,s); }
    @Override public float calculateStressApplied() { lastStressApplied=getBlockState().is(RevoExtraction.STEEL_INPUT.get())?16:getBlockState().is(RevoExtraction.IRON_INPUT.get())?8:4; return lastStressApplied; }
}
