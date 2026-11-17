package dk.revo.revoindustry.saltern;

import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revolib.energy.EnergyInterop;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.registries.*;

public final class Saltern {
    public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,name);}
    public static final int EU_PER_TICK=16,DURATION=400,BATCH_MB=1000,CAPACITY=8000,ENERGY_CAPACITY=6400;
    public enum Part { CONTROLLER, WATER, BRINE, ENERGY, CORE }
    public static final DeferredBlock<SalternBlock> CONTROLLER=block("salt_geode_chamber",Part.CONTROLLER);
    public static final DeferredBlock<SalternBlock> WATER=block("geode_water_input",Part.WATER);
    public static final DeferredBlock<SalternBlock> BRINE=block("geode_brine_output",Part.BRINE);
    public static final DeferredBlock<SalternBlock> ENERGY=block("geode_energy_hatch",Part.ENERGY);
    public static final DeferredBlock<SalternBlock> CORE=block("saline_geode",Part.CORE);
    public static final DeferredRegister<FluidType> FLUID_TYPES=DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES,RevoIndustry.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,RevoIndustry.MOD_ID);
    public static final DeferredHolder<FluidType,FluidType> SALT_WATER_TYPE=FLUID_TYPES.register("salt_water",()->new FluidType(FluidType.Properties.create().density(1100).viscosity(1200)));
    public static final DeferredHolder<Fluid,BaseFlowingFluid.Source> SALT_WATER=FLUIDS.register("salt_water",()->new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid,BaseFlowingFluid.Flowing> FLOWING_SALT_WATER=FLUIDS.register("flowing_salt_water",()->new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> LIQUID=RevoIndustry.BLOCKS.register("salt_water",()->new LiquidBlock(SALT_WATER.get(),Block.Properties.ofFullCopy(Blocks.WATER)));
    public static final DeferredItem<BucketItem> BUCKET=RevoIndustry.ITEMS.register("salt_water_bucket",()->new BucketItem(SALT_WATER.get(),new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    private static BaseFlowingFluid.Properties properties(){return new BaseFlowingFluid.Properties(SALT_WATER_TYPE,SALT_WATER,FLOWING_SALT_WATER).block(LIQUID).bucket(BUCKET).slopeFindDistance(4).levelDecreasePerBlock(1).tickRate(5);}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SalternController>> ENTITY=RevoIndustry.ENTITIES.register("salt_geode_chamber",()->BlockEntityType.Builder.of(SalternController::new,CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SalternPort>> PORT=RevoIndustry.ENTITIES.register("salt_geode_port",()->BlockEntityType.Builder.of(SalternPort::new,WATER.get(),BRINE.get(),ENERGY.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<SalternMenu>> MENU=RevoIndustry.MENUS.register("salt_geode_chamber",()->IMenuTypeExtension.create((id,inv,b)->new SalternMenu(id,inv,b.readBlockPos())));
    private static DeferredBlock<SalternBlock> block(String name,Part part){return RevoIndustry.BLOCKS.register(name,()->new SalternBlock(part));}
    public static void bootstrap(IEventBus bus){FLUID_TYPES.register(bus);FLUIDS.register(bus);}
    public static void capabilities(RegisterCapabilitiesEvent e){
        e.registerBlockEntity(Capabilities.FluidHandler.BLOCK,PORT.get(),(be,side)->be.fluids(side));
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,PORT.get(),(be,side)->be.fe(side));
        EnergyInterop.registerEuStorage(e,PORT.get(),(be,side)->be.eu(side));
    }
}
