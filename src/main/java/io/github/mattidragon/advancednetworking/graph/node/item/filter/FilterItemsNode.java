package io.github.mattidragon.advancednetworking.graph.node.item.filter;

import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.base.FilterResourceNode;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class FilterItemsNode extends FilterResourceNode<ItemVariant, ItemTransformer> {
    public FilterItemsNode(Graph graph) {
        super(ModNodeTypes.FILTER_ITEMS, graph);
    }

    @Override
    protected DataType<PathBundle<Storage<ItemVariant>, ItemTransformer>> getDataType() {
        return ModDataTypes.ITEM_STREAM;
    }

    @Override
    protected @Nullable NbtCompound getNbt(ItemVariant resource) {
        return resource.getNbt();
    }

    @Override
    protected Identifier getId(ItemVariant resource) {
        return Registry.ITEM.getId(resource.getItem());
    }

    @Override
    protected ItemTransformer createTransformer(Predicate<ItemVariant> predicate) {
        return new ItemTransformer.Filter(predicate);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected RegistryEntry<?> getRegistryEntry(ItemVariant resource) {
        return resource.getItem().getRegistryEntry();
    }

    @Override
    protected Registry<?> getRegistry() {
        return Registry.ITEM;
    }
}
