package dk.revo.revoindustry;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
public final class PrimitiveMachines{
 public static final DeferredBlock<PrimitiveVesselBlock> BARREL=RevoIndustry.BLOCKS.register("rain_barrel",()->new PrimitiveVesselBlock(true));
 public static final DeferredBlock<PrimitiveVesselBlock> CRUCIBLE=RevoIndustry.BLOCKS.register("clay_crucible",()->new PrimitiveVesselBlock(false));
 public static final DeferredBlock<Block> UNFIRED=RevoIndustry.BLOCKS.register("unfired_crucible",()->new Block(Block.Properties.of().strength(.7f).sound(SoundType.GRAVEL).noOcclusion()));
 public static final DeferredItem<Item> RAW_BUCKET=RevoIndustry.ITEMS.registerSimpleItem("unfired_clay_bucket");
 public static final DeferredItem<ClayBucketItem> BUCKET=RevoIndustry.ITEMS.register("clay_bucket",()->new ClayBucketItem(0));
 public static final DeferredItem<ClayBucketItem> WATER_BUCKET=RevoIndustry.ITEMS.register("clay_water_bucket",()->new ClayBucketItem(1));
 public static final DeferredItem<ClayBucketItem> LAVA_BUCKET=RevoIndustry.ITEMS.register("clay_lava_bucket",()->new ClayBucketItem(2));
 public static final DeferredItem<RevolutionBookItem> BOOK=RevoIndustry.ITEMS.register("revolution_book",RevolutionBookItem::new);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PrimitiveVesselEntity>> ENTITY=RevoIndustry.ENTITIES.register("primitive_vessel",()->BlockEntityType.Builder.of(PrimitiveVesselEntity::new,BARREL.get(),CRUCIBLE.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<PrimitiveVesselMenu>> MENU=RevoIndustry.MENUS.register("primitive_vessel",()->IMenuTypeExtension.create((id,inv,buf)->new PrimitiveVesselMenu(id,inv,buf.readBoolean())));
 public static final DeferredBlock<PowerConverterBlock> CONVERTER=RevoIndustry.BLOCKS.register("power_converter",()->new PowerConverterBlock(0));
 public static final DeferredBlock<PowerConverterBlock> ADVANCED_CONVERTER=RevoIndustry.BLOCKS.register("advanced_power_converter",()->new PowerConverterBlock(1));
 public static final DeferredBlock<PowerConverterBlock> ELITE_CONVERTER=RevoIndustry.BLOCKS.register("elite_power_converter",()->new PowerConverterBlock(2));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PowerConverterEntity>> CONVERTER_ENTITY=RevoIndustry.ENTITIES.register("power_converter",()->BlockEntityType.Builder.of(PowerConverterEntity::new,CONVERTER.get(),ADVANCED_CONVERTER.get(),ELITE_CONVERTER.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<PowerConverterMenu>> CONVERTER_MENU=RevoIndustry.MENUS.register("power_converter",()->new MenuType<>(PowerConverterMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
 public static void bootstrap(){}
}

