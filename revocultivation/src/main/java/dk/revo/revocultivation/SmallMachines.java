package dk.revo.revocultivation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
public final class SmallMachines {
 public static final DeferredBlock<SmallMachineBlock> COMPOSTER=RevoCultivation.BLOCKS.register("composter",()->new SmallMachineBlock(false));
 public static final DeferredBlock<SmallMachineBlock> CULTIVATOR=RevoCultivation.BLOCKS.register("silkworm_cultivator",()->new SmallMachineBlock(true));
 public static final DeferredItem<Item> WORM=RevoCultivation.ITEMS.registerSimpleItem("silkworm");
 public static final DeferredItem<Item> COCOON=RevoCultivation.ITEMS.registerSimpleItem("silk_cocoon");
 public static final DeferredItem<Item> SILK=RevoCultivation.ITEMS.registerSimpleItem("silk");
 public static final DeferredItem<BoneMealItem> COMPOST=RevoCultivation.ITEMS.register("compost",()->new BoneMealItem(new Item.Properties()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SmallMachineEntity>> ENTITY=RevoCultivation.ENTITIES.register("small_cultivation",()->BlockEntityType.Builder.of(SmallMachineEntity::new,COMPOSTER.get(),CULTIVATOR.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<SmallMachineMenu>> MENU=RevoCultivation.MENUS.register("small_cultivation",()->IMenuTypeExtension.create((id,inv,buf)->new SmallMachineMenu(id,inv,buf.readBoolean())));
 public static void bootstrap(){}
 private SmallMachines(){}
}

