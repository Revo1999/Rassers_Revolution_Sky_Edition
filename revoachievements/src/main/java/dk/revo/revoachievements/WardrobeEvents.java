package dk.revo.revoachievements;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WardrobeEvents {
    @SubscribeEvent public void reload(AddReloadListenerEvent e){e.addListener(new CosmeticCatalog());}
    @SubscribeEvent public void commands(RegisterCommandsEvent e){WardrobeCommands.register(e.getDispatcher());}
    @SubscribeEvent public void join(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity() instanceof ServerPlayer player){WardrobeData.get(player.getServer()).validate(player.getUUID());WardrobeService.sendOwn(player,false,"");WardrobeService.sendPublic(player);WardrobeService.changed(player.getServer(),player.getUUID());}
    }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)WardrobeService.changed(p.getServer(),p.getUUID());}
    @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)WardrobeService.changed(p.getServer(),p.getUUID());}
    @SubscribeEvent public void leave(PlayerEvent.PlayerLoggedOutEvent e){
        WardrobeNetwork.forget(e.getEntity().getUUID());var t=new CompoundTag();t.putString("Type","remove");t.putUUID("Player",e.getEntity().getUUID());PacketDistributor.sendToAllPlayers(new WardrobeNetwork.Sync(t));
    }
    @SubscribeEvent public void stop(ServerStoppingEvent e){WardrobeNetwork.reset();}
    @SubscribeEvent public void data(OnDatapackSyncEvent e){
        if(e.getPlayer()!=null)return;
        e.getPlayerList().getPlayers().forEach(p->{WardrobeData.get(p.getServer()).validate(p.getUUID());WardrobeService.changed(p.getServer(),p.getUUID());});
    }
    @SubscribeEvent public void name(PlayerEvent.NameFormat e){
        if(ModList.get().isLoaded("playernamestyler"))return;
        if(e.getEntity() instanceof ServerPlayer p){var t=WardrobeService.title(p);if(t!=null)e.setDisplayname(Component.literal("["+t.name()+"] ").withColor(t.color()).append(e.getDisplayname()));}
        else if(e.getEntity().level().isClientSide){var t=dk.revo.revoachievements.client.WardrobeClient.publicState(e.getEntity().getUUID());if(t!=null&&!t.title().isEmpty())e.setDisplayname(Component.literal("["+t.title()+"] ").withColor(t.color()).append(e.getDisplayname()));}
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public void tab(PlayerEvent.TabListNameFormat e){
        if(e.getEntity() instanceof ServerPlayer p){
            if(ModList.get().isLoaded("playernamestyler"))e.setDisplayName(dk.revo.revoachievements.compat.TitleStyler.tab(p));
            else {var t=WardrobeService.title(p);if(t!=null)e.setDisplayName(Component.literal("["+t.name()+"] ").withColor(t.color()).append(p.getGameProfile().getName()));}
        }
    }
}
