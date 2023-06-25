package io.github.mattidragon.advancednetworking.network.node;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.HalfLink;
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode;
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableNode implements CenterWireBlockNode {
    public static final Identifier ID = AdvancedNetworking.id("cable");
    public static final CableNode INSTANCE = new CableNode();
    public static final BlockNodeType TYPE = BlockNodeType.of(ID, () -> INSTANCE);

    private CableNode() {}

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return (other.other().getNode() instanceof InterfaceNode && other.other().getPos().equals(self.getPos())) || WireConnectionDiscoverers.centerWireCanConnect(this, self, other);
    }

    @Override
    public boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull Direction onSide, @NotNull HalfLink link) {
        var world = self.getBlockWorld();
        var pos = self.getBlockPos();
        var posDiff = link.other().getBlockPos().subtract(pos);
        if (posDiff.equals(BlockPos.ORIGIN))
            return true; // Connections to interface nodes are always valid

        // We only connect to cables if the block state is connected
        return world.getBlockState(pos).get(CableBlock.FACING_PROPERTIES.get(Direction.fromVector(posDiff.getX(), posDiff.getY(), posDiff.getZ()))) == CableBlock.ConnectionType.CONNECTED;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {

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
