package io.github.mattidragon.advancednetworking.network.node;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class CableNode implements AdvancedNetworkingNode, BlockNode {
    public static final Identifier ID = AdvancedNetworking.id("cable");
    public static final CableNode INSTANCE = new CableNode();

    private CableNode() {}

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
        var list = nodeView.getNodesAt(pos).filter(node -> node.data().getNode() instanceof InterfaceNode).collect(Collectors.toCollection(ArrayList::new));

        for (var dir : Direction.values()) {
            nodeView.getNodesAt(pos.offset(dir)).filter(node -> node.data().getNode() instanceof CableNode || node.data().getNode() instanceof ControllerNode)
                    .forEach(list::add);
        }
        return list;
    }

    @Override
    public boolean canConnect(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self, @NotNull Node<BlockNodeHolder> other) {
        if (!world.getBlockState(pos).isOf(ModBlocks.CABLE)) return false;

        var node = other.data().getNode();
        if (node instanceof InterfaceNode)
            return pos.equals(other.data().getPos());

        if (node instanceof CableNode || node instanceof ControllerNode)
            return world.getBlockState(pos).get(CableBlock.FACING_PROPERTIES.get(Direction.fromVector(other.data().getPos().subtract(pos)))) == CableBlock.ConnectionType.CONNECTED;
        return false;
    }

    @Override
    public void onConnectionsChanged(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {

    }

    @Override
    public String toString() {
        return "CableNode[]";
    }

    @Override
    public int hashCode() {
        return 1125412074;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CableNode;
    }
}
