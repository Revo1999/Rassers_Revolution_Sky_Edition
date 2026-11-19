package dk.revo.revoutil;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class PlaytestStatus {
    private final Set<UUID> viewers = new HashSet<>();

    int toggle(CommandSourceStack source) {
        var player = source.getPlayer();
        if (player == null) {
            var text = String.format(Locale.ROOT, "Revo %s | Server %.1f ms/tick",
                    RevoUtil.PACK_VERSION, millisecondsPerTick(source.getServer()));
            source.sendSuccess(() -> Component.literal(text), false);
            return 1;
        }

        boolean enabled = viewers.add(player.getUUID());
        if (!enabled) {
            viewers.remove(player.getUUID());
        }
        PacketDistributor.sendToPlayer(player, new Update(enabled, millisecondsPerTick(source.getServer())));
        source.sendSuccess(() -> Component.literal(enabled ? "Revo status HUD on." : "Revo status HUD off."), false);
        return 1;
    }

    void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (viewers.isEmpty() || server.getTickCount() % 20 != 0) {
            return;
        }
        var update = new Update(true, millisecondsPerTick(server));
        for (UUID viewer : viewers) {
            var player = server.getPlayerList().getPlayer(viewer);
            if (player != null) {
                PacketDistributor.sendToPlayer(player, update);
            }
        }
    }

    void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        viewers.remove(event.getEntity().getUUID());
    }

    void stopped(ServerStoppedEvent event) {
        viewers.clear();
    }

    private static double millisecondsPerTick(MinecraftServer server) {
        return server.getAverageTickTimeNanos() / 1_000_000.0;
    }

    static void registerPayload(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Update.TYPE, Update.CODEC,
                (message, context) -> context.enqueueWork(() -> dk.revo.revoutil.client.StatusHud.receive(message)));
    }

    public record Update(boolean enabled, double millisecondsPerTick) implements CustomPacketPayload {
        static final Type<Update> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RevoUtil.MOD_ID, "status"));
        static final StreamCodec<RegistryFriendlyByteBuf, Update> CODEC = StreamCodec.of(
                (buffer, update) -> {
                    buffer.writeBoolean(update.enabled());
                    buffer.writeDouble(update.millisecondsPerTick());
                },
                buffer -> new Update(buffer.readBoolean(), buffer.readDouble()));

        @Override
        public Type<Update> type() {
            return TYPE;
        }
    }
}
