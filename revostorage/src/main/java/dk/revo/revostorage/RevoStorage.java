package dk.revo.revostorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
@Mod(RevoStorage.MOD_ID)
public final class RevoStorage {
 public static final String MOD_ID="revostorage";
 public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
 public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD_ID);
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD_ID);
 public static final DeferredBlock<StorageBlock> DRIVE=BLOCKS.register("chest_drive",()->new StorageBlock(false));
 public static final DeferredBlock<StorageBlock> TERMINAL=BLOCKS.register("crafting_terminal",()->new StorageBlock(true));
 public static final DeferredBlock<SpringBlock> SPRING=BLOCKS.register("spring_module",SpringBlock::new);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SpringEntity>> SPRING_ENTITY=ENTITIES.register("spring_module",()->BlockEntityType.Builder.of(SpringEntity::new,SPRING.get()).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StorageEntity>> ENTITY=ENTITIES.register("storage",()->BlockEntityType.Builder.of(StorageEntity::new,DRIVE.get(),TERMINAL.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<StorageMenu>> MENU=MENUS.register("storage",()->new MenuType<>(StorageMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
 public RevoStorage(IEventBus bus){ITEMS.registerSimpleBlockItem(DRIVE);ITEMS.registerSimpleBlockItem(TERMINAL);ITEMS.registerSimpleBlockItem(SPRING);BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);MENUS.register(bus);bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ENTITY.get(),(be,side)->dk.revo.revolib.FeedbackHandlers.items(be,be.networkItems())));bus.addListener(StorageAction::register);bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)ITEMS.getEntries().forEach(i->e.accept(i.get()));});}
}
