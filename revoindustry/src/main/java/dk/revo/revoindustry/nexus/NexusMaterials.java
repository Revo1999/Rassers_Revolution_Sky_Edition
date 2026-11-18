package dk.revo.revoindustry.nexus;

import com.google.gson.*;
import dk.revo.revoindustry.RevoIndustry;
import dk.revo.revoindustry.saltern.Saltern;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.registries.*;

/** Stable material identifiers from the checked, generated survival recipe catalog. */
public final class NexusMaterials {
 public static final JsonObject CATALOG=load();
 public static final Map<String,DeferredItem<Item>> ITEMS=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<FluidType,FluidType>> TYPES=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<Fluid,BaseFlowingFluid.Source>> FLUIDS=new LinkedHashMap<>();
 private static final Map<String,DeferredHolder<Fluid,BaseFlowingFluid.Flowing>> FLOWING=new LinkedHashMap<>();
 private static final Map<String,DeferredBlock<LiquidBlock>> BLOCKS=new LinkedHashMap<>();
 private static final Map<String,DeferredItem<BucketItem>> BUCKETS=new LinkedHashMap<>();
 static {
  for(var raw:CATALOG.getAsJsonArray("items")){String name=raw.getAsString();ITEMS.put(name,RevoIndustry.ITEMS.registerSimpleItem(name,new Item.Properties().stacksTo(name.startsWith("nexus_mould_")?1:64)));}
  for(var entry:CATALOG.getAsJsonObject("fluids").entrySet()){
   String name=entry.getKey();TYPES.put(name,Saltern.FLUID_TYPES.register(name,()->new FluidType(FluidType.Properties.create().density(1200).viscosity(1600))));
   FLUIDS.put(name,Saltern.FLUIDS.register(name,()->new BaseFlowingFluid.Source(properties(name))));
   FLOWING.put(name,Saltern.FLUIDS.register("flowing_"+name,()->new BaseFlowingFluid.Flowing(properties(name))));
   BLOCKS.put(name,RevoIndustry.BLOCKS.register(name,()->new LiquidBlock(FLUIDS.get(name).get(),Block.Properties.ofFullCopy(Blocks.WATER))));
   BUCKETS.put(name,RevoIndustry.ITEMS.register(name+"_bucket",()->new BucketItem(FLUIDS.get(name).get(),new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))));
  }
 }
 private static BaseFlowingFluid.Properties properties(String name){return new BaseFlowingFluid.Properties(TYPES.get(name),FLUIDS.get(name),FLOWING.get(name)).block(BLOCKS.get(name)).bucket(BUCKETS.get(name)).slopeFindDistance(3).levelDecreasePerBlock(2).tickRate(10);}
 private static JsonObject load(){try(var in=NexusMaterials.class.getResourceAsStream("/nexus/materials.json")){if(in==null)throw new IllegalStateException("Missing Nexus material catalog");return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}catch(IOException ex){throw new ExceptionInInitializerError(ex);}}
 public static Item item(String name){return ITEMS.get(name).get();}
 public static Fluid fluid(String name){return FLUIDS.get(name).get();}
 public static boolean fluidBlock(String name){return CATALOG.getAsJsonObject("fluids").has(name);}
 public static void bootstrap(){}
 private NexusMaterials(){}
}
