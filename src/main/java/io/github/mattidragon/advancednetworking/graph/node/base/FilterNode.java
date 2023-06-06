package io.github.mattidragon.advancednetworking.graph.node.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.advancednetworking.misc.ResourceFilter;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class FilterNode<R, V extends TransferVariant<R>, T> extends Node {
    private final ResourceFilter<R, V> filter;

    public FilterNode(NodeType<? extends FilterNode<R, V, T>> type, Graph graph, Registry<R> registry) {
        super(type, List.of(), graph);
        this.filter = new ResourceFilter<>(registry);
    }

    protected abstract DataType<PathBundle<Storage<V>, T>> getDataType();

    protected abstract T createTransformer(Predicate<V> predicate);

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { getDataType().makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { getDataType().makeRequiredInput("in", this) };
    }

    @Override
    public List<Text> validate() {
        var list = new ArrayList<Text>();
        list.addAll(filter.validate());
        list.addAll(super.validate());
        return list;
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var stream = inputs[0].getAs(getDataType());
        stream.transform(createTransformer(filter::isAllowed));
        return Either.left(new DataValue<?>[]{ getDataType().makeValue(stream) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        filter.readNbt(data);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        filter.writeNbt(data);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return filter.createScreen(this, parent);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }
}
