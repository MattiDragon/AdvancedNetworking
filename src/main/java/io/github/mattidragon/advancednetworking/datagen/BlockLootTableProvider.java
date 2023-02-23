package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    public BlockLootTableProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    public void generateBlockLootTables() {
        addDrop(ModBlocks.CABLE);
        addDrop(ModBlocks.CONTROLLER);
    }
}
