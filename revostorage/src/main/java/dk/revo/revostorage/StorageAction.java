package dk.revo.revostorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public record StorageAction(String action,String value,boolean maximum) implements CustomPacketPayload {
 public static final Type<StorageAction> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("revostorage","action"));
 public static final StreamCodec<RegistryFriendlyByteBuf,StorageAction> CODEC=StreamCodec.of((buf,p)->{buf.writeUtf(p.action,16);buf.writeUtf(p.value,256);buf.writeBoolean(p.maximum);},buf->new StorageAction(buf.readUtf(16),buf.readUtf(256),buf.readBoolean()));
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(TYPE,CODEC,(p,ctx)->ctx.enqueueWork(()->{if(ctx.player().containerMenu instanceof StorageMenu menu&&menu.stillValid(ctx.player())){if(p.action.equals("search")){menu.search=p.value.toLowerCase(java.util.Locale.ROOT);menu.page=0;menu.broadcastChanges();}if(p.action.equals("recipe")){var id=ResourceLocation.tryParse(p.value);if(id!=null)menu.transfer(id,p.maximum);}}}));}
}
