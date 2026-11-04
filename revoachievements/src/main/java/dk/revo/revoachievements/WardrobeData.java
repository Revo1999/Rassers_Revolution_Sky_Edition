package dk.revo.revoachievements;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** UUID-owned cosmetics live in world SavedData, independently of inventory, death and FTB progress. */
public final class WardrobeData extends SavedData {
    public static final Factory<WardrobeData> FACTORY=new Factory<>(WardrobeData::new,WardrobeData::load);
    private final Map<UUID,Profile> players=new LinkedHashMap<>();
    public static final class Profile {
        private final Set<String> unlocked=new LinkedHashSet<>();
        private String title="",hat="";
        public Set<String> unlocked() { return Set.copyOf(unlocked); }
        public String title() { return title; }
        public String hat() { return hat; }
        public boolean owns(String id) { return unlocked.contains(id); }
    }
    public static WardrobeData get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(FACTORY,"revoachievements_wardrobe"); }
    public Profile profile(UUID id) { return players.computeIfAbsent(id,key->new Profile()); }
    public boolean unlock(UUID player,String id) { boolean changed=profile(player).unlocked.add(id);if(changed)setDirty();return changed; }
    public boolean revoke(UUID player,String id) {
        var p=profile(player);boolean changed=p.unlocked.remove(id);
        if(p.title.equals(id)){p.title="";changed=true;}if(p.hat.equals(id)){p.hat="";changed=true;}
        if(changed)setDirty();return changed;
    }
    public boolean equip(UUID player,String id) {
        var entry=CosmeticCatalog.get(id);var p=profile(player);
        if(entry==null||!p.owns(id))return false;
        if(entry.kind()==Cosmetic.Kind.TITLE)p.title=id;else p.hat=id;setDirty();return true;
    }
    public void clear(UUID player,Cosmetic.Kind kind) { var p=profile(player);if(kind==Cosmetic.Kind.TITLE)p.title="";else p.hat="";setDirty(); }
    public void validate(UUID player) {
        var p=profile(player);var title=CosmeticCatalog.get(p.title);var hat=CosmeticCatalog.get(p.hat);
        if(!p.title.isEmpty()&&(title==null||title.kind()!=Cosmetic.Kind.TITLE||!p.owns(p.title)))clear(player,Cosmetic.Kind.TITLE);
        if(!p.hat.isEmpty()&&(hat==null||hat.kind()!=Cosmetic.Kind.HAT||!p.owns(p.hat)))clear(player,Cosmetic.Kind.HAT);
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {
        var list=new ListTag();players.forEach((id,p)->{
            var t=new CompoundTag();t.putUUID("Player",id);t.putString("Title",p.title);t.putString("Hat",p.hat);
            var unlocked=new ListTag();p.unlocked.forEach(s->unlocked.add(StringTag.valueOf(s)));t.put("Unlocked",unlocked);list.add(t);
        });tag.putInt("Version",1);tag.put("Players",list);return tag;
    }
    public static WardrobeData load(CompoundTag tag,HolderLookup.Provider registries) {
        var result=new WardrobeData();for(var value:tag.getList("Players",Tag.TAG_COMPOUND)){
            var t=(CompoundTag)value;if(!t.hasUUID("Player"))continue;var p=result.profile(t.getUUID("Player"));
            p.title=t.getString("Title");p.hat=t.getString("Hat");
            for(var unlocked:t.getList("Unlocked",Tag.TAG_STRING))if(net.minecraft.resources.ResourceLocation.tryParse(unlocked.getAsString())!=null)p.unlocked.add(unlocked.getAsString());
        }return result;
    }
}
