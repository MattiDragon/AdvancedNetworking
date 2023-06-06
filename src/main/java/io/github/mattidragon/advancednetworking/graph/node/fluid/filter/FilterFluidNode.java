package io.github.mattidragon.advancednetworking.graph.node.fluid.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.FilterNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;

import java.util.function.Predicate;

public class FilterFluidNode extends FilterNode<Fluid, FluidVariant, FluidTransformer> {
    public FilterFluidNode(Graph graph) {
        super(ModNodeTypes.FILTER_FLUID, graph, Registries.FLUID);
    }

    @Override
    protected DataType<PathBundle<Storage<FluidVariant>, FluidTransformer>> getDataType() {
        return ModDataTypes.FLUID_STREAM;
    }

    @Override
    protected FluidTransformer createTransformer(Predicate<FluidVariant> predicate) {
        return new FluidTransformer.Filter(predicate);
    }
}
