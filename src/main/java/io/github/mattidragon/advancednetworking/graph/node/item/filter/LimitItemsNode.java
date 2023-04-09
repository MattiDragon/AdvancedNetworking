package io.github.mattidragon.advancednetworking.graph.node.item.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.LimitNode;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public class LimitItemsNode extends LimitNode<Storage<ItemVariant>, ItemTransformer> {
    public LimitItemsNode(Graph graph) {
        super(ModNodeTypes.LIMIT_ITEMS, graph, 64);
    }

    @Override
    protected DataType<PathBundle<Storage<ItemVariant>, ItemTransformer>> getDataType() {
        return ModDataTypes.ITEM_STREAM;
    }

    @Override
    protected ItemTransformer createLimiter(int limit) {
        return new ItemTransformer.Limit(limit);
    }
}
