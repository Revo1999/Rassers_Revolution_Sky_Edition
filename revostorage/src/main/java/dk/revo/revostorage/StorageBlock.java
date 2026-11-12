package dk.revo.revostorage;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public class StorageBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public final boolean terminal;
 public StorageBlock(boolean terminal){super(Properties.of().strength(4).sound(SoundType.METAL).noOcclusion());this.terminal=terminal;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new StorageBlock(terminal));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new StorageEntity(p,s);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof StorageEntity e){if(player.isShiftKeyDown()&&terminal){e.wind();player.displayClientMessage(net.minecraft.network.chat.Component.literal("Spring charge: "+e.charge+" / 4096 operations"),true);}else player.openMenu(new SimpleMenuProvider((id,inv,pl)->new StorageMenu(id,inv,e),net.minecraft.network.chat.Component.literal(terminal?"Crafting Terminal":"Chest Drive")));}return InteractionResult.sidedSuccess(l.isClientSide);}
 protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof StorageEntity e)e.dropContents();super.onRemove(old,l,p,next,moving);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,RevoStorage.ENTITY.get(),StorageEntity::tick);}
}
