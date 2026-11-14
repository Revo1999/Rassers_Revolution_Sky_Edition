package dk.revo.revoextraction;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.*;

@Mod(RevoExtraction.MOD_ID)
public final class RevoExtraction {
    public static final String MOD_ID = "revoextraction";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<DrillMenu>> DRILL_MENU = MENUS.register("wooden_drill", () -> new net.minecraft.world.inventory.MenuType<>(DrillMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<DrillHatchMenu>> HATCH_MENU = MENUS.register("wooden_drill_output", () -> new net.minecraft.world.inventory.MenuType<>(DrillHatchMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredBlock<Block> SUPPORT = BLOCKS.register("wooden_drill_support", () -> new DrillPart(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> CASING = BLOCKS.register("wooden_drill_casing", () -> new DrillPart(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> CORE = BLOCKS.register("wooden_drill_core", () -> new DrillPart(BlockBehaviour.Properties.of().strength(3f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<DrillController> CONTROLLER = BLOCKS.register("wooden_drill_controller", () -> new DrillController());
    public static final DeferredBlock<DrillInput> INPUT = BLOCKS.register("wooden_drill_input", () -> new DrillInput());
    public static final DeferredBlock<DrillHatch> HATCH = BLOCKS.register("wooden_drill_output_hatch", () -> new DrillHatch());
    public static final DeferredBlock<Block> IRON_SUPPORT = BLOCKS.register("iron_drill_support", () -> new DrillPart(BlockBehaviour.Properties.of().strength(4f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> IRON_CASING = BLOCKS.register("iron_drill_casing", () -> new DrillPart(BlockBehaviour.Properties.of().strength(4f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> IRON_CORE = BLOCKS.register("iron_drill_core", () -> new DrillPart(BlockBehaviour.Properties.of().strength(5f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<DrillController> IRON_CONTROLLER = BLOCKS.register("iron_drill_controller", DrillController::new);
    public static final DeferredBlock<DrillInput> IRON_INPUT = BLOCKS.register("iron_drill_input", DrillInput::new);
    public static final DeferredBlock<DrillHatch> IRON_HATCH = BLOCKS.register("iron_drill_output_hatch", DrillHatch::new);
    public static final DeferredBlock<Block> STEEL_SUPPORT=BLOCKS.register("steel_drill_support",()->new DrillPart(BlockBehaviour.Properties.of().strength(6).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> STEEL_CASING=BLOCKS.register("steel_drill_casing",()->new DrillPart(BlockBehaviour.Properties.of().strength(6).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> STEEL_CORE=BLOCKS.register("steel_drill_core",()->new DrillPart(BlockBehaviour.Properties.of().strength(6).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<DrillController> STEEL_CONTROLLER=BLOCKS.register("steel_drill_controller",DrillController::new);
    public static final DeferredBlock<DrillInput> STEEL_INPUT=BLOCKS.register("steel_drill_input",DrillInput::new);
    public static final DeferredBlock<DrillHatch> STEEL_HATCH=BLOCKS.register("steel_drill_output_hatch",DrillHatch::new);
    public static final DeferredBlock<DrillWaterTank> STEEL_TANK=BLOCKS.register("steel_drill_water_tank",DrillWaterTank::new);
    public static final DeferredItem<DrillRepairKit> REPAIR_KIT=ITEMS.register("drill_repair_kit",DrillRepairKit::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DrillWaterTankEntity>> WATER_TANK_ENTITY=ENTITIES.register("steel_drill_water_tank",()->BlockEntityType.Builder.of(DrillWaterTankEntity::new,STEEL_TANK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillMachine>> MACHINE = ENTITIES.register("wooden_drill", () -> BlockEntityType.Builder.of(DrillMachine::new, CONTROLLER.get(), IRON_CONTROLLER.get(), STEEL_CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillDrive>> DRIVE = ENTITIES.register("wooden_drill_drive", () -> BlockEntityType.Builder.of(DrillDrive::new, INPUT.get(), IRON_INPUT.get(), STEEL_INPUT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HatchEntity>> OUTPUT = ENTITIES.register("wooden_drill_output", () -> BlockEntityType.Builder.of(HatchEntity::new, HATCH.get(), IRON_HATCH.get(), STEEL_HATCH.get()).build(null));
    public static final DeferredBlock<ElectricDrill> ELECTRIC=BLOCKS.register("electric_drill",ElectricDrill::new);
    public static final DeferredItem<Item> SURVEY_CORE=ITEMS.register("deep_survey_core",()->new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ElectricDrillEntity>> ELECTRIC_ENTITY=ENTITIES.register("electric_drill",()->BlockEntityType.Builder.of(ElectricDrillEntity::new,ELECTRIC.get()).build(null));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,net.minecraft.world.inventory.MenuType<ElectricDrillMenu>> ELECTRIC_MENU=MENUS.register("electric_drill",()->new net.minecraft.world.inventory.MenuType<>(ElectricDrillMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<Item> TUNGSTEN_CORE=ITEMS.registerSimpleItem("tungsten_survey_core",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> NUCLEAR_CORE=ITEMS.registerSimpleItem("nuclear_survey_core",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> LOGIC_BLANK=ITEMS.registerSimpleItem("logic_press_blank");
    public static final DeferredItem<Item> CALCULATION_BLANK=ITEMS.registerSimpleItem("calculation_press_blank");
    public static final DeferredItem<Item> ENGINEERING_BLANK=ITEMS.registerSimpleItem("engineering_press_blank");
    public static final DeferredItem<Item> SILICON_BLANK=ITEMS.registerSimpleItem("silicon_press_blank");
    public static final DeferredItem<Item> TEMPLATE_BLANK=ITEMS.registerSimpleItem("netherite_template_blank");
    public RevoExtraction(IEventBus bus) {
        SluiceContent.bootstrap();
        ITEMS.registerSimpleBlockItem(ELECTRIC);HammerItems.bootstrap();ITEMS.registerSimpleBlockItem(HammerItems.DUST);
        for (var block : new DeferredBlock<?>[]{SUPPORT, CASING, CORE, CONTROLLER, INPUT, HATCH, IRON_SUPPORT, IRON_CASING, IRON_CORE, IRON_CONTROLLER, IRON_INPUT, IRON_HATCH, STEEL_SUPPORT, STEEL_CASING, STEEL_CORE, STEEL_CONTROLLER, STEEL_INPUT, STEEL_HATCH, STEEL_TANK}) ITEMS.registerSimpleBlockItem(block);
        BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); MENUS.register(bus);
        bus.addListener(this::capabilities);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) ITEMS.getEntries().forEach(i -> e.accept(i.get()));
        });
    }
    private void capabilities(RegisterCapabilitiesEvent event) {
        SluiceContent.capabilities(event);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ELECTRIC_ENTITY.get(),(be,side)->side==net.minecraft.core.Direction.UP?new net.neoforged.neoforge.items.wrapper.RangedWrapper(be.items,0,2):be.outputPort());
        dk.revo.revolib.energy.EnergyInterop.registerEuStorage(event,ELECTRIC_ENTITY.get(),(be,side)->be);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,WATER_TANK_ENTITY.get(),(be,side)->be.water);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, OUTPUT.get(), (be, side) -> be.output());
    }
}
