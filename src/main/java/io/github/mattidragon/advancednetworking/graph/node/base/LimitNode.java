package io.github.mattidragon.advancednetworking.graph.node.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class LimitNode<S, T> extends SingleSliderNode {
    private final DataType<PathBundle<S, T>> dataType;
    private final int max;
    private int limit;

    public LimitNode(NodeType<? extends LimitNode<S, T>> type, Graph graph, DataType<PathBundle<S, T>> dataType, int max) {
        super(type, List.of(), graph);
        this.dataType = dataType;
        this.max = max;
        this.limit = max;
    }

    protected final DataType<PathBundle<S, T>> getDataType() {
        return dataType;
    }

    protected abstract T createLimiter(int limit);

    protected int getStepSize() {
        return 1;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { getDataType().makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { getDataType().makeRequiredInput("in", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var stream = inputs[0].getAs(getDataType());
        stream.transform(createLimiter(limit));
        return Either.left(new DataValue<?>[]{ getDataType().makeValue(stream) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        limit = MathHelper.clamp(data.getInt("limit"), 1, (int) FluidConstants.BUCKET);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putInt("limit", limit);
    }

    @Override
    public int getMin() {
        return 1;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public int getValue() {
        return limit;
    }

    @Override
    public void setValue(int value) {
        limit = (int) (Math.round(value / (double) getStepSize()) * getStepSize());
    }

    @Override
    public Text getSliderText() {
        return Text.translatable("node.advanced_networking.limit");
    }
}
