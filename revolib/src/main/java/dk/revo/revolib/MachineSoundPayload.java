package dk.revo.revolib;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record MachineSoundPayload(BlockPos pos,int voice) implements CustomPacketPayload {
    public static final Type<MachineSoundPayload> TYPE=new Type<>(ResourceLocation.parse("revolib:machine_sound"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MachineSoundPayload> CODEC=StreamCodec.of(
        (buf,p)->{buf.writeBlockPos(p.pos);buf.writeVarInt(p.voice);},buf->new MachineSoundPayload(buf.readBlockPos(),buf.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToClient(TYPE,CODEC,(p,ctx)->ctx.enqueueWork(()->dk.revo.revolib.client.MachineAudio.receive(p)));
    }
}
