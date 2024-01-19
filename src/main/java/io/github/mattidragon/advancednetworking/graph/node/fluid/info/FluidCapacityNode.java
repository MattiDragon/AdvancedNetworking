package io.github.mattidragon.advancednetworking.graph.node.fluid.info;

import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.CapacityNode;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;

public class FluidCapacityNode extends CapacityNode<Fluid, FluidVariant> {
    public FluidCapacityNode(Graph graph) {
        super(ModNodeTypes.FLUID_COUNT, graph, FluidStorage.SIDED);
    }
}
