package io.github.mattidragon.advancednetworking.graph.node.fluid;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.client.screen.SliderConfigScreen;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class SplitFluidNode extends Node {
    private int count = 2;

    public SplitFluidNode(Graph graph) {
        super(ModNodeTypes.SPLIT_FLUID, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        var connectors = new Connector[count];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = ModDataTypes.FLUID_STREAM.makeOptionalOutput(String.valueOf(i), this);
        }

        return connectors;
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.FLUID_STREAM.makeRequiredInput("items", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var current = inputs[0].getAs(ModDataTypes.FLUID_STREAM);
        var out = new DataValue<?>[count];

        for (int i = 0; i < count - 1; i++) {
            var split = current.split();
            out[i] = ModDataTypes.FLUID_STREAM.makeValue(split.getLeft());
            current = split.getRight();
        }
        out[count - 1] = ModDataTypes.FLUID_STREAM.makeValue(current);

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

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new SliderConfigScreen(this, parent, value -> count = value, () -> count, Text.translatable("node.advanced_networking.streams"), 2, 8);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }
}
