package io.github.mattidragon.advancednetworking.graph.node.item.route;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.SingleSliderNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class MergeItemsNode extends SingleSliderNode {
    private int count = 2;

    public MergeItemsNode(Graph graph) {
        super(ModNodeTypes.MERGE_ITEMS, List.of(), graph);
    }

    @Override
    public Connector<?>[] getInputs() {
        var connectors = new Connector[count];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = ModDataTypes.ITEM_STREAM.makeRequiredInput(String.valueOf(i), this);
        }

        return connectors;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeOptionalOutput("combined", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var current = inputs[0].getAs(ModDataTypes.ITEM_STREAM);

        for (int i = 1; i < count; i++) {
            current = current.merge(inputs[i].getAs(ModDataTypes.ITEM_STREAM));
        }

        return Either.left(new DataValue<?>[] { ModDataTypes.ITEM_STREAM.makeValue(current) });
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        count = MathHelper.clamp(view.getInt("count", 2), 2, 8);
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("count", count);
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
