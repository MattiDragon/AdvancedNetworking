package io.github.mattidragon.advancednetworking.network.node;

import com.google.common.collect.ImmutableMap;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.HalfLink;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.wire.SidedFaceBlockNode;
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InterfaceNode implements SidedFaceBlockNode, AdvancedNetworkingNode {
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
    public @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
        return WireConnectionDiscoverers.sidedFaceFindConnections(this, self);
    }

    @Override
    public boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return WireConnectionDiscoverers.sidedFaceCanConnect(this, self, other);
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {

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
