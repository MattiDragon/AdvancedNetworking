package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ScreenPosSyncPacket(int syncId, double x, double y, int zoom) implements CustomPayload {
    private static final Id<ScreenPosSyncPacket> ID = new Id<>(AdvancedNetworking.id("pos_sync"));
    public static final PacketCodec<PacketByteBuf, ScreenPosSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ScreenPosSyncPacket::syncId,
            PacketCodecs.DOUBLE, ScreenPosSyncPacket::x,
            PacketCodecs.DOUBLE, ScreenPosSyncPacket::y,
            PacketCodecs.VAR_INT, ScreenPosSyncPacket::zoom,
            ScreenPosSyncPacket::new
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            var currentScreenHandler = context.player().currentScreenHandler;
            if (currentScreenHandler.syncId == packet.syncId && currentScreenHandler instanceof ControllerScreenHandler networking) {
                networking.viewX = packet.x;
                networking.viewY = packet.y;
                networking.zoom = packet.zoom;
            }
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}