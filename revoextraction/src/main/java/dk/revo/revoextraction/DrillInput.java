package dk.revo.revoextraction;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
public class DrillInput extends KineticBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public DrillInput() { super(Properties.of().strength(3).noOcclusion()); registerDefaultState(stateDefinition.any().setValue(FACING,Direction.EAST).setValue(DrillPart.FORMED,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(FACING,DrillPart.FORMED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    @Override public Direction.Axis getRotationAxis(BlockState s) { return s.getValue(FACING).getAxis(); }
    @Override public boolean hasShaftTowards(LevelReader l,BlockPos p,BlockState s,Direction d) { return d == s.getValue(FACING); }
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) { return new DrillDrive(p,s); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type) {
        return (world,pos,state,be) -> { if (be instanceof DrillDrive drive) drive.tick(); };
    }
}
