package io.github.mattidragon.advancednetworking.network;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import io.github.mattidragon.advancednetworking.network.node.CableNode;
import io.github.mattidragon.advancednetworking.network.node.ControllerNode;
import io.github.mattidragon.advancednetworking.network.node.InterfaceNode;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;

import java.util.List;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public final class NetworkRegistry {
    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("networks"));

    private NetworkRegistry() { throw new UnsupportedOperationException(); }

    public static void register() {
        UNIVERSE.register();
        UNIVERSE.addDiscoverer(((world, pos) -> {
            var state = world.getBlockState(pos);
            if (state.isOf(ModBlocks.CABLE))
                return ModBlocks.CABLE.getNodes(state);
            if (state.isOf(ModBlocks.CONTROLLER))
                return List.of(ControllerNode.INSTANCE);
            return List.of();
        }));
        UNIVERSE.addNodeTypes(CableNode.TYPE, ControllerNode.TYPE, InterfaceNode.TYPE);
    }
}
