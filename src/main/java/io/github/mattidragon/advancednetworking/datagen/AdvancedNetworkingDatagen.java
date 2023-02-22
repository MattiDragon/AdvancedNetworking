package io.github.mattidragon.advancednetworking.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AdvancedNetworkingDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.addProvider(ReadmeProvider::new);
        generator.addProvider(ModelProvider::new);
        generator.addProvider(RecipeProvider::new);
        generator.addProvider(BlockLootTableProvider::new);
        generator.addProvider(BlockTagProvider::new);
        generator.addProvider(ItemTagProvider::new);
    }
}
