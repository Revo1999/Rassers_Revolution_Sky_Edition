package dk.revo.revogenetics;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import dk.revo.revocultivation.*;
import net.minecraft.world.item.CreativeModeTabs;

/** Wooden Mob Harvester controller; shares the Cultivation machine infrastructure. */
@Mod(RevoGenetics.MOD_ID)
public final class RevoGenetics {
    public static final String MOD_ID = "revogenetics";
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
    public static final DeferredBlock<FarmController> HARVESTER=BLOCKS.register("mob_harvester",()->new FarmController(MachineKind.HARVESTER));
    public static final DeferredBlock<FarmController> STEEL_HARVESTER=BLOCKS.register("steel_mob_harvester",()->new FarmController(MachineKind.STEEL_HARVESTER));
    public RevoGenetics(IEventBus bus) {
        MachineKind.HARVESTER.bind(HARVESTER);MachineKind.STEEL_HARVESTER.bind(STEEL_HARVESTER);ITEMS.registerSimpleBlockItem(HARVESTER);ITEMS.registerSimpleBlockItem(STEEL_HARVESTER);BLOCKS.register(bus);ITEMS.register(bus);
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS){e.accept(HARVESTER);e.accept(STEEL_HARVESTER);}});
    }
}
