package dk.revo.revocultivation;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
public final class FarmWaterTank extends BaseEntityBlock {
    public FarmWaterTank(){super(Properties.of().strength(4).sound(SoundType.METAL).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FarmPart.FORMED,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new FarmWaterTank());}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FarmPart.FORMED);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new FarmWaterTankEntity(p,s);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
        if(FluidUtil.getFluidHandler(stack).isPresent()){
            if(!level.isClientSide)FluidUtil.interactWithFluidHandler(player,hand,level,p,hit.getDirection());
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        var machine=FarmMachine.find(l,p);
        if(!l.isClientSide&&machine!=null)player.openMenu(new SimpleMenuProvider((id,inv,viewer)->new FarmControllerMenu(id,inv,machine),machine.kind().title()));
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
