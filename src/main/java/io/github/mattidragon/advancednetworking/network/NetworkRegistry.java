package io.github.mattidragon.advancednetworking.network;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.network.node.CableNode;
import io.github.mattidragon.advancednetworking.network.node.ControllerNode;
import io.github.mattidragon.advancednetworking.network.node.InterfaceNode;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.Direction;

import java.util.List;

public final class NetworkRegistry {
    private NetworkRegistry() { throw new UnsupportedOperationException(); }

    public static void register() {
        GraphLib.registerDiscoverer(((world, pos) -> {
            var state = world.getBlockState(pos);
            if (state.isOf(ModBlocks.CABLE))
                return ModBlocks.CABLE.getNodes(state);
            if (state.isOf(ModBlocks.CONTROLLER))
                return List.of(ControllerNode.INSTANCE);
            return List.of();
        }));
        Registry.register(GraphLib.BLOCK_NODE_DECODER, CableNode.ID, tag -> CableNode.INSTANCE);
        Registry.register(GraphLib.BLOCK_NODE_DECODER, ControllerNode.ID, tag -> ControllerNode.INSTANCE);
        Registry.register(GraphLib.BLOCK_NODE_DECODER, InterfaceNode.ID, tag -> InterfaceNode.INSTANCES.get(Direction.byName(((NbtCompound)tag).getString("direction"))));
    }
}
