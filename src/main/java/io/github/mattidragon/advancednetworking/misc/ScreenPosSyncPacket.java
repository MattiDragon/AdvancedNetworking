package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class ScreenPosSyncPacket {
    private static final Identifier ID = id("pos_sync");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, ((server, player, handler, buf, responseSender) -> {
            var x = buf.readDouble();
            var y = buf.readDouble();
            var zoom = buf.readInt();
            var syncId = buf.readByte();

            server.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId && player.currentScreenHandler instanceof ControllerScreenHandler networking) {
                    networking.viewX = x;
                    networking.viewY = y;
                    networking.zoom = zoom;
                }
            });
        }));
    }

    public static void send(int syncId, double x, double y, int zoom) {
        var buf = PacketByteBufs.create();
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeInt(zoom);
        buf.writeByte(syncId);

        ClientPlayNetworking.send(ID, buf);
    }
}