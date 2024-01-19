package io.github.mattidragon.advancednetworking.graph.node.item.info;

import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.CapacityNode;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;

public class ItemCapacityNode extends CapacityNode<Item, ItemVariant> {
    public ItemCapacityNode(Graph graph) {
        super(ModNodeTypes.ITEM_COUNT, graph, ItemStorage.SIDED);
    }
}
