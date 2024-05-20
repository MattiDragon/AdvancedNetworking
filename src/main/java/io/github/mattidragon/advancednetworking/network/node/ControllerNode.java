package io.github.mattidragon.advancednetworking.network.node;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.wire.FullWireBlockNode;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ControllerNode implements FullWireBlockNode {
    public static final Identifier ID = AdvancedNetworking.id("controller");
    public static final ControllerNode INSTANCE = new ControllerNode();
    public static final BlockNodeType TYPE = BlockNodeType.of(ID, () -> INSTANCE);

    private ControllerNode() {}

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {
    }

    @Override
    public String toString() {
        return "ControllerNode[]";
    }
}
