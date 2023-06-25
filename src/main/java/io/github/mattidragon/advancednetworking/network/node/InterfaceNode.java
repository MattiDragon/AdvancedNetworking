package io.github.mattidragon.advancednetworking.network.node;

import com.google.common.collect.ImmutableMap;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.wire.SidedFaceBlockNode;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InterfaceNode implements SidedFaceBlockNode {
    public static final Identifier ID = AdvancedNetworking.id("interface");
    public static final Map<Direction, InterfaceNode> INSTANCES = Util.<ImmutableMap.Builder<Direction, InterfaceNode>>make(ImmutableMap.builder(), (builder) -> {
        for (var dir : Direction.values()) {
            builder.put(dir, new InterfaceNode(dir));
        }
    }).build();
    public static final BlockNodeType TYPE = BlockNodeType.of(ID,
            tag -> tag == null ? null : InterfaceNode.INSTANCES.get(Direction.byName(((NbtCompound)tag).getString("direction"))),
            (buf, ctx) -> Objects.requireNonNull(InterfaceNode.INSTANCES.get(buf.readEnumConstant(Direction.class))));
    private final Direction direction;

    private InterfaceNode(Direction direction) {
        this.direction = direction;
    }

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        var nbt = new NbtCompound();
        nbt.putString("direction", direction.asString());
        return nbt;
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
