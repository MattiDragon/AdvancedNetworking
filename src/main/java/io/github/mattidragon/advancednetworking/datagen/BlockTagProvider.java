package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.tag.BlockTags;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(ModBlocks.CABLE, ModBlocks.CONTROLLER);
    }
}
