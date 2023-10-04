package io.github.mattidragon.advancednetworking.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AdvancedNetworkingDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();

        pack.addProvider(ReadmeProvider::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(RecipeProvider::new);
        pack.addProvider(BlockLootTableProvider::new);
        pack.addProvider(NodeTypeTagProvider::new);
        var blockTagProvider = pack.addProvider(BlockTagProvider::new);
        pack.addProvider((output, registriesFuture) ->  new ItemTagProvider(output, registriesFuture, blockTagProvider));
    }
}
