package dk.revo.revoindustry.vacuum;
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
public final class VacuumBlock extends Block implements EntityBlock {
 public static final DirectionProperty OUTLET=BlockStateProperties.FACING;
 public final int tier;
 public VacuumBlock(int tier){super(Properties.of().strength(3.5f).sound(SoundType.METAL).noOcclusion());this.tier=tier;registerDefaultState(stateDefinition.any().setValue(OUTLET,Direction.SOUTH));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(OUTLET);}
 protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return VacuumShape.get(tier,s.getValue(OUTLET));}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(OUTLET,c.getClickedFace().getOpposite());}
 protected BlockState rotate(BlockState s,Rotation r){return s.setValue(OUTLET,r.rotate(s.getValue(OUTLET)));}
 protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(OUTLET)));}
 protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
  var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
  boolean wrench=id.toString().equals("create:wrench")||id.toString().equals("modern_industrialization:wrench")||stack.is(TagKey.create(Registries.ITEM,ResourceLocation.parse("c:tools/wrench")))||stack.is(TagKey.create(Registries.ITEM,ResourceLocation.parse("neoforge:tools/wrench")));
  if(!wrench)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  if(!p.mayUseItemAt(pos,hit.getDirection(),stack)||!l.mayInteract(p,pos))return ItemInteractionResult.FAIL;
  if(!l.isClientSide){var facing=s.getValue(OUTLET);var axis=hit.getDirection().getAxis();var next=p.isShiftKeyDown()?facing.getCounterClockWise(axis):facing.getClockWise(axis);if(next==facing)next=facing.getClockWise(axis==Direction.Axis.Y?Direction.Axis.X:Direction.Axis.Y);l.setBlockAndUpdate(pos,s.setValue(OUTLET,next));l.invalidateCapabilities(pos);}
  return ItemInteractionResult.sidedSuccess(l.isClientSide);
 }
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new VacuumEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide&&t==Vacuums.ENTITY.get()?(world,pos,state,be)->((VacuumEntity)be).tick():null;}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){if(l.getBlockEntity(pos) instanceof VacuumEntity v){if(!l.isClientSide)p.openMenu(v,b->b.writeVarInt(tier));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 protected void onRemove(BlockState s,Level l,BlockPos pos,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(pos) instanceof VacuumEntity v)for(int i=0;i<v.inventory.getSlots();i++)Containers.dropItemStack(l,pos.getX(),pos.getY(),pos.getZ(),v.inventory.extractItem(i,Integer.MAX_VALUE,false));super.onRemove(s,l,pos,next,moving);}
}
