package io.github.mattidragon.advancednetworking.graph;

import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.nodeflow.graph.context.ContextType;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public record NetworkControllerContext(ControllerBlockEntity controller, long graphId) {
    public static final ContextType<NetworkControllerContext> TYPE = new ContextType<>(NetworkControllerContext.class);

    public static void register() {
        ContextType.register(TYPE, id("network_controller"));
    }
}
