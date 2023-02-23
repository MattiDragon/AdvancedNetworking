package io.github.mattidragon.advancednetworking.graph;

import io.github.mattidragon.advancednetworking.graph.node.energy.filter.LimitEnergyNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.info.EnergyCapacityNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.info.EnergyAmountNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.route.MergeEnergyNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.route.SplitEnergyNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.storage.EmptyEnergyStreamNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.storage.EnergySourceNode;
import io.github.mattidragon.advancednetworking.graph.node.energy.storage.EnergyTargetNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.filter.FilterFluidNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.filter.LimitFluidNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.info.FluidCapacityNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.info.FluidCountNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.route.MergeFluidNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.route.SplitFluidNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.storage.EmptyFluidStreamNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.storage.FluidSourceNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.storage.FluidTargetNode;
import io.github.mattidragon.advancednetworking.graph.node.item.filter.FilterItemsNode;
import io.github.mattidragon.advancednetworking.graph.node.item.filter.LimitItemsNode;
import io.github.mattidragon.advancednetworking.graph.node.item.info.ItemCapacityNode;
import io.github.mattidragon.advancednetworking.graph.node.item.info.ItemCountNode;
import io.github.mattidragon.advancednetworking.graph.node.item.route.MergeItemsNode;
import io.github.mattidragon.advancednetworking.graph.node.item.route.SplitItemsNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.EmptyItemStreamNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemSourceNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemTargetNode;
import io.github.mattidragon.advancednetworking.graph.node.redstone.ReadRedstoneNode;
import io.github.mattidragon.advancednetworking.graph.node.redstone.SetRedstoneNode;
import io.github.mattidragon.advancednetworking.graph.node.redstone.WriteRedstoneNode;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class ModNodeTypes {
    public static final NodeType<ReadRedstoneNode> READ_REDSTONE = new NodeType<>(ReadRedstoneNode::new);
    public static final NodeType<WriteRedstoneNode> WRITE_REDSTONE = new NodeType<>(WriteRedstoneNode::new);
    public static final NodeType<SetRedstoneNode> SET_REDSTONE = new NodeType<>(SetRedstoneNode::new);
    public static final NodeGroup REDSTONE_GROUP = new NodeGroup(Text.translatable("group.advanced_networking.redstone"), READ_REDSTONE, WRITE_REDSTONE, SET_REDSTONE);

    public static final NodeType<ItemSourceNode> ITEM_SOURCE = new NodeType<>(ItemSourceNode::new);
    public static final NodeType<ItemTargetNode> ITEM_TARGET = new NodeType<>(ItemTargetNode::new);
    public static final NodeType<SplitItemsNode> SPLIT_ITEMS = new NodeType<>(SplitItemsNode::new);
    public static final NodeType<MergeItemsNode> MERGE_ITEMS = new NodeType<>(MergeItemsNode::new);
    public static final NodeType<EmptyItemStreamNode> EMPTY_ITEM_STREAM = new NodeType<>(EmptyItemStreamNode::new);
    public static final NodeType<LimitItemsNode> LIMIT_ITEMS = new NodeType<>(LimitItemsNode::new);
    public static final NodeType<FilterItemsNode> FILTER_ITEMS = new NodeType<>(FilterItemsNode::new);
    public static final NodeType<ItemCountNode> ITEM_COUNT = new NodeType<>(ItemCountNode::new);
    public static final NodeType<ItemCapacityNode> ITEM_CAPACITY = new NodeType<>(ItemCapacityNode::new);
    public static final NodeGroup ITEM_GROUP = new NodeGroup(Text.translatable("group.advanced_networking.item"), ITEM_SOURCE, ITEM_TARGET, SPLIT_ITEMS, MERGE_ITEMS, EMPTY_ITEM_STREAM, LIMIT_ITEMS, FILTER_ITEMS, ITEM_COUNT, ITEM_CAPACITY);

    public static final NodeType<EnergySourceNode> ENERGY_SOURCE = new NodeType<>(EnergySourceNode::new);
    public static final NodeType<EnergyTargetNode> ENERGY_TARGET = new NodeType<>(EnergyTargetNode::new);
    public static final NodeType<SplitEnergyNode> SPLIT_ENERGY = new NodeType<>(SplitEnergyNode::new);
    public static final NodeType<MergeEnergyNode> MERGE_ENERGY = new NodeType<>(MergeEnergyNode::new);
    public static final NodeType<EmptyEnergyStreamNode> EMPTY_ENERGY_STREAM = new NodeType<>(EmptyEnergyStreamNode::new);
    public static final NodeType<LimitEnergyNode> LIMIT_ENERGY = new NodeType<>(LimitEnergyNode::new);
    public static final NodeType<EnergyCapacityNode> ENERGY_CAPACITY = new NodeType<>(EnergyCapacityNode::new);
    public static final NodeType<EnergyAmountNode> ENERGY_AMOUNT = new NodeType<>(EnergyAmountNode::new);
    public static final NodeGroup ENERGY_GROUP = new NodeGroup(Text.translatable("group.advanced_networking.energy"), ENERGY_SOURCE, ENERGY_TARGET, SPLIT_ENERGY, MERGE_ENERGY, EMPTY_ENERGY_STREAM, LIMIT_ENERGY, ENERGY_AMOUNT, ENERGY_CAPACITY);

    public static final NodeType<FluidSourceNode> FLUID_SOURCE = new NodeType<>(FluidSourceNode::new);
    public static final NodeType<FluidTargetNode> FLUID_TARGET = new NodeType<>(FluidTargetNode::new);
    public static final NodeType<SplitFluidNode> SPLIT_FLUID = new NodeType<>(SplitFluidNode::new);
    public static final NodeType<MergeFluidNode> MERGE_FLUID = new NodeType<>(MergeFluidNode::new);
    public static final NodeType<EmptyFluidStreamNode> EMPTY_FLUID_STREAM = new NodeType<>(EmptyFluidStreamNode::new);
    public static final NodeType<LimitFluidNode> LIMIT_FLUID = new NodeType<>(LimitFluidNode::new);
    public static final NodeType<FilterFluidNode> FILTER_FLUID = new NodeType<>(FilterFluidNode::new);
    public static final NodeType<FluidCountNode> FLUID_COUNT = new NodeType<>(FluidCountNode::new);
    public static final NodeType<FluidCapacityNode> FLUID_CAPACITY = new NodeType<>(FluidCapacityNode::new);
    public static final NodeGroup FLUID_GROUP = new NodeGroup(Text.translatable("group.advanced_networking.fluid"), FLUID_SOURCE, FLUID_TARGET, SPLIT_FLUID, MERGE_FLUID, EMPTY_FLUID_STREAM, LIMIT_FLUID, FILTER_FLUID, FLUID_COUNT, FLUID_CAPACITY);

    public static void register() {
        NodeType.register(READ_REDSTONE, id("read_redstone"));
        NodeType.register(WRITE_REDSTONE, id("write_redstone"));
        NodeType.register(SET_REDSTONE, id("set_redstone"));
        
        NodeType.register(ITEM_SOURCE, id("item_source"));
        NodeType.register(ITEM_TARGET, id("item_target"));
        NodeType.register(SPLIT_ITEMS, id("split_items"));
        NodeType.register(MERGE_ITEMS, id("merge_items"));
        NodeType.register(LIMIT_ITEMS, id("limit_items"));
        NodeType.register(FILTER_ITEMS, id("filter_items"));
        NodeType.register(ITEM_COUNT, id("item_count"));
        NodeType.register(ITEM_CAPACITY, id("item_capacity"));
        NodeType.register(EMPTY_ITEM_STREAM, id("empty_item_stream"));

        NodeType.register(ENERGY_SOURCE, id("energy_source"));
        NodeType.register(ENERGY_TARGET, id("energy_target"));
        NodeType.register(SPLIT_ENERGY, id("split_energy"));
        NodeType.register(MERGE_ENERGY, id("merge_energy"));
        NodeType.register(LIMIT_ENERGY, id("limit_energy"));
        NodeType.register(ENERGY_CAPACITY, id("energy_capacity"));
        NodeType.register(ENERGY_AMOUNT, id("energy_amount"));
        NodeType.register(EMPTY_ENERGY_STREAM, id("empty_energy_stream"));

        NodeType.register(FLUID_SOURCE, id("fluid_source"));
        NodeType.register(FLUID_TARGET, id("fluid_target"));
        NodeType.register(SPLIT_FLUID, id("split_fluid"));
        NodeType.register(MERGE_FLUID, id("merge_fluid"));
        NodeType.register(LIMIT_FLUID, id("limit_fluid"));
        NodeType.register(FILTER_FLUID, id("filter_fluid"));
        NodeType.register(FLUID_COUNT, id("fluid_count"));
        NodeType.register(FLUID_CAPACITY, id("fluid_capacity"));
        NodeType.register(EMPTY_FLUID_STREAM, id("empty_fluid_stream"));
    }
}
