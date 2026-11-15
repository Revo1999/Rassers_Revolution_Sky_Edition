package dk.revo.revoindustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
@Mod(RevoIndustry.MOD_ID)
public final class RevoIndustry {
 public static final String MOD_ID="revoindustry";
 public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
 public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD_ID);
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD_ID);
 public RevoIndustry(IEventBus bus){PrimitiveMachines.bootstrap();dk.revo.revoindustry.vacuum.Vacuums.bootstrap();dk.revo.revoindustry.furnace.Furnaces.bootstrap();dk.revo.revoindustry.hatchery.Hatchery.bootstrap(bus);dk.revo.revoindustry.saltern.Saltern.bootstrap(bus);for(var b:BLOCKS.getEntries())if(!b.getId().getPath().equals("salt_water"))ITEMS.registerSimpleBlockItem(b);BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);MENUS.register(bus);bus.addListener(net.neoforged.bus.api.EventPriority.HIGH,this::capabilities);bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)ITEMS.getEntries().forEach(i->e.accept(i.get()));});}
 private void capabilities(RegisterCapabilitiesEvent e){dk.revo.revoindustry.vacuum.Vacuums.capabilities(e);dk.revo.revoindustry.furnace.Furnaces.capabilities(e);dk.revo.revoindustry.hatchery.Hatchery.capabilities(e);dk.revo.revoindustry.saltern.Saltern.capabilities(e);e.registerBlockEntity(Capabilities.FluidHandler.BLOCK,PrimitiveMachines.ENTITY.get(),(be,side)->be.fluids());e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,PrimitiveMachines.ENTITY.get(),(be,side)->be.items(side));e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,PrimitiveMachines.CONVERTER_ENTITY.get(),(be,side)->be.fePort(side));dk.revo.revolib.energy.EnergyInterop.registerEu(e,PrimitiveMachines.CONVERTER_ENTITY.get(),(be,side)->be.euPort(side));e.registerItem(Capabilities.FluidHandler.ITEM,(stack,ctx)->new ClayBucketFluidHandler(stack),PrimitiveMachines.BUCKET.get(),PrimitiveMachines.WATER_BUCKET.get(),PrimitiveMachines.LAVA_BUCKET.get());}
}
