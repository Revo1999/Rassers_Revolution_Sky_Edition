package dk.revo.revocultivation;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** Early leaf collection with reliable sapling replacement; no infestation minigame. */
public class CrookItem extends Item{
 public CrookItem(){super(new Properties().durability(96));}
 public float getDestroySpeed(ItemStack stack,BlockState state){return state.is(BlockTags.LEAVES)?8:1;}
 public boolean mineBlock(ItemStack tool,Level level,BlockState state,BlockPos pos,LivingEntity user){
  if(!level.isClientSide&&state.is(BlockTags.LEAVES)){Block.popResource(level,pos,new ItemStack(state.getBlock()));var id=BuiltInRegistries.BLOCK.getKey(state.getBlock());String sapling=id.getPath().replace("_leaves","_sapling");var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),sapling));if(tool.getDamageValue()%4==0&&item!=Items.AIR)Block.popResource(level,pos,new ItemStack(item));if(tool.getDamageValue()%16==0&&(state.is(Blocks.OAK_LEAVES)||state.is(Blocks.DARK_OAK_LEAVES)))Block.popResource(level,pos,new ItemStack(Items.APPLE));tool.hurtAndBreak(1,user,EquipmentSlot.MAINHAND);}
  return true;
 }
}

