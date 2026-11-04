package dk.revo.revoindustry;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
public class RevolutionBookItem extends Item{
 public RevolutionBookItem(){super(new Properties().stacksTo(1));}
 public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){if(level.isClientSide)dk.revo.revoindustry.client.RevolutionBookClient.open();return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),level.isClientSide);}
}

