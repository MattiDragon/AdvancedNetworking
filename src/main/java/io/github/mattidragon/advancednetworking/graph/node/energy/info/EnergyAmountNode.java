package io.github.mattidragon.advancednetworking.graph.node.energy.info;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyNodeUtils;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;

import java.util.List;

public class EnergyAmountNode extends InterfaceNode {
    public EnergyAmountNode(Graph graph) {
        super(ModNodeTypes.ENERGY_AMOUNT, List.of(NetworkControllerContext.TYPE, ContextType.SERVER_WORLD), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { DataType.NUMBER.makeRequiredOutput("amount", this) };
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

        var storage = EnergyNodeUtils.buildCombinedStorage(positions, world);

        return Either.left(new DataValue<?>[]{ DataType.NUMBER.makeValue((double) storage.getAmount()) });
    }
}
