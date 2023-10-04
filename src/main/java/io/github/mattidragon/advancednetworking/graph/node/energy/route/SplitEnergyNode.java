package io.github.mattidragon.advancednetworking.graph.node.energy.route;

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

public class SplitEnergyNode extends SingleSliderNode {
    private int count = 2;

    public SplitEnergyNode(Graph graph) {
        super(ModNodeTypes.SPLIT_ENERGY, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        var connectors = new Connector[count];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = ModDataTypes.ENERGY_STREAM.makeOptionalOutput(String.valueOf(i), this);
        }

        return connectors;
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.ENERGY_STREAM.makeRequiredInput("stream", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var current = inputs[0].getAs(ModDataTypes.ENERGY_STREAM);
        var out = new DataValue<?>[count];

        for (int i = 0; i < count - 1; i++) {
            var split = current.split();
            out[i] = ModDataTypes.ENERGY_STREAM.makeValue(current);
            current = split;
        }
        out[count - 1] = ModDataTypes.ENERGY_STREAM.makeValue(current);

        return Either.left(out);
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
