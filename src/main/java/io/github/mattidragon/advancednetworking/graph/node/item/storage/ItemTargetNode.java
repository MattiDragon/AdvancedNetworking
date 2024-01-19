package io.github.mattidragon.advancednetworking.graph.node.item.storage;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.advancednetworking.graph.node.base.TransferNodeUtils;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.text.Text;

import java.util.List;

public class ItemTargetNode extends InterfaceNode {
    public ItemTargetNode(Graph graph) {
        super(ModNodeTypes.ITEM_TARGET, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[0];
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredInput("items", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);
        var world = context.get(ContextType.SERVER_WORLD);
        var positions = findInterfaces(world, controller.graphId());

        var storage = TransferNodeUtils.buildCombinedStorage(positions, world, ItemStorage.SIDED);

        var stream = inputs[0].getAs(ModDataTypes.ITEM_STREAM);
        stream.end(storage, controller.controller().itemEnvironment);
        return Either.left(new DataValue<?>[0]);
    }
}
