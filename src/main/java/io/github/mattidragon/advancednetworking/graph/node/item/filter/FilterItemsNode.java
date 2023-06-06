package io.github.mattidragon.advancednetworking.graph.node.item.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.FilterNode;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.function.Predicate;

public class FilterItemsNode extends FilterNode<Item, ItemVariant, ItemTransformer> {
    public FilterItemsNode(Graph graph) {
        super(ModNodeTypes.FILTER_ITEMS, graph, Registry.ITEM);
    }

    @Override
    protected DataType<PathBundle<Storage<ItemVariant>, ItemTransformer>> getDataType() {
        return ModDataTypes.ITEM_STREAM;
    }

    @Override
    protected ItemTransformer createTransformer(Predicate<ItemVariant> predicate) {
        return new ItemTransformer.Filter(predicate);
    }
}
