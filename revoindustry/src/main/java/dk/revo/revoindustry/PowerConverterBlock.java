package dk.revo.revoindustry;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
public final class PowerConverterBlock extends Block implements EntityBlock{
 public static final DirectionProperty FACING=BlockStateProperties.FACING;
 public final int tier;
 public PowerConverterBlock(int tier){super(Properties.of().strength(3.5f).sound(SoundType.METAL).noOcclusion());this.tier=tier;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(dk.revo.revolib.energy.ConverterAssemblyState.INTEGRATED,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,dk.revo.revolib.energy.ConverterAssemblyState.INTEGRATED);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getNearestLookingDirection().getOpposite());}
 protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
 protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
  var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
  boolean wrench=id.toString().equals("create:wrench")||id.toString().equals("modern_industrialization:wrench")||stack.is(TagKey.create(Registries.ITEM,ResourceLocation.parse("c:tools/wrench")))||stack.is(TagKey.create(Registries.ITEM,ResourceLocation.parse("neoforge:tools/wrench")));
  if(!wrench)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  if(!p.mayUseItemAt(pos,hit.getDirection(),stack)||!l.mayInteract(p,pos))return ItemInteractionResult.FAIL;
  if(!l.isClientSide){var facing=s.getValue(FACING);var axis=hit.getDirection().getAxis();var next=p.isShiftKeyDown()?facing.getCounterClockWise(axis):facing.getClockWise(axis);if(next==facing)next=facing.getClockWise(axis==Direction.Axis.Y?Direction.Axis.X:Direction.Axis.Y);l.setBlockAndUpdate(pos,s.setValue(FACING,next));l.invalidateCapabilities(pos);}
  return ItemInteractionResult.sidedSuccess(l.isClientSide);
 }
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PowerConverterEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==PrimitiveMachines.CONVERTER_ENTITY.get()?(w,p,st,be)->((PowerConverterEntity)be).tick():null;}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){if(l.getBlockEntity(pos) instanceof PowerConverterEntity be){if(!l.isClientSide)p.openMenu(be);return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
}
