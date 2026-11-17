package dk.revo.revoextraction;

import net.minecraft.core.Direction;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.registries.*;

public final class SluiceContent {
    public static final DeferredBlock<SluiceBlock> SLUICE = RevoExtraction.BLOCKS.register("mechanical_sluice", SluiceBlock::new);
    public static final DeferredBlock<SluiceFeeder> FEEDER = RevoExtraction.BLOCKS.register("gravel_feeder", SluiceFeeder::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceEntity>> MACHINE = RevoExtraction.ENTITIES.register("mechanical_sluice", () -> BlockEntityType.Builder.of(SluiceEntity::new, SLUICE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceFeeder.Entity>> FEEDER_ENTITY = RevoExtraction.ENTITIES.register("gravel_feeder", () -> BlockEntityType.Builder.of(SluiceFeeder.Entity::new, FEEDER.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<SluiceMenu>> CONTROL_MENU = RevoExtraction.MENUS.register("sluice_control", () -> new MenuType<>((id, inv) -> new SluiceMenu(id, inv, SluiceMenu.Kind.CONTROL, null, null), FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<SluiceMenu>> OUTPUT_MENU = RevoExtraction.MENUS.register("sluice_output", () -> new MenuType<>((id, inv) -> new SluiceMenu(id, inv, SluiceMenu.Kind.OUTPUT, null, null), FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<SluiceMenu>> FEED_MENU = RevoExtraction.MENUS.register("sluice_feeder", () -> new MenuType<>((id, inv) -> new SluiceMenu(id, inv, SluiceMenu.Kind.FEEDER, null, null), FeatureFlags.DEFAULT_FLAGS));
    public static void bootstrap() {
        RevoExtraction.ITEMS.registerSimpleBlockItem(SLUICE);
        RevoExtraction.ITEMS.registerSimpleBlockItem(FEEDER);
    }
    public static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FEEDER_ENTITY.get(), (be, side) -> side == null || side == Direction.UP ? dk.revo.revolib.FeedbackHandlers.items(be,be.input) : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FEEDER_ENTITY.get(), (be, side) -> side == null || side == be.getBlockState().getValue(SluiceFeeder.FACING).getOpposite() ? dk.revo.revolib.FeedbackHandlers.fluids(be,be.water) : null);
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, be, side) -> {
            if (state.getValue(SluiceBlock.PART) != 2) return null;
            var machine = SluiceBlock.controller(level, pos, state);
            return machine != null && machine.assembled() ? machine.outputPort : null;
        }, SLUICE.get());
    }
}
