package io.github.mattidragon.advancednetworking.graph;

import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import team.reborn.energy.api.EnergyStorage;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class ModDataTypes {
    public static final DataType<PathBundle<Storage<ItemVariant>, ItemTransformer>> ITEM_STREAM = new DataType<>(0x30F271,false);
    public static final DataType<PathBundle<Storage<FluidVariant>, FluidTransformer>> FLUID_STREAM = new DataType<>(0x36C3FF,false);
    public static final DataType<PathBundle<EnergyStorage, EnergyLimitTransformer>> ENERGY_STREAM = new DataType<>(0xe83a09,false);

    public static void register() {
        DataType.register(ITEM_STREAM, id("item_stream"));
        DataType.register(FLUID_STREAM, id("fluid_stream"));
        DataType.register(ENERGY_STREAM, id("energy_stream"));
    }
}
