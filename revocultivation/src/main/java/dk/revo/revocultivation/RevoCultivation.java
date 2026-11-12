package dk.revo.revocultivation;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(RevoCultivation.MOD_ID)
public final class RevoCultivation {
    public static final String MOD_ID = "revocultivation";
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
    public static final DeferredItem<CrookItem> CROOK=ITEMS.register("crook",CrookItem::new);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD_ID);
    public static final DeferredBlock<FarmPart> SUPPORT=BLOCKS.register("timber_support",()->new FarmPart(BlockBehaviour.Properties.of().strength(2.5f).noOcclusion().sound(SoundType.WOOD)));
    public static final DeferredBlock<FarmPart> CASING=BLOCKS.register("wooden_casing",()->new FarmPart(BlockBehaviour.Properties.of().strength(2.5f).noOcclusion().sound(SoundType.WOOD)));
    public static final DeferredBlock<FarmPart> BED=BLOCKS.register("soil_bed",()->new FarmPart(BlockBehaviour.Properties.of().strength(1).noOcclusion().sound(SoundType.GRAVEL)));
    public static final DeferredBlock<FarmPart> GRATE=BLOCKS.register("collection_grate",()->new FarmPart(BlockBehaviour.Properties.of().strength(3).noOcclusion().sound(SoundType.METAL)));
    public static final DeferredBlock<FarmPart> GLASS=BLOCKS.register("glass_panel",()->new FarmPart(BlockBehaviour.Properties.of().strength(.6f).noOcclusion().sound(SoundType.GLASS)));
    public static final DeferredBlock<FarmPart> BARS=BLOCKS.register("barred_panel",()->new FarmPart(BlockBehaviour.Properties.of().strength(3).noOcclusion().sound(SoundType.METAL)));
    public static final DeferredBlock<FarmFilter> FILTER=BLOCKS.register("mob_filter",()->new FarmFilter(false));
    public static final DeferredBlock<FarmHatch> HATCH=BLOCKS.register("item_hatch",FarmHatch::new);
    public static final DeferredBlock<FarmController> TREE=BLOCKS.register("tree_grower",()->new FarmController(MachineKind.TREE));
    public static final DeferredBlock<FarmController> GREENHOUSE=BLOCKS.register("greenhouse",()->new FarmController(MachineKind.GREENHOUSE));
    public static final DeferredBlock<FarmController> STEEL_TREE=BLOCKS.register("steel_tree_grower",()->new FarmController(MachineKind.STEEL_TREE));
    public static final DeferredBlock<FarmController> IRON_GREENHOUSE=BLOCKS.register("iron_greenhouse",()->new FarmController(MachineKind.IRON_GREENHOUSE));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FarmMachine>> MACHINE=ENTITIES.register("first_tier_machine",()->BlockEntityType.Builder.of(FarmMachine::new,java.util.Arrays.stream(MachineKind.values()).filter(MachineKind::registered).map(MachineKind::controller).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FarmHatchEntity>> HATCH_ENTITY=ENTITIES.register("item_hatch",()->BlockEntityType.Builder.of(FarmHatchEntity::new,FarmMaterials.hatches()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FarmWaterTankEntity>> TANK_ENTITY=ENTITIES.register("farm_water_tank",()->BlockEntityType.Builder.of(FarmWaterTankEntity::new,FarmMaterials.IRON.get(MachineKind.Part.TANK).get(),FarmMaterials.STEEL.get(MachineKind.Part.TANK).get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<FarmControllerMenu>> CONTROLLER_MENU=MENUS.register("controller",()->new MenuType<>(FarmControllerMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>,MenuType<FarmHatchMenu>> HATCH_MENU=MENUS.register("hatch",()->IMenuTypeExtension.create((id,inv,buf)->new FarmHatchMenu(id,inv,buf.readEnum(MachineKind.class))));
    public static final DeferredBlock<TradingColony> TRADE=BLOCKS.register("trading_colony",TradingColony::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TradingColony.Entity>> TRADE_ENTITY=ENTITIES.register("trading_colony",()->BlockEntityType.Builder.of(TradingColony.Entity::new,TRADE.get()).build(null));
    public RevoCultivation(IEventBus bus) {
        bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,TRADE_ENTITY.get(),(be,side)->be.items));SmallMachines.bootstrap();FarmMaterials.bootstrap();
        MachineKind.TREE.bind(TREE);MachineKind.GREENHOUSE.bind(GREENHOUSE);
        MachineKind.STEEL_TREE.bind(STEEL_TREE);MachineKind.IRON_GREENHOUSE.bind(IRON_GREENHOUSE);
        for(var block:BLOCKS.getEntries())ITEMS.registerSimpleBlockItem(block);
        BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);MENUS.register(bus);
        bus.addListener(this::capabilities);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)ITEMS.getEntries().forEach(i->e.accept(i.get()));});
    }
    private void capabilities(RegisterCapabilitiesEvent event){
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,SmallMachines.ENTITY.get(),(be,side)->be.items(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,HATCH_ENTITY.get(),(be,side)->be.items());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,HATCH_ENTITY.get(),(be,side)->be.fluids());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,TANK_ENTITY.get(),(be,side)->be.fluids());
        dk.revo.revolib.energy.EnergyInterop.registerEuStorage(event,HATCH_ENTITY.get(),(be,side)->be.euInput());
    }
}
