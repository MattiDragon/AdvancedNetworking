package io.github.mattidragon.advancednetworking.graph.node.fluid.info;

import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.CountNode;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;

public class FluidCountNode extends CountNode<Fluid, FluidVariant> {
    public FluidCountNode(Graph graph) {
        super(ModNodeTypes.FLUID_COUNT, graph, Registries.FLUID, FluidStorage.SIDED);
    }
}
