package io.github.mattidragon.advancednetworking.graph.node.fluid.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.LimitNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public class LimitFluidNode extends LimitNode<Storage<FluidVariant>, FluidTransformer> {
    public LimitFluidNode(Graph graph) {
        super(ModNodeTypes.LIMIT_FLUID, graph, ModDataTypes.FLUID_STREAM, (int) FluidConstants.BUCKET);
    }

    @Override
    protected int getStepSize() {
        return 1000;
    }

    @Override
    protected FluidTransformer createLimiter(int limit) {
        return new FluidTransformer.Limit(limit);
    }
}
