package io.github.mattidragon.advancednetworking.graph.node.base;

import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.SidedPos;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public abstract class InterfaceNode extends Node {
    public String interfaceId = "";

    protected InterfaceNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    protected final Optional<SidedPos> findInterface(ServerWorld world, long graphId) {
        var graph = NetworkRegistry.UNIVERSE.getServerGraphWorld(world).getGraph(graphId);
        if (graph == null)
            return Optional.empty();

        return graph.getNodes()
                .filter(node -> node.getNode() instanceof SidedBlockNode)
                .map(node -> new SidedPos(node.getBlockPos(), node.cast(SidedBlockNode.class).getNode().getSide()))
                .filter(pos -> interfaceId.equals(CableBlock.calcInterfaceId(pos.pos(), pos.side())))
                .findFirst();
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        interfaceId = data.getString("interfaceId");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("interfaceId", interfaceId);
    }

    @Override
    public List<Text> validate() {
        return interfaceId.length() == 12 ? List.of() : List.of(Text.translatable("node.advanced_networking.interface.invalid"));
    }
}
