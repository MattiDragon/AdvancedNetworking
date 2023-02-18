package io.github.mattidragon.advancednetworking.graph.node.item;

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

public class LimitItemsNode extends Node {
    private int limit = 64;

    public LimitItemsNode(Graph graph) {
        super(ModNodeTypes.LIMIT_ITEMS, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredInput("in", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var stream = inputs[0].getAs(ModDataTypes.ITEM_STREAM);
        stream.transform(new ItemTransformer.Limit(limit));
        return Either.left(new DataValue<?>[]{ ModDataTypes.ITEM_STREAM.makeValue(stream) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        limit = MathHelper.clamp(data.getInt("limit"), 1, 64);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putInt("limit", limit);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new SliderConfigScreen(this, parent, value -> limit = value, () -> limit, Text.translatable("node.advanced_networking.limit"), 1, 64);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }
}
