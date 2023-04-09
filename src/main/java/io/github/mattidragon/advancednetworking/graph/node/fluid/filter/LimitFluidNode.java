package io.github.mattidragon.advancednetworking.graph.node.fluid.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.LimitNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public class LimitFluidNode extends LimitNode<Storage<FluidVariant>, FluidTransformer> {
    public LimitFluidNode(Graph graph) {
        super(ModNodeTypes.LIMIT_FLUID, graph, (int) FluidConstants.BUCKET);
    }

    @Override
    protected int getStepSize() {
        return 1000;
    }

    @Override
    protected DataType<PathBundle<Storage<FluidVariant>, FluidTransformer>> getDataType() {
        return ModDataTypes.FLUID_STREAM;
    }

    @Override
    protected FluidTransformer createLimiter(int limit) {
        return new FluidTransformer.Limit(limit);
    }
}
