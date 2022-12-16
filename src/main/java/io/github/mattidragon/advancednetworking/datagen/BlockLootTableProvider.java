package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    public BlockLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.CABLE);
        addDrop(ModBlocks.CONTROLLER);
    }
}
