package dk.revo.revostorage;

import java.util.Locale;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record StorageAction(int containerId, String action, String value, boolean maximum) implements CustomPacketPayload {
    public static final Type<StorageAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RevoStorage.MOD_ID, "action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageAction> CODEC = StreamCodec.of((buffer, packet) -> {
        buffer.writeVarInt(packet.containerId);
        buffer.writeUtf(packet.action, 16);
        buffer.writeUtf(packet.value, 256);
        buffer.writeBoolean(packet.maximum);
    }, buffer -> new StorageAction(buffer.readVarInt(), buffer.readUtf(16), buffer.readUtf(256), buffer.readBoolean()));

    @Override
    public Type<StorageAction> type() {
        return TYPE;
    }

    public record GridClick(int containerId, int revision, int slot, int button, boolean quickMove, boolean deposit) implements CustomPacketPayload {
        private static final Type<GridClick> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RevoStorage.MOD_ID, "grid_click"));
        private static final StreamCodec<RegistryFriendlyByteBuf, GridClick> CODEC = StreamCodec.of((buffer, packet) -> {
            buffer.writeVarInt(packet.containerId);
            buffer.writeVarInt(packet.revision);
            buffer.writeVarInt(packet.slot);
            buffer.writeVarInt(packet.button);
            buffer.writeBoolean(packet.quickMove);
            buffer.writeBoolean(packet.deposit);
        }, buffer -> new GridClick(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean()));

        @Override
        public Type<GridClick> type() {
            return TYPE;
        }
    }

    private static StorageMenu activeTerminal(Player player, int containerId) {
        if (!player.isSpectator() && player.containerMenu instanceof StorageMenu menu && menu.containerId == containerId
                && menu.terminal() && menu.stillValid(player)) {
            return menu;
        }
        return null;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToServer(TYPE, CODEC, (packet, context) -> {
            var menu = activeTerminal(context.player(), packet.containerId);
            if (menu == null) return;
            switch (packet.action) {
                case "search" -> {
                    menu.search = packet.value.toLowerCase(Locale.ROOT);
                    menu.page = 0;
                    menu.broadcastChanges();
                }
                case "recipe" -> {
                    var id = ResourceLocation.tryParse(packet.value);
                    if (id != null) menu.transfer(id, packet.maximum);
                }
            }
        });
        registrar.playToServer(GridClick.TYPE, GridClick.CODEC, (packet, context) -> {
            var player = context.player();
            var menu = activeTerminal(player, packet.containerId);
            if (menu != null) menu.clickGrid(player, packet.revision, packet.slot, packet.button, packet.quickMove, packet.deposit);
        });
    }
}
