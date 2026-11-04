package dk.revo.revoextraction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.registries.*;
public final class HammerItems {
 public static final DeferredItem<Item> PEBBLE=RevoExtraction.ITEMS.registerSimpleItem("stone_pebble");
 public static final DeferredItem<HammerItem> WOOD=RevoExtraction.ITEMS.register("wooden_hammer",()->new HammerItem(96));
 public static final DeferredItem<HammerItem> STONE=RevoExtraction.ITEMS.register("stone_hammer",()->new HammerItem(256));
 public static final DeferredBlock<Block> DUST=RevoExtraction.BLOCKS.register("dust",()->new Block(Block.Properties.of().strength(.5f).sound(SoundType.SAND)));
 public static void bootstrap(){}
}

