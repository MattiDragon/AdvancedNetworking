package io.github.mattidragon.advancednetworking.networking.node;

import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.data.DataType;
import io.github.mattidragon.advancednetworking.networking.data.DataValue;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class TimeNode extends Node {
    private final Connector<?>[] outputs = { DataType.NUMBER.makeConnector("time", true, this) };

    public TimeNode() {
        super(NodeType.TIME);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return outputs;
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    public DataValue<?>[] process(DataValue<?>[] inputs, ServerWorld world, BlockPos pos) {
        return new DataValue[] { DataType.NUMBER.makeValue((double) world.getTime()) };
    }
}
