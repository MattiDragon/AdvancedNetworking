package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.CableBlock;

public enum InterfaceType {
    INTERFACE("interface"),
    BLOCKED("blocked"),
    DEFAULT("default");

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
