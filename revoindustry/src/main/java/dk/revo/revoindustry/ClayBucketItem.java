package dk.revo.revoindustry;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
public class ClayBucketItem extends Item{
 public final int content;
 public ClayBucketItem(int content){super(new Properties().stacksTo(content==0?16:1));this.content=content;}
 @Override public int getBurnTime(ItemStack stack,net.minecraft.world.item.crafting.RecipeType<?> type){return content==2?20000:0;}
 public static void exchange(Player p,InteractionHand hand,ItemStack result){if(p.getAbilities().instabuild){if(!result.isEmpty()&&!p.getInventory().contains(result))p.getInventory().add(result);return;}var held=p.getItemInHand(hand);held.shrink(1);if(held.isEmpty())p.setItemInHand(hand,result);else if(!result.isEmpty()&&!p.getInventory().add(result))p.drop(result,false);}
 public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){var hit=getPlayerPOVHitResult(level,p,content==0?ClipContext.Fluid.SOURCE_ONLY:ClipContext.Fluid.NONE);if(hit.getType()!=HitResult.Type.BLOCK)return InteractionResultHolder.pass(p.getItemInHand(hand));var pos=hit.getBlockPos();if(!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,hit.getDirection(),p.getItemInHand(hand)))return InteractionResultHolder.fail(p.getItemInHand(hand));
  if(content==0){var fluid=level.getFluidState(pos);if(!fluid.isSource()||fluid.getType()!=Fluids.WATER&&fluid.getType()!=Fluids.LAVA)return InteractionResultHolder.pass(p.getItemInHand(hand));var state=level.getBlockState(pos);if(!(state.getBlock() instanceof BucketPickup pickup))return InteractionResultHolder.pass(p.getItemInHand(hand));if(!level.isClientSide){var taken=pickup.pickupBlock(p,level,pos,state);if(taken.isEmpty())return InteractionResultHolder.fail(p.getItemInHand(hand));exchange(p,hand,new ItemStack(fluid.getType()==Fluids.WATER?PrimitiveMachines.WATER_BUCKET.get():PrimitiveMachines.LAVA_BUCKET.get()));}return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),level.isClientSide);}
  var target=pos.relative(hit.getDirection());if(!p.mayUseItemAt(target,hit.getDirection(),p.getItemInHand(hand))||!level.mayInteract(p,target))return InteractionResultHolder.fail(p.getItemInHand(hand));var targetState=level.getBlockState(target);if(!targetState.canBeReplaced()||targetState.hasBlockEntity())return InteractionResultHolder.fail(p.getItemInHand(hand));
  if(!level.isClientSide){var nativeBucket=(BucketItem)(content==1?Items.WATER_BUCKET:Items.LAVA_BUCKET);if(!nativeBucket.emptyContents(p,level,target,hit,p.getItemInHand(hand)))return InteractionResultHolder.fail(p.getItemInHand(hand));exchange(p,hand,content==1?new ItemStack(PrimitiveMachines.BUCKET.get()):ItemStack.EMPTY);}return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),level.isClientSide);
 }
}

