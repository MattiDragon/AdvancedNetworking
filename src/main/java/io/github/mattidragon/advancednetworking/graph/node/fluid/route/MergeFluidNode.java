package io.github.mattidragon.advancednetworking.graph.node.fluid.route;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.SingleSliderNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class MergeFluidNode extends SingleSliderNode {
    private int count = 2;

    public MergeFluidNode(Graph graph) {
        super(ModNodeTypes.MERGE_FLUID, List.of(), graph);
    }

    @Override
    public Connector<?>[] getInputs() {
        var connectors = new Connector[count];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = ModDataTypes.FLUID_STREAM.makeRequiredInput(String.valueOf(i), this);
        }

        return connectors;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.FLUID_STREAM.makeOptionalOutput("combined", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var current = inputs[0].getAs(ModDataTypes.FLUID_STREAM);

        for (int i = 1; i < count; i++) {
            current = current.merge(inputs[i].getAs(ModDataTypes.FLUID_STREAM));
        }

        return Either.left(new DataValue<?>[] { ModDataTypes.FLUID_STREAM.makeValue(current) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        count = MathHelper.clamp(data.getInt("count"), 2, 8);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putInt("count", count);
    }

    @Override
    public Text getSliderText() {
        return Text.translatable("node.advanced_networking.streams");
    }

    @Override
    public int getMin() {
        return 2;
    }

    @Override
    public int getMax() {
        return 8;
    }

    @Override
    public int getValue() {
        return count;
    }

    @Override
    public void setValue(int value) {
        count = value;
    }
}
