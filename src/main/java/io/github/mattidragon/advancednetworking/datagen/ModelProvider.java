package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import static io.github.mattidragon.advancednetworking.registry.ModBlocks.CONTROLLER;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        Identifier activeModel = TexturedModel.CUBE_ALL.upload(CONTROLLER, blockStateModelGenerator.modelCollector);
        Identifier errorModel = blockStateModelGenerator.createSubModel(CONTROLLER, "_error", Models.CUBE_ALL, TextureMap::all);
        Identifier inactiveModel = blockStateModelGenerator.createSubModel(CONTROLLER, "_inactive", Models.CUBE_ALL, TextureMap::all);

        blockStateModelGenerator.blockStateCollector
                .accept(VariantsBlockStateSupplier.create(CONTROLLER)
                        .coordinate(BlockStateVariantMap.create(ControllerBlock.POWERED, ControllerBlock.SUCCESS).register((powered, successful) -> {
                            if (!successful)
                                return BlockStateVariant.create().put(VariantSettings.MODEL, errorModel);
                            if (powered)
                                return BlockStateVariant.create().put(VariantSettings.MODEL, activeModel);
                            return BlockStateVariant.create().put(VariantSettings.MODEL, inactiveModel);
                        })));


    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }
}
