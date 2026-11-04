package dk.revo.revoextraction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public class DrillController extends BaseEntityBlock {
    public static final MapCodec<DrillController> CODEC = simpleCodec(p -> new DrillController());
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public DrillController() { super(Properties.of().strength(3).noOcclusion()); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DrillPart.FORMED,false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(FACING, DrillPart.FORMED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) { return new DrillMachine(p,s); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t) { return createTickerHelper(t,RevoExtraction.MACHINE.get(),DrillMachine::tick); }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit) {
        if (!l.isClientSide && l.getBlockEntity(p) instanceof DrillMachine m) { m.checkStructure();
            openPanel(l,player,m); }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    public InteractionResult openPanel(Level l,Player player,DrillMachine m){
        if(!l.isClientSide)player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,viewer)->new DrillMenu(id,inventory,m),Component.translatable("gui.revoextraction."+m.tier().prefix())));
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder params){
        var drops=super.getDrops(state,params);
        var entity=params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if(entity instanceof DrillMachine m&&m.tier()==DrillTier.STEEL&&(m.broken()||m.dryTicks()>0)){
            var tag=new net.minecraft.nbt.CompoundTag();tag.putBoolean("DrillBroken",m.broken());tag.putInt("DrillDryTicks",m.dryTicks());
            for(var stack:drops)if(stack.is(RevoExtraction.STEEL_CONTROLLER.get().asItem()))stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
        }
        return drops;
    }
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity placer,net.minecraft.world.item.ItemStack stack){
        super.setPlacedBy(l,p,s,placer,stack);
        if(l.getBlockEntity(p) instanceof DrillMachine m&&m.tier()==DrillTier.STEEL){
            var data=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            m.restoreDamage(data.getBoolean("DrillBroken"),data.getInt("DrillDryTicks"));
        }
    }
    @Override protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving) {
        if (old.getBlock()!=next.getBlock() && l.getBlockEntity(p) instanceof DrillMachine m) { m.dismantle(); m.dropContents(); }
        super.onRemove(old,l,p,next,moving);
    }
}
