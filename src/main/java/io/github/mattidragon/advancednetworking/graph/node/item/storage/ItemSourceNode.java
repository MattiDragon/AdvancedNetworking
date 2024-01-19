package io.github.mattidragon.advancednetworking.graph.node.item.storage;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.advancednetworking.graph.node.base.TransferNodeUtils;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.text.Text;

import java.util.List;

public class ItemSourceNode extends InterfaceNode {
    public ItemSourceNode(Graph graph) {
        super(ModNodeTypes.ITEM_SOURCE, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredOutput("items", this) };
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

        var storage = TransferNodeUtils.buildCombinedStorage(positions, world, ItemStorage.SIDED);

        var stream = PathBundle.<Storage<ItemVariant>, ItemTransformer>begin(storage);
        return Either.left(new DataValue<?>[] { ModDataTypes.ITEM_STREAM.makeValue(stream) });
    }
}
