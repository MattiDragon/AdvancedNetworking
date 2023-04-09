package io.github.mattidragon.advancednetworking.graph.node.energy.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.LimitNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import team.reborn.energy.api.EnergyStorage;

public class LimitEnergyNode extends LimitNode<EnergyStorage, EnergyLimitTransformer> {
    public LimitEnergyNode(Graph graph) {
        super(ModNodeTypes.LIMIT_ENERGY, graph, 256);
    }

    @Override
    protected DataType<PathBundle<EnergyStorage, EnergyLimitTransformer>> getDataType() {
        return ModDataTypes.ENERGY_STREAM;
    }

    @Override
    protected EnergyLimitTransformer createLimiter(int limit) {
        return new EnergyLimitTransformer(limit);
    }
}
