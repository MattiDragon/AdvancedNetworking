package io.github.mattidragon.advancednetworking.network.node;

import com.google.common.collect.ImmutableMap;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.SidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class InterfaceNode implements AdvancedNetworkingNode, SidedBlockNode {
    public static final Identifier ID = AdvancedNetworking.id("interface");
    public static final Map<Direction, InterfaceNode> INSTANCES = Util.<ImmutableMap.Builder<Direction, InterfaceNode>>make(ImmutableMap.builder(), (builder) -> {
        for (var dir : Direction.values()) {
            builder.put(dir, new InterfaceNode(dir));
        }
    }).build();
    private final Direction direction;

    private InterfaceNode(Direction direction) {
        this.direction = direction;
    }

    @Override
    public @NotNull Identifier getTypeId() {
        return ID;
    }

    @Override
    public @Nullable NbtElement toTag() {
        var nbt = new NbtCompound();
        nbt.putString("direction", direction.asString());
        return nbt;
    }

    @Override
    public @NotNull Collection<Node<BlockNodeHolder>> findConnections(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {
        return nodeView.getNodesAt(pos).filter(other -> canConnect(world, nodeView, pos, self, other)).toList();
    }

    @Override
    public boolean canConnect(@NotNull ServerWorld world, @NotNull NodeView nodeView, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self, @NotNull Node<BlockNodeHolder> other) {
        return other.data().getNode() instanceof CableNode && other.data().getPos().equals(pos);
    }

    @Override
    public void onConnectionsChanged(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Node<BlockNodeHolder> self) {

    }

    @Override
    public @NotNull Direction getSide() {
        return direction;
    }

    @Override
    public String toString() {
        return "InterfaceNode[%s]".formatted(direction);
    }
}
