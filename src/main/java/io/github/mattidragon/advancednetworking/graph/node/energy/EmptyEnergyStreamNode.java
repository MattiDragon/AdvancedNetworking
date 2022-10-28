package io.github.mattidragon.advancednetworking.graph.node.energy;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.stream.ResourceStream;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.text.Text;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class EmptyEnergyStreamNode extends Node {
    public EmptyEnergyStreamNode(Graph graph) {
        super(ModNodeTypes.EMPTY_ENERGY_STREAM, List.of(NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ENERGY_STREAM.makeRequiredOutput("energy", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);

        var stream = ResourceStream.start(EnergyStorage.EMPTY);
        controller.controller().addEnergyStream(stream);
        return Either.left(new DataValue<?>[] { ModDataTypes.ENERGY_STREAM.makeValue(stream) });
    }
}
