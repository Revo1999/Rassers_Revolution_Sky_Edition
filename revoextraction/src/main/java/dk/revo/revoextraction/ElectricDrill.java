package dk.revo.revoextraction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
public class ElectricDrill extends BaseEntityBlock {
 public ElectricDrill(){super(Properties.of().strength(6).sound(SoundType.METAL));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new ElectricDrill());}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ElectricDrillEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,RevoExtraction.ELECTRIC_ENTITY.get(),ElectricDrillEntity::tick);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof ElectricDrillEntity e)player.openMenu(new SimpleMenuProvider((id,inv,pl)->new ElectricDrillMenu(id,inv,e),Component.literal("Electric Geological Drill")));return InteractionResult.sidedSuccess(l.isClientSide);}
 protected void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof ElectricDrillEntity e)for(int i=0;i<e.items.getSlots();i++)Containers.dropItemStack(l,p.getX(),p.getY(),p.getZ(),e.items.getStackInSlot(i));super.onRemove(old,l,p,next,moving);}
}
