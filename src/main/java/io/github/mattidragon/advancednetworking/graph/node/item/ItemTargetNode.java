package io.github.mattidragon.advancednetworking.graph.node.item;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.InterfaceNode;
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
        var optionalPos = findInterface(world, controller.graphId());
        if (optionalPos.isEmpty())
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var storage = ItemStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
        if (storage == null)
            return Either.right(Text.translatable("node.advanced_networking.item_target.missing", interfaceId));

        var stream = inputs[0].getAs(ModDataTypes.ITEM_STREAM);
        stream.end(storage, controller.controller().itemEnvironment);
        return Either.left(new DataValue<?>[0]);
    }
}
