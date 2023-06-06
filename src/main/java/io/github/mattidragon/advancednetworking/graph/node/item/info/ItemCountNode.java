package io.github.mattidragon.advancednetworking.graph.node.item.info;

import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.CountNode;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class ItemCountNode extends CountNode<Item, ItemVariant> {
    public ItemCountNode(Graph graph) {
        super(ModNodeTypes.ITEM_COUNT, graph, Registries.ITEM, ItemStorage.SIDED);
    }
}
