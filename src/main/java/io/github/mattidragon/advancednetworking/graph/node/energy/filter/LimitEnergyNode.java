package io.github.mattidragon.advancednetworking.graph.node.energy.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.LimitNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.nodeflow.graph.Graph;
import team.reborn.energy.api.EnergyStorage;

public class LimitEnergyNode extends LimitNode<EnergyStorage, EnergyLimitTransformer> {
    public LimitEnergyNode(Graph graph) {
        super(ModNodeTypes.LIMIT_ENERGY, graph, ModDataTypes.ENERGY_STREAM, 256);
    }

    @Override
    protected EnergyLimitTransformer createLimiter(int limit) {
        return new EnergyLimitTransformer(limit);
    }
}
