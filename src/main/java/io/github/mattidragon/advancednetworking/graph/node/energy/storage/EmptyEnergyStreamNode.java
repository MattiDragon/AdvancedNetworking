package io.github.mattidragon.advancednetworking.graph.node.energy.storage;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.text.Text;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class EmptyEnergyStreamNode extends Node {
    public EmptyEnergyStreamNode(Graph graph) {
        super(ModNodeTypes.EMPTY_ENERGY_STREAM, List.of(), graph);
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
        var stream = PathBundle.<EnergyStorage, EnergyLimitTransformer>begin(EnergyStorage.EMPTY);
        return Either.left(new DataValue<?>[] { ModDataTypes.ENERGY_STREAM.makeValue(stream) });
    }
}
