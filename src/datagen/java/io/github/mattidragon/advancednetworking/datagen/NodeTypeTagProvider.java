package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NodeTypeTagProvider extends FabricTagProvider<NodeType<?>> {
    public NodeTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, NodeType.KEY, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var valueToKey = (Function<NodeType<?>, RegistryKey<NodeType<?>>>) type -> NodeType.REGISTRY.getKey(type).orElseThrow();

        builder(ModNodeTypes.REDSTONE_GROUP).mapped(valueToKey)
                .add(ModNodeTypes.READ_REDSTONE, ModNodeTypes.WRITE_REDSTONE, ModNodeTypes.SET_REDSTONE);
        builder(ModNodeTypes.ITEM_GROUP).mapped(valueToKey)
                .add(ModNodeTypes.ITEM_SOURCE, ModNodeTypes.ITEM_TARGET, ModNodeTypes.SPLIT_ITEMS,
                        ModNodeTypes.MERGE_ITEMS, ModNodeTypes.EMPTY_ITEM_STREAM, ModNodeTypes.LIMIT_ITEMS,
                        ModNodeTypes.FILTER_ITEMS, ModNodeTypes.ITEM_COUNT, ModNodeTypes.ITEM_CAPACITY);
        builder(ModNodeTypes.FLUID_GROUP).mapped(valueToKey)
                .add(ModNodeTypes.FLUID_SOURCE, ModNodeTypes.FLUID_TARGET, ModNodeTypes.SPLIT_FLUID,
                        ModNodeTypes.MERGE_FLUID, ModNodeTypes.EMPTY_FLUID_STREAM, ModNodeTypes.LIMIT_FLUID,
                        ModNodeTypes.FILTER_FLUID, ModNodeTypes.FLUID_COUNT, ModNodeTypes.FLUID_CAPACITY);
        builder(ModNodeTypes.ENERGY_GROUP).mapped(valueToKey)
                .add(ModNodeTypes.ENERGY_SOURCE, ModNodeTypes.ENERGY_TARGET, ModNodeTypes.SPLIT_ENERGY,
                        ModNodeTypes.MERGE_ENERGY, ModNodeTypes.EMPTY_ENERGY_STREAM, ModNodeTypes.LIMIT_ENERGY,
                        ModNodeTypes.ENERGY_AMOUNT, ModNodeTypes.ENERGY_CAPACITY);
    }
}
