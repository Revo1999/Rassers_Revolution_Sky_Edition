package dk.revo.revoachievements;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;

/** Pack authors extend/override data/<namespace>/revo_cosmetics/<id>.json. */
public final class CosmeticCatalog extends SimpleJsonResourceReloadListener {
    private static volatile Map<ResourceLocation,Cosmetic> entries=Map.of();
    public CosmeticCatalog() { super(new Gson(),"revo_cosmetics"); }
    public static Cosmetic get(String id) { var key=ResourceLocation.tryParse(id);return key==null?null:entries.get(key); }
    public static List<Cosmetic> all() { return entries.values().stream().sorted(Comparator.comparingInt(Cosmetic::order).thenComparing(e->e.id().toString())).toList(); }
    public static void replace(Collection<Cosmetic> values) { var map=new LinkedHashMap<ResourceLocation,Cosmetic>();values.forEach(v->map.put(v.id(),v));entries=Map.copyOf(map); }
    @Override protected void apply(Map<ResourceLocation,JsonElement> files,ResourceManager manager,ProfilerFiller profiler) {
        var parsed=new ArrayList<Cosmetic>();
        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(file->{
            try {
                if(parsed.size()>=512)throw new IllegalArgumentException("Cosmetic catalogue limit is 512");
                var j=file.getValue().getAsJsonObject();
                if(j.has("requires_mod")&&!net.neoforged.fml.ModList.get().isLoaded(j.get("requires_mod").getAsString()))return;
                var kind=Cosmetic.Kind.valueOf(text(j,"kind",16).toUpperCase(Locale.ROOT));
                var item=ResourceLocation.parse(text(j,"item",128));
                if(!BuiltInRegistries.ITEM.containsKey(item)||BuiltInRegistries.ITEM.get(item)==net.minecraft.world.item.Items.AIR)throw new IllegalArgumentException("Missing cosmetic icon/hat item "+item);
                int color=Integer.parseInt(text(j,"color",7).replace("#",""),16);
                parsed.add(new Cosmetic(file.getKey(),kind,text(j,"name",64),text(j,"description",256),text(j,"unlock_hint",256),item,color&0xFFFFFF,j.has("order")?j.get("order").getAsInt():0));
            } catch(Exception e) { RevoAchievements.LOGGER.warn("Skipping invalid Revo cosmetic {}: {}",file.getKey(),e.getMessage()); }
        });
        replace(parsed);RevoAchievements.LOGGER.info("Loaded {} Revo cosmetics",entries.size());
    }
    private static String text(JsonObject j,String key,int max) {
        String s=j.get(key).getAsString();if(s.isBlank()||s.length()>max||s.chars().anyMatch(c->c<32||c=='§'))throw new IllegalArgumentException("Invalid "+key);return s;
    }
}
