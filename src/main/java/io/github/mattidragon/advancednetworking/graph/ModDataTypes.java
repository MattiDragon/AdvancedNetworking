package io.github.mattidragon.advancednetworking.graph;

import io.github.mattidragon.advancednetworking.graph.stream.ResourceStream;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import team.reborn.energy.api.EnergyStorage;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class ModDataTypes {
    public static final DataType<ResourceStream.Extendable<Storage<FluidVariant>>> FLUID_STREAM = new DataType<>(0x36C3FF,false);
    public static final DataType<ResourceStream.Extendable<Storage<ItemVariant>>> ITEM_STREAM = new DataType<>(0x30F271,false);
    public static final DataType<ResourceStream.Extendable<EnergyStorage>> ENERGY_STREAM = new DataType<>(0xe83a09,false);

    public static void register() {
        DataType.register(FLUID_STREAM, id("fluid_stream"));
        DataType.register(ITEM_STREAM, id("item_stream"));
        DataType.register(ENERGY_STREAM, id("energy_stream"));
    }
}
