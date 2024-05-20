package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RequestInterfacesPayload(int syncId) implements CustomPayload {
    public static final Id<RequestInterfacesPayload> ID = new Id<>(AdvancedNetworking.id("request_interfaces"));
    public static final PacketCodec<PacketByteBuf, RequestInterfacesPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, RequestInterfacesPayload::syncId, RequestInterfacesPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        PayloadTypeRegistry.playS2C().register(Response.ID, Response.CODEC);
        
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            var syncId = packet.syncId();
            var screenHandler = context.player().currentScreenHandler; 
            
            if (screenHandler.syncId == syncId && screenHandler instanceof ControllerScreenHandler controllerScreenHandler) {
                var interfaces = controllerScreenHandler.getInterfaces();
                if (interfaces.isEmpty()) {
                    AdvancedNetworking.LOGGER.warn("Failed to get interfaces for client, sending empty map");
                    context.responseSender().sendPacket(new Response(Map.of(), Map.of(), syncId));
                    return;
                }
                var pair = interfaces.get();
                Map<String, Text> interfaces1 = pair.getLeft();
                Map<String, List<String>> groups = pair.getRight();
                context.responseSender().sendPacket(new Response(interfaces1, groups, syncId));
            }
        });
    }

    public record Response(Map<String, Text> interfaces, Map<String, List<String>> groups, int syncId) implements CustomPayload {
        public static final Id<Response> ID = new Id<>(AdvancedNetworking.id("request_interfaces_response"));
        public static final PacketCodec<PacketByteBuf, Response> CODEC = PacketCodec.tuple(
                PacketCodecs.map(HashMap::new, PacketCodecs.STRING, TextCodecs.PACKET_CODEC), Response::interfaces,
                PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.STRING.collect(PacketCodecs.toList())), Response::groups,
                PacketCodecs.VAR_INT, Response::syncId,
                Response::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}