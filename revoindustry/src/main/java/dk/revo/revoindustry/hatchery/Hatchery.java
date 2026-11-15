package dk.revo.revoindustry.hatchery;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revolib.energy.EnergyInterop;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class Hatchery {
    public static final int EU_PER_TICK=8192, INCUBATION_TICKS=1200, HATCH_TICKS=100, KILL_TICKS=200;
    public static final int BUFFER_EU=EU_PER_TICK*40;
    public enum Part { CASING, GLASS, LIGHT, CONTROLLER, INPUT, OUTPUT, ENERGY }
    public static final DeferredBlock<HatcheryBlock> CASING=block("wither_chamber_casing",Part.CASING);
    public static final DeferredBlock<HatcheryBlock> GLASS=block("wither_chamber_glass",Part.GLASS);
    public static final DeferredBlock<HatcheryBlock> LIGHT=block("wither_chamber_light",Part.LIGHT);
    public static final DeferredBlock<HatcheryBlock> CONTROLLER=block("wither_hatchery",Part.CONTROLLER);
    public static final DeferredBlock<HatcheryBlock> INPUT=block("wither_egg_input_hatch",Part.INPUT);
    public static final DeferredBlock<HatcheryBlock> OUTPUT=block("wither_star_output_hatch",Part.OUTPUT);
    public static final DeferredBlock<HatcheryBlock> ENERGY=block("wither_energy_hatch",Part.ENERGY);
    public static final DeferredItem<Item> EGG=RevoIndustry.ITEMS.registerSimpleItem("wither_egg",new Item.Properties().stacksTo(16).fireResistant());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<HatcheryController>> CONTROLLER_ENTITY=RevoIndustry.ENTITIES.register("wither_hatchery",()->BlockEntityType.Builder.of(HatcheryController::new,CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<HatcheryPort>> PORT_ENTITY=RevoIndustry.ENTITIES.register("wither_chamber_port",()->BlockEntityType.Builder.of(HatcheryPort::new,INPUT.get(),OUTPUT.get(),ENERGY.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<HatcheryMenu>> MENU=RevoIndustry.MENUS.register("wither_hatchery",()->IMenuTypeExtension.create((id,inv,b)->new HatcheryMenu(id,inv,b.readBlockPos(),b.readBoolean())));
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,RevoIndustry.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<ChamberWither>> WITHER=ENTITY_TYPES.register("chamber_wither",()->EntityType.Builder.<ChamberWither>of(ChamberWither::new,MobCategory.MONSTER).sized(.9f,3.5f).fireImmune().clientTrackingRange(10).updateInterval(1).build("revoindustry:chamber_wither"));
    private static DeferredBlock<HatcheryBlock> block(String id,Part part){return RevoIndustry.BLOCKS.register(id,()->new HatcheryBlock(part));}
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,path);}
    public static void bootstrap(IEventBus bus){ENTITY_TYPES.register(bus);bus.addListener((EntityAttributeCreationEvent e)->e.put(WITHER.get(),net.minecraft.world.entity.boss.wither.WitherBoss.createAttributes().build()));}
    public static void capabilities(RegisterCapabilitiesEvent e){
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,PORT_ENTITY.get(),(be,side)->be.items(side));
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,PORT_ENTITY.get(),(be,side)->be.fe(side));
        EnergyInterop.registerEuStorage(e,PORT_ENTITY.get(),(be,side)->be.eu(side));
    }
}
