package io.github.mattidragon.advancednetworking.graph.node.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class CapacityNode<R, V extends TransferVariant<R>> extends InterfaceNode {
    private final BlockApiLookup<Storage<V>, @Nullable Direction> lookup;

    public CapacityNode(NodeType<?> type, Graph graph, BlockApiLookup<Storage<V>, @Nullable Direction> lookup) {
        super(type, List.of(NetworkControllerContext.TYPE, ContextType.SERVER_WORLD), graph);
        this.lookup = lookup;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { DataType.NUMBER.makeRequiredOutput("capacity", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);
        var world = context.get(ContextType.SERVER_WORLD);
        var positions = findInterfaces(world, controller.graphId());

        var capacity = positions.stream()
                .map(sidePos -> {
                    var pos = sidePos.pos();
                    var side = sidePos.side();
                    return lookup.find(world, pos.offset(side), side.getOpposite());
                })
                .filter(Objects::nonNull)
                .flatMap(storage -> StreamSupport.stream(storage.spliterator(), false))
                .mapToLong(StorageView::getCapacity)
                .sum();

        return Either.left(new DataValue<?>[] { DataType.NUMBER.makeValue((double) capacity) });
    }
}
