package dk.revo.revoachievements.client;

import dk.revo.revoachievements.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WardrobeClient {
    public record PublicState(String title,int color,String hatItem) {}
    private static final Map<UUID,PublicState> visible=new HashMap<>();
    private static final Map<String,ItemStack> hats=new HashMap<>();
    public static List<Cosmetic> catalogue=List.of();
    public static Set<String> owned=Set.of();
    public static String title="",hat="",error="";
    public static AbstractClientPlayer previewEntity;
    public static ItemStack previewHat=ItemStack.EMPTY;
    public static PublicState publicState(UUID player){return visible.get(player);}
    public static void request(int action,String id){PacketDistributor.sendToServer(new WardrobeNetwork.Request(action,id));}
    public static Cosmetic entry(String id){return catalogue.stream().filter(e->e.id().toString().equals(id)).findFirst().orElse(null);}
    public static ItemStack hatFor(AbstractClientPlayer player){
        if(player==previewEntity)return previewHat;
        var state=visible.get(player.getUUID());if(state==null||state.hatItem.isEmpty())return ItemStack.EMPTY;
        return hats.computeIfAbsent(state.hatItem,id->{var key=ResourceLocation.tryParse(id);return key==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.get(key));});
    }
    private static void row(CompoundTag tag){
        if(!tag.hasUUID("Player"))return;visible.put(tag.getUUID("Player"),new PublicState(tag.getString("Title"),tag.getInt("Color"),tag.getString("HatItem")));
        var mc=Minecraft.getInstance();if(mc.level!=null){var player=mc.level.getPlayerByUUID(tag.getUUID("Player"));if(player!=null)player.refreshDisplayName();}
    }
    public static void receive(CompoundTag tag){
        var mc=Minecraft.getInstance();switch(tag.getString("Type")){
            case "own"->{
                var entries=new ArrayList<Cosmetic>();for(var t:tag.getList("Entries",Tag.TAG_COMPOUND))entries.add(Cosmetic.fromTag((CompoundTag)t));
                boolean changed=!catalogue.equals(entries);catalogue=List.copyOf(entries);
                // Dedicated-server clients do not run the server datapack listener.
                // Populate the common read-only lookup used by FTB reward labels/icons too.
                if(changed){
                    if(!mc.hasSingleplayerServer())CosmeticCatalog.replace(entries);
                    if(net.neoforged.fml.ModList.get().isLoaded("ftbquests"))FtbClientCache.refresh();
                }
                var unlocked=new HashSet<String>();for(var t:tag.getList("Owned",Tag.TAG_STRING))unlocked.add(t.getAsString());owned=Set.copyOf(unlocked);
                title=tag.getString("Title");hat=tag.getString("Hat");error=tag.getString("Error");
                if(tag.getBoolean("Open")&&!(mc.screen instanceof WardrobeScreen))mc.setScreen(new WardrobeScreen(mc.screen));
                else if(mc.screen instanceof WardrobeScreen screen)screen.refresh();
            }
            case "public"->row(tag);
            case "all"->{visible.clear();for(var t:tag.getList("Players",Tag.TAG_COMPOUND))row((CompoundTag)t);}
            case "remove"->{if(tag.hasUUID("Player"))visible.remove(tag.getUUID("Player"));}
        }
    }
    public static void clear(){visible.clear();hats.clear();catalogue=List.of();owned=Set.of();title=hat=error="";previewEntity=null;previewHat=ItemStack.EMPTY;}
}
