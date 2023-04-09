package io.github.mattidragon.advancednetworking.graph.node.item.info;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.text.Text;

import java.util.List;

public class ItemCapacityNode extends InterfaceNode {
    public ItemCapacityNode(Graph graph) {
        super(ModNodeTypes.ITEM_CAPACITY, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
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
        var optionalPos = findInterface(world, controller.graphId());
        if (optionalPos.isEmpty())
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var storage = ItemStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
        if (storage == null)
            return Either.right(Text.translatable("node.advanced_networking.item_source.missing", interfaceId));

        var capacity = 0L;
        for (var view : storage) {
            capacity += view.getCapacity();
        }

        return Either.left(new DataValue<?>[] { DataType.NUMBER.makeValue((double) capacity) });
    }
}
