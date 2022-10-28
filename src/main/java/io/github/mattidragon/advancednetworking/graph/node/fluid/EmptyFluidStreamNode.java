package io.github.mattidragon.advancednetworking.graph.node.fluid;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.stream.ResourceStream;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.text.Text;

import java.util.List;

public class EmptyFluidStreamNode extends Node {
    public EmptyFluidStreamNode(Graph graph) {
        super(ModNodeTypes.EMPTY_FLUID_STREAM, List.of(NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.FLUID_STREAM.makeRequiredOutput("fluid", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);

        var stream = ResourceStream.start(Storage.<FluidVariant>empty());
        controller.controller().addFluidStream(stream);
        return Either.left(new DataValue<?>[] { ModDataTypes.FLUID_STREAM.makeValue(stream) });
    }
}
