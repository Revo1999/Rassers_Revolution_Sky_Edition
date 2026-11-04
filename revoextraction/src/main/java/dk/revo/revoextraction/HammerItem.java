package dk.revo.revoextraction;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
public class HammerItem extends Item{
 public HammerItem(int durability){super(new Properties().durability(durability));}
 public static ItemStack result(BlockState s){if(s.is(Blocks.DIRT)||s.is(Blocks.COARSE_DIRT)||s.is(Blocks.ROOTED_DIRT))return new ItemStack(HammerItems.PEBBLE.get(),4);if(s.is(Blocks.COBBLESTONE))return new ItemStack(Items.GRAVEL);if(s.is(Blocks.GRAVEL))return new ItemStack(Items.SAND);if(s.is(Blocks.SAND)||s.is(Blocks.RED_SAND))return new ItemStack(HammerItems.DUST.get());return ItemStack.EMPTY;}
 public float getDestroySpeed(ItemStack tool,BlockState s){return result(s).isEmpty()?1:5;}
 public boolean isCorrectToolForDrops(ItemStack tool,BlockState s){return !result(s).isEmpty();}
 public boolean mineBlock(ItemStack tool,Level l,BlockState s,BlockPos p,LivingEntity user){if(!l.isClientSide&&!result(s).isEmpty())tool.hurtAndBreak(1,user,EquipmentSlot.MAINHAND);return true;}
 /** Right-click is an accessible deterministic alternative to mining with the tool. */
 public InteractionResult useOn(UseOnContext c){var level=c.getLevel();var pos=c.getClickedPos();var state=level.getBlockState(pos);var product=result(state);if(product.isEmpty()||c.getPlayer()==null)return InteractionResult.PASS;if(!level.isClientSide){if(!level.mayInteract(c.getPlayer(),pos)||!c.getPlayer().mayUseItemAt(pos,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level,pos,state,c.getPlayer())).isCanceled())return InteractionResult.FAIL;level.destroyBlock(pos,false,c.getPlayer());Block.popResource(level,pos,product);c.getItemInHand().hurtAndBreak(1,c.getPlayer(),LivingEntity.getSlotForHand(c.getHand()));}return InteractionResult.sidedSuccess(level.isClientSide);}
}

