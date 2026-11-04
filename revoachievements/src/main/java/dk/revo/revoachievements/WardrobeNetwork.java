package dk.revo.revoachievements;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class WardrobeNetwork {
    private static final Map<UUID,Long> lastRequest=new HashMap<>();
    public record Request(int action,String id) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(RevoAchievements.MOD_ID,"wardrobe_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.action);b.writeUtf(p.id,128);},b->new Request(b.readVarInt(),b.readUtf(128)));
        public Type<Request> type(){return TYPE;}
    }
    public record Sync(CompoundTag data) implements CustomPacketPayload {
        public static final Type<Sync> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(RevoAchievements.MOD_ID,"wardrobe_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Sync> CODEC=StreamCodec.of((b,p)->b.writeNbt(p.data),b->new Sync(Objects.requireNonNull(b.readNbt())));
        public Type<Sync> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar=event.registrar("1");
        registrar.playToServer(Request.TYPE,Request.CODEC,(message,context)->context.enqueueWork(()->{
            if(context.player() instanceof ServerPlayer player)handle(player,message);
        }));
        registrar.playToClient(Sync.TYPE,Sync.CODEC,(message,context)->context.enqueueWork(()->dk.revo.revoachievements.client.WardrobeClient.receive(message.data)));
    }
    public static void handle(ServerPlayer player,Request request) {
        long now=player.serverLevel().getGameTime();Long previous=lastRequest.put(player.getUUID(),now);
        if(previous!=null&&now-previous<2)return;
        switch(request.action) {
            case 0->WardrobeService.sendOwn(player,true,"");
            case 1->{if(!WardrobeService.equip(player,request.id))WardrobeService.sendOwn(player,false,"This cosmetic is locked or unavailable.");}
            case 2->WardrobeService.clear(player,Cosmetic.Kind.TITLE);
            case 3->WardrobeService.clear(player,Cosmetic.Kind.HAT);
            default->WardrobeService.sendOwn(player,false,"Unknown wardrobe action.");
        }
    }
    public static void forget(UUID id){lastRequest.remove(id);}
    public static void reset(){lastRequest.clear();}
}
