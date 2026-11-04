package dk.revo.revolib.quest;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dk.revo.revolib.RevoLib;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Freely replaceable manuals are information, not a renewable crafting/recycling ingredient. */
@EventBusSubscriber(modid=RevoLib.MOD_ID)
public final class FreeManualRecipes {
    private static final TagKey<Item> MANUALS=TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(RevoLib.MOD_ID,"free_quest_manuals"));
    private static final Set<RecipeManager> FILTERED=Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<String> OUTPUT_FIELDS=Set.of("result","results","output","outputs",
        "item_outputs","fluid_outputs","remainder","remainders","neoforge:conditions","type","group","category");

    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void beforeRecipeSync(OnDatapackSyncEvent event){
        var server=event.getPlayerList().getServer();
        var manager=server.getRecipeManager();
        // A null player means a datapack reload; always reapply then. Initial login filters once.
        if(event.getPlayer()!=null&&FILTERED.contains(manager))return;
        filter(server);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void serverStarted(ServerStartedEvent event){
        filter(event.getServer());
    }

    private static void filter(MinecraftServer server){
        var manager=server.getRecipeManager();
        var ops=RegistryOps.create(JsonOps.INSTANCE,server.registryAccess());
        List<RecipeHolder<?>> retained=new ArrayList<>();
        List<ResourceLocation> removed=new ArrayList<>();
        for(var holder:manager.getRecipes()){
            var recipe=holder.value();
            boolean consumesManual=recipe.getIngredients().stream().anyMatch(ingredient->
                Arrays.stream(ingredient.getItems()).anyMatch(FreeManualRecipes::protectedStack));
            // Machine serializers may keep their input lists outside Recipe#getIngredients.
            if(!consumesManual){
                try {
                    var encoded=Recipe.CODEC.encodeStart(ops,recipe);
                    consumesManual=encoded.result().map(FreeManualRecipes::protectedInput).orElse(false);
                } catch(RuntimeException exception) {
                    // A non-serializable recipe still receives the normal Ingredient check above.
                    LogUtils.getLogger().debug("Recipe {} cannot expose additional serialized inputs",holder.id(),exception);
                }
            }
            if(consumesManual)removed.add(holder.id());else retained.add(holder);
        }
        if(!removed.isEmpty())manager.replaceRecipes(retained);
        FILTERED.add(manager);
        LogUtils.getLogger().info("Revo free-manual protection: removed {} input/recycling recipes: {}",removed.size(),removed);
    }

    private static boolean protectedStack(ItemStack stack){return !stack.isEmpty()&&stack.is(MANUALS);}
    private static boolean protectedId(String value){
        var id=ResourceLocation.tryParse(value);
        return id!=null&&BuiltInRegistries.ITEM.getHolder(id).map(holder->holder.is(MANUALS)).orElse(false);
    }
    private static boolean protectedInput(JsonElement element){
        if(element.isJsonArray()){
            for(var child:element.getAsJsonArray())if(protectedInput(child))return true;
        }else if(element.isJsonObject()){
            var object=element.getAsJsonObject();
            if(object.has("tag")&&object.get("tag").isJsonPrimitive()){
                var id=ResourceLocation.tryParse(object.get("tag").getAsString());
                if(id!=null&&BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM,id))
                    .map(holders->holders.stream().anyMatch(holder->holder.is(MANUALS))).orElse(false))return true;
            }
            for(var entry:object.entrySet())
                if(!OUTPUT_FIELDS.contains(entry.getKey())&&protectedInput(entry.getValue()))return true;
        }else if(element.isJsonPrimitive()&&element.getAsJsonPrimitive().isString()){
            return protectedId(element.getAsString());
        }
        return false;
    }
}
