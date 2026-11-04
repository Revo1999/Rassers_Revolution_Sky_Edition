package dk.revo.revoachievements;

import java.util.UUID;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WardrobeService {
    private WardrobeService() {}
    /** -1 invalid definition, 0 already owned, 1 newly unlocked. No auto-equipping or items. */
    public static int grant(MinecraftServer server,UUID player,String id,boolean notify) {
        var entry=CosmeticCatalog.get(id);if(entry==null)return -1;
        if(!WardrobeData.get(server).unlock(player,id))return 0;
        changed(server,player);
        var online=server.getPlayerList().getPlayer(player);
        if(notify&&online!=null)online.sendSystemMessage(Component.literal("Unlocked: ").append(Component.literal(entry.name()).withColor(entry.color())).append(" — Revo Wardrobe [K]").withStyle(s->s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/revoachievements wardrobe")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Choose your title and hat")))));
        return 1;
    }
    public static boolean equip(ServerPlayer player,String id) {
        if(!WardrobeData.get(player.getServer()).equip(player.getUUID(),id))return false;
        changed(player.getServer(),player.getUUID());return true;
    }
    public static void clear(ServerPlayer player,Cosmetic.Kind kind) { WardrobeData.get(player.getServer()).clear(player.getUUID(),kind);changed(player.getServer(),player.getUUID()); }
    public static void changed(MinecraftServer server,UUID player) {
        var online=server.getPlayerList().getPlayer(player);if(online==null)return;
        online.refreshDisplayName();online.refreshTabListName();
        if(ModList.get().isLoaded("playernamestyler"))dk.revo.revoachievements.compat.TitleStyler.refresh(online);
        sendOwn(online,false,"");broadcast(online);
    }
    public static Cosmetic title(ServerPlayer player) { return CosmeticCatalog.get(WardrobeData.get(player.getServer()).profile(player.getUUID()).title()); }
    public static CompoundTag own(ServerPlayer player,boolean open,String error) {
        var p=WardrobeData.get(player.getServer()).profile(player.getUUID());var t=new CompoundTag();
        t.putString("Type","own");t.putBoolean("Open",open);t.putString("Title",p.title());t.putString("Hat",p.hat());t.putString("Error",error);
        var owned=new ListTag();p.unlocked().stream().sorted().forEach(id->owned.add(StringTag.valueOf(id)));t.put("Owned",owned);
        var entries=new ListTag();CosmeticCatalog.all().forEach(e->entries.add(e.toTag()));t.put("Entries",entries);return t;
    }
    public static CompoundTag publicRow(ServerPlayer player) {
        var p=WardrobeData.get(player.getServer()).profile(player.getUUID());var title=CosmeticCatalog.get(p.title());var hat=CosmeticCatalog.get(p.hat());
        var t=new CompoundTag();t.putUUID("Player",player.getUUID());t.putString("Title",title==null?"":title.name());t.putInt("Color",title==null?0xFFFFFF:title.color());t.putString("HatItem",hat==null?"":hat.item().toString());return t;
    }
    public static void sendOwn(ServerPlayer player,boolean open,String error) { PacketDistributor.sendToPlayer(player,new WardrobeNetwork.Sync(own(player,open,error))); }
    public static void broadcast(ServerPlayer player) { var t=publicRow(player);t.putString("Type","public");PacketDistributor.sendToAllPlayers(new WardrobeNetwork.Sync(t)); }
    public static void sendPublic(ServerPlayer player) {
        var t=new CompoundTag();t.putString("Type","all");var list=new ListTag();player.getServer().getPlayerList().getPlayers().forEach(p->list.add(publicRow(p)));t.put("Players",list);PacketDistributor.sendToPlayer(player,new WardrobeNetwork.Sync(t));
    }
}
