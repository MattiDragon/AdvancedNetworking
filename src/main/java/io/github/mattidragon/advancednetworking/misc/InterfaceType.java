package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum InterfaceType {
    INTERFACE("interface"),
    BLOCKED("blocked"),
    DEFAULT("default");

    public static final PacketCodec<ByteBuf, InterfaceType> PACKET_CODEC = PacketCodecs.indexed(ValueLists.createIndexToValueFunction(
            InterfaceType::ordinal, values(), ValueLists.OutOfBoundsHandling.WRAP
    ), InterfaceType::ordinal);

    public final String id;

    InterfaceType(String id) {
        this.id = id;
    }

    public static InterfaceType ofConnectionType(CableBlock.ConnectionType connectionType) {
        return switch (connectionType) {
            case NONE, CONNECTED -> InterfaceType.DEFAULT;
            case DISABLED -> InterfaceType.BLOCKED;
            case INTERFACE, INTERFACE_POWERED -> InterfaceType.INTERFACE;
        };
    }
}
