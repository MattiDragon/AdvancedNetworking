package io.github.mattidragon.advancednetworking.network.node;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ControllerNode implements BlockNode, AdvancedNetworkingNode {
    public static final Identifier ID = AdvancedNetworking.id("controller");
    public static final ControllerNode INSTANCE = new ControllerNode();

    private ControllerNode() {}

    @Override
    public @NotNull Identifier getTypeId() {
        return ID;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public @NotNull Collection<Node<BlockNodeHolder>> findConnections(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {
        var list = new ArrayList<Node<BlockNodeHolder>>();

        for (var dir : Direction.values()) {
            nodeView.getNodesAt(pos.offset(dir)).filter(node -> node.data().getNode() instanceof CableNode || node.data().getNode() instanceof ControllerNode)
                    .forEach(list::add);
        }
        return list;
    }

    @Override
    public boolean canConnect(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self, @NotNull Node<BlockNodeHolder> other) {
        var node = other.data().getNode();

        return node instanceof CableNode || node instanceof ControllerNode;
    }

    @Override
    public void onConnectionsChanged(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {

    }

    @Override
    public String toString() {
        return "ControllerNode[]";
    }
}
