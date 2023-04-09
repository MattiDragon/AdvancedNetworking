package io.github.mattidragon.advancednetworking.graph.node.fluid.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.FilterResourceNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class FilterFluidNode extends FilterResourceNode<FluidVariant, FluidTransformer> {
    public FilterFluidNode(Graph graph) {
        super(ModNodeTypes.FILTER_FLUID, graph);
    }

    @Override
    protected DataType<PathBundle<Storage<FluidVariant>, FluidTransformer>> getDataType() {
        return ModDataTypes.FLUID_STREAM;
    }

    @Override
    protected @Nullable NbtCompound getNbt(FluidVariant resource) {
        return resource.getNbt();
    }

    @Override
    protected Identifier getId(FluidVariant resource) {
        return Registry.FLUID.getId(resource.getFluid());
    }

    @Override
    protected FluidTransformer createTransformer(Predicate<FluidVariant> predicate) {
        return new FluidTransformer.Filter(predicate);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected RegistryEntry<?> getRegistryEntry(FluidVariant resource) {
        return resource.getFluid().getRegistryEntry();
    }

    @Override
    protected Registry<?> getRegistry() {
        return Registry.FLUID;
    }
}
