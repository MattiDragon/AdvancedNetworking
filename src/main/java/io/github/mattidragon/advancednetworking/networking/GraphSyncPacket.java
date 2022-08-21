package io.github.mattidragon.advancednetworking.networking;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.ui.screen.handler.NetworkingScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class GraphSyncPacket {
    public static final Identifier GRAPH_SYNC_ID = AdvancedNetworking.id("graph_sync");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(GRAPH_SYNC_ID, ((server, player, handler, buf, responseSender) -> {
            var nbt = buf.readNbt();
            var syncId = buf.readByte();

            if (nbt == null) return;

            server.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId && player.currentScreenHandler instanceof NetworkingScreenHandler networking) {
                    networking.graph.readNbt(nbt);
                }
            });
        }));
    }

    public static void send(int syncId, Graph graph) {
        var buf = PacketByteBufs.create();
        var nbt = new NbtCompound();
        graph.writeNbt(nbt);
        buf.writeNbt(nbt);
        buf.writeByte(syncId);

        ClientPlayNetworking.send(GRAPH_SYNC_ID, buf);
    }
}
