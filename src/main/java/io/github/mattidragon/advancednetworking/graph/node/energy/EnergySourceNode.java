package io.github.mattidragon.advancednetworking.graph.node.energy;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.InterfaceNode;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class EnergySourceNode extends InterfaceNode {
    public EnergySourceNode(Graph graph) {
        super(ModNodeTypes.ENERGY_SOURCE, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
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
        var world = context.get(ContextType.SERVER_WORLD);
        var optionalPos = findInterface(world, controller.graphId());
        if (optionalPos.isEmpty())
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var storage = EnergyStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
        if (storage == null)
            return Either.right(Text.translatable("node.advanced_networking.energy_source.missing", interfaceId));

        var bundle = PathBundle.<EnergyStorage, EnergyLimitTransformer>begin(storage);
        return Either.left(new DataValue<?>[] { ModDataTypes.ENERGY_STREAM.makeValue(bundle) });
    }
}
