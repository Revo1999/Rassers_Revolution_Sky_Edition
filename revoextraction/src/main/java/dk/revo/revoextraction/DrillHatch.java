package dk.revo.revoextraction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class DrillHatch extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<DrillHatch> CODEC = simpleCodec(p -> new DrillHatch());
    public DrillHatch() { super(Properties.of().strength(3).noOcclusion()); registerDefaultState(stateDefinition.any().setValue(DrillPart.FORMED,false).setValue(FACING,net.minecraft.core.Direction.NORTH)); }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b) { b.add(DrillPart.FORMED,FACING); }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) { return new HatchEntity(p,s); }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit) {
        if (!l.isClientSide && l.getBlockEntity(p) instanceof HatchEntity h) {
            DrillMachine m=h.machine();
            if(m==null) player.displayClientMessage(Component.literal("Assemble the drill first."),true);
            else player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inventory, viewer) -> new DrillHatchMenu(id,inventory,h,m),Component.translatable("gui.revoextraction.item_output_hatch")));
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
