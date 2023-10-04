package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class RequestInterfacesPacket {
    public static final Identifier ID = id("request_interfaces");
    public static final Identifier RESPONSE_ID = id("request_interfaces_response");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            var syncId = buf.readByte();

            server.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId && player.currentScreenHandler instanceof ControllerScreenHandler controllerScreenHandler) {
                    var interfaces = controllerScreenHandler.getInterfaces();
                    if (interfaces.isEmpty()) {
                        AdvancedNetworking.LOGGER.warn("Failed to get interfaces for client, sending empty map");
                    }
                    var map = interfaces.orElseGet(Map::of);
                    respond(map, syncId, responseSender);
                }
            });
        });
    }

    private static void respond(Map<String, String> interfaces, int syncId, PacketSender responseSender) {
        var buf = PacketByteBufs.create();
        buf.writeByte(syncId);
        buf.writeMap(interfaces, PacketByteBuf::writeString, PacketByteBuf::writeString);

        responseSender.sendPacket(RESPONSE_ID, buf);
    }

}