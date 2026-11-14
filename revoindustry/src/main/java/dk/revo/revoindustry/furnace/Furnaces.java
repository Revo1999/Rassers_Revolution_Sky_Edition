package dk.revo.revoindustry.furnace;
import dk.revo.revoindustry.RevoIndustry;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
public final class Furnaces {
 public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,path);}
 public static final HatchType FUEL=HatchTypes.register(id("furnace_fuel"),Component.translatable("hatch.revoindustry.furnace_fuel"));
 public static final HatchType VENT=HatchTypes.register(id("furnace_vent"),Component.translatable("hatch.revoindustry.furnace_vent"));
 public static final Map<FurnaceTier,DeferredBlock<MachineBlock>> CONTROLLERS=new EnumMap<>(FurnaceTier.class);
 public static final List<DeferredBlock<MachineBlock>> ITEM_HATCHES=new ArrayList<>();
 public static final List<DeferredBlock<MachineBlock>> FUEL_HATCHES=new ArrayList<>();
 public static final DeferredBlock<MachineBlock> VENT_BLOCK=RevoIndustry.BLOCKS.register("furnace_vent",()->new MachineBlock((p,s)->new FurnaceVent(new BEP(Furnaces.VENT_ENTITY.get(),p,s)),properties()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FurnaceVent>> VENT_ENTITY=RevoIndustry.ENTITIES.register("furnace_vent",()->BlockEntityType.Builder.of((p,s)->new FurnaceVent(new BEP(Furnaces.VENT_ENTITY.get(),p,s)),VENT_BLOCK.get()).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FurnaceController>> CONTROLLER_ENTITY=RevoIndustry.ENTITIES.register("multiblock_furnace",()->BlockEntityType.Builder.of((p,s)->new FurnaceController(new BEP(Furnaces.CONTROLLER_ENTITY.get(),p,s),tier(s.getBlock())),CONTROLLERS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FurnaceFuelHatch>> FUEL_ENTITY=RevoIndustry.ENTITIES.register("furnace_fuel_hatch",()->BlockEntityType.Builder.of((p,s)->createFuel(p,s),FUEL_HATCHES.stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FurnaceItemHatch>> ITEM_ENTITY=RevoIndustry.ENTITIES.register("furnace_item_hatch",()->BlockEntityType.Builder.of((p,s)->{int i=0;for(var b:ITEM_HATCHES){if(b.get()==s.getBlock())return new FurnaceItemHatch(new BEP(Furnaces.ITEM_ENTITY.get(),p,s),i%2==0,i/2);i++;}throw new IllegalStateException("Unknown furnace item hatch");},ITEM_HATCHES.stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
 static {
  for(var tier:FurnaceTier.values()){
   if(tier.casingId.startsWith("revoindustry:"))RevoIndustry.BLOCKS.register(tier.id+"_furnace_casing",()->new Block(properties()));
   MachineCasings.createBlockImitation(ResourceLocation.parse("revoindustry:"+tier.id+"_furnace"),tier::casingBlock);

   CONTROLLERS.put(tier,RevoIndustry.BLOCKS.register(tier.id+"_multiblock_furnace",()->new MachineBlock((p,s)->new FurnaceController(new BEP(CONTROLLER_ENTITY.get(),p,s),tier),properties())));
  }
  for(int grade=0;grade<3;grade++)for(int direction=0;direction<2;direction++){final int g=grade;final boolean input=direction==0;ITEM_HATCHES.add(RevoIndustry.BLOCKS.register("furnace_item_"+(input?"input":"output")+"_hatch_"+(grade+1),()->new MachineBlock((p,s)->new FurnaceItemHatch(new BEP(ITEM_ENTITY.get(),p,s),input,g),properties())));}
  for(int grade=0;grade<3;grade++)for(var kind:FurnaceFuelHatch.Kind.values()){
   final int g=grade;FUEL_HATCHES.add(RevoIndustry.BLOCKS.register(kind.name().toLowerCase(Locale.ROOT)+"_fuel_hatch_"+(grade+1),()->new MachineBlock((p,s)->new FurnaceFuelHatch(new BEP(FUEL_ENTITY.get(),p,s),kind,g),properties())));
  }
 }
 private static Block.Properties properties(){return Block.Properties.of().strength(5,6).sound(SoundType.METAL).noOcclusion();}
 public static FurnaceTier tier(Block block){return CONTROLLERS.entrySet().stream().filter(e->e.getValue().get()==block).findFirst().orElseThrow().getKey();}
 private static FurnaceFuelHatch createFuel(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){int i=0;for(var b:FUEL_HATCHES){if(b.get()==s.getBlock())return new FurnaceFuelHatch(new BEP(FUEL_ENTITY.get(),p,s),FurnaceFuelHatch.Kind.values()[i%3],i/3);i++;}throw new IllegalStateException("Unknown furnace fuel hatch");}
 public static void bootstrap(){}
 public static void capabilities(RegisterCapabilitiesEvent e){
  e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ITEM_ENTITY.get(),(be,side)->side==null||side==be.portFace()?dk.revo.revolib.FeedbackHandlers.items(be,be.getInventory().itemStorage.itemHandler):null);
  e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,FUEL_ENTITY.get(),(be,side)->side==null||side==be.portFace()?dk.revo.revolib.FeedbackHandlers.items(be,be.items()):null);
  e.registerBlockEntity(Capabilities.FluidHandler.BLOCK,FUEL_ENTITY.get(),(be,side)->side==null||side==be.portFace()?dk.revo.revolib.FeedbackHandlers.fluids(be,be.fluids()):null);
 }
}
