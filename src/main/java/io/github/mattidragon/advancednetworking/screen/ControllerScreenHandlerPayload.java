package io.github.mattidragon.advancednetworking.screen;

import io.github.mattidragon.nodeflow.graph.Graph;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.List;

public record ControllerScreenHandlerPayload(Graph graph,
                                             boolean adventureModeAccessAllowed,
                                             double viewX,
                                             double viewY,
                                             int zoom,
                                             List<Text> errors) {
    public static final PacketCodec<RegistryByteBuf, ControllerScreenHandlerPayload> CODEC = PacketCodec.tuple(
            Graph.PACKET_CODEC, ControllerScreenHandlerPayload::graph,
            PacketCodecs.BOOL, ControllerScreenHandlerPayload::adventureModeAccessAllowed,
            PacketCodecs.DOUBLE, ControllerScreenHandlerPayload::viewX,
            PacketCodecs.DOUBLE, ControllerScreenHandlerPayload::viewY,
            PacketCodecs.VAR_INT, ControllerScreenHandlerPayload::zoom,
            TextCodecs.PACKET_CODEC.collect(PacketCodecs.toList()), ControllerScreenHandlerPayload::errors,
            ControllerScreenHandlerPayload::new
    );
}
