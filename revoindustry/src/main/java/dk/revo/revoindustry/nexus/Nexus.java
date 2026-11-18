package dk.revo.revoindustry.nexus;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import dk.revo.revoindustry.RevoIndustry;
import aztech.modern_industrialization.machines.*;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Native MI controllers/hatches around individually placed Blockbench-authored parts. */
public final class Nexus {
    public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath(RevoIndustry.MOD_ID,name);}
    public static final JsonObject MANIFEST=load();
    public static final Map<String,DeferredBlock<? extends Block>> BLOCKS=new LinkedHashMap<>();
    public static final Map<String,MachineRecipeType> RECIPES=new LinkedHashMap<>();
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,net.minecraft.world.inventory.MenuType<NexusBuilderMenu>> BUILDER_MENU=RevoIndustry.MENUS.register("nexus_builder",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.<NexusBuilderMenu>create((id,inv,buffer)->new NexusBuilderMenu(id,inv,buffer.readBlockPos())));
    public static final DeferredItem<net.minecraft.world.item.Item> PROGRAM=RevoIndustry.ITEMS.registerSimpleItem("nexus_construction_program",new net.minecraft.world.item.Item.Properties().stacksTo(1));
    private static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,RevoIndustry.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,RevoIndustry.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<NexusMachine>> ENTITY=RevoIndustry.ENTITIES.register("nexus_machine",()->BlockEntityType.Builder.of((pos,state)->new NexusMachine(new BEP(Nexus.ENTITY.get(),pos,state),kind(state.getBlock())),MANIFEST.getAsJsonObject("structures").entrySet().stream().map(e->block(e.getValue().getAsJsonObject().get("controller").getAsString())).toArray(Block[]::new)).build(null));
    static {
        for(var entry:MANIFEST.getAsJsonObject("structures").entrySet()){
            var type=new MachineRecipeType(id(entry.getKey())).withItemInputs().withItemOutputs().withFluidInputs().withFluidOutputs();
            RECIPES.put(entry.getKey(),type);TYPES.register(entry.getKey(),()->type);SERIALIZERS.register(entry.getKey(),()->type);
        }
        for(var entry:MANIFEST.getAsJsonObject("blocks").entrySet()){
            String name=ResourceLocation.parse(entry.getKey()).getPath();var meta=entry.getValue().getAsJsonObject();
            if(meta.get("controller").getAsBoolean())BLOCKS.put(name,RevoIndustry.BLOCKS.register(name,()->new NexusControllerBlock((pos,state)->new NexusMachine(new BEP(ENTITY.get(),pos,state),kind(state.getBlock())),properties(name))));
            else if(name.equals("nexus_rail"))BLOCKS.put(name,RevoIndustry.BLOCKS.register(name,()->new FenceBlock(properties(name))));
            else BLOCKS.put(name,RevoIndustry.BLOCKS.register(name,()->new NexusBlock(properties(name))));
        }
        MachineCasings.createBlockImitation(id("nexus_machine"),()->block("nexus_machine_casing"));
    }
    private static Block.Properties properties(String name){var p=Block.Properties.of().strength(6,30).sound(SoundType.METAL).noOcclusion();if(name.contains("light_")||name.endsWith("beacon"))p.lightLevel(s->12);if(name.endsWith("core"))p.lightLevel(s->10);return p;}
    private static JsonObject load(){
        try(var stream=Nexus.class.getResourceAsStream("/nexus/structures.json")){
            if(stream==null)throw new IllegalStateException("Missing installed Nexus blueprints");
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
    }
    public static Block block(String name){return BLOCKS.get(name).get();}
    public static String kind(Block block){for(var entry:MANIFEST.getAsJsonObject("structures").entrySet())if(block(entry.getValue().getAsJsonObject().get("controller").getAsString())==block)return entry.getKey();throw new IllegalArgumentException("Unknown Nexus controller");}
    public static JsonObject blueprint(String kind){return MANIFEST.getAsJsonObject("structures").getAsJsonObject(kind);}
    public static BlockPos position(JsonArray a){return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    public static ShapeTemplate shape(String kind){
        var bp=blueprint(kind);var origin=position(bp.getAsJsonArray("origin"));
        var builder=new ShapeTemplate.Builder(MachineCasings.get(id("nexus_machine")));
        for(var raw:bp.getAsJsonArray("cells")){
            var cell=raw.getAsJsonObject();var pos=position(cell.getAsJsonArray("position")).subtract(origin);if(pos.equals(BlockPos.ZERO))continue;
            var blockId=ResourceLocation.parse(cell.get("block").getAsString());
            HatchType port=switch(cell.get("part").getAsString()){case "input"->HatchTypes.ITEM_INPUT;case "output"->HatchTypes.ITEM_OUTPUT;case "fluid_in"->HatchTypes.FLUID_INPUT;case "fluid_out"->HatchTypes.FLUID_OUTPUT;case "energy"->HatchTypes.ENERGY_INPUT;default->null;};
            if(cell.get("part").getAsString().startsWith("builder_")){
                String role=cell.get("part").getAsString().substring(8);HatchType type=switch(role){case "input"->HatchTypes.ITEM_INPUT;case "output"->HatchTypes.ITEM_OUTPUT;default->HatchTypes.ENERGY_INPUT;};
                builder.add(pos.getX(),pos.getY(),pos.getZ(),new SimpleMember(){public boolean matchesState(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.block.entity.BlockEntity be){return be instanceof HatchBlockEntity h&&h.getHatchType()==type;}public net.minecraft.world.level.block.state.BlockState getPreviewState(){return BuiltInRegistries.BLOCK.get(blockId).defaultBlockState();}});continue;
            }
            if(port==null)builder.add(pos.getX(),pos.getY(),pos.getZ(),SimpleMember.forBlockId(blockId));
            else builder.add(pos.getX(),pos.getY(),pos.getZ(),new SimpleMember(){
                public boolean matchesState(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.block.entity.BlockEntity be){return false;}
                public net.minecraft.world.level.block.state.BlockState getPreviewState(){return BuiltInRegistries.BLOCK.get(blockId).defaultBlockState();}
            },new HatchFlags(Set.of(port)));
        }
        for(var raw:bp.getAsJsonArray("requiredAir")){
            var pos=position(raw.getAsJsonArray()).subtract(origin);builder.add(pos.getX(),pos.getY(),pos.getZ(),SimpleMember.forBlock(()->Blocks.AIR));
        }
        return builder.build();
    }
    public static void bootstrap(IEventBus bus){NexusMaterials.bootstrap();TYPES.register(bus);SERIALIZERS.register(bus);}
    private Nexus(){}
}
