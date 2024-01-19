package io.github.mattidragon.advancednetworking.graph.node.energy.info;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.advancednetworking.misc.CombinedEnergyStorage;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Objects;

public class EnergyCapacityNode extends InterfaceNode {
    public EnergyCapacityNode(Graph graph) {
        super(ModNodeTypes.ENERGY_CAPACITY, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
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

        var storage = new CombinedEnergyStorage(positions.stream()
                .map(sidePos -> {
                    var pos = sidePos.pos();
                    var side = sidePos.side();
                    return EnergyStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
                })
                .filter(Objects::nonNull)
                .toList());

        return Either.left(new DataValue<?>[] { DataType.NUMBER.makeValue((double) storage.getCapacity()) });
    }
}
