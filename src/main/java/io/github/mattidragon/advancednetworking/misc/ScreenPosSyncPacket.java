package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public record ScreenPosSyncPacket(int syncId, double x, double y, int zoom) implements FabricPacket {
    private static final Identifier ID = id("pos_sync");
    private static final PacketType<ScreenPosSyncPacket> TYPE = PacketType.create(ID, ScreenPosSyncPacket::new);

    public ScreenPosSyncPacket(PacketByteBuf buf) {
        this(buf.readByte(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (packet, player, responseSender) -> {
            if (player.currentScreenHandler.syncId == packet.syncId && player.currentScreenHandler instanceof ControllerScreenHandler networking) {
                networking.viewX = packet.x;
                networking.viewY = packet.y;
                networking.zoom = packet.zoom;
            }
        });
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeInt(zoom);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}