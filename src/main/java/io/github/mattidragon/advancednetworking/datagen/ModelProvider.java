package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;

import java.util.Map;

import static io.github.mattidragon.advancednetworking.registry.ModBlocks.CABLE;
import static io.github.mattidragon.advancednetworking.registry.ModBlocks.CONTROLLER;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        Identifier activeModel = TexturedModel.CUBE_ALL.upload(CONTROLLER, generator.modelCollector);
        Identifier errorModel = generator.createSubModel(CONTROLLER, "_error", Models.CUBE_ALL, TextureMap::all);
        Identifier inactiveModel = generator.createSubModel(CONTROLLER, "_inactive", Models.CUBE_ALL, TextureMap::all);

        generator.blockStateCollector
                .accept(VariantsBlockStateSupplier.create(CONTROLLER)
                        .coordinate(BlockStateVariantMap.create(ControllerBlock.POWERED, ControllerBlock.SUCCESS).register((powered, successful) -> {
                            if (!powered)
                                return BlockStateVariant.create().put(VariantSettings.MODEL, inactiveModel);
                            if (!successful)
                                return BlockStateVariant.create().put(VariantSettings.MODEL, errorModel);
                            return BlockStateVariant.create().put(VariantSettings.MODEL, activeModel);
                        })));

        generateCableModel(generator);
    }

    private void generateCableModel(BlockStateModelGenerator generator) {
        var noneId = ModelIds.getBlockSubModelId(CABLE, "_none");
        var disabledId = ModelIds.getBlockSubModelId(CABLE, "_disabled");
        var interfaceId = ModelIds.getBlockSubModelId(CABLE, "_interface");
        var poweredInterfaceId = ModelIds.getBlockSubModelId(CABLE, "_powered_interface");
        var connectionId = ModelIds.getBlockSubModelId(CABLE, "_connection");

        var models = Map.of(CableBlock.ConnectionType.NONE, noneId,
                CableBlock.ConnectionType.CONNECTED, connectionId,
                CableBlock.ConnectionType.DISABLED, disabledId,
                CableBlock.ConnectionType.INTERFACE, interfaceId,
                CableBlock.ConnectionType.INTERFACE_POWERED, poweredInterfaceId);
        var builder = MultipartBlockStateSupplier.create(CABLE);

        for (var type : CableBlock.ConnectionType.values()) {
            var modelId = models.get(type);

            builder.with(When.create().set(CableBlock.NORTH, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId));
            builder.with(When.create().set(CableBlock.EAST, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId)
                    .put(VariantSettings.Y, VariantSettings.Rotation.R90));
            builder.with(When.create().set(CableBlock.SOUTH, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId)
                    .put(VariantSettings.Y, VariantSettings.Rotation.R180));
            builder.with(When.create().set(CableBlock.WEST, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId)
                    .put(VariantSettings.Y, VariantSettings.Rotation.R270));
            builder.with(When.create().set(CableBlock.UP, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId)
                    .put(VariantSettings.X, VariantSettings.Rotation.R270));
            builder.with(When.create().set(CableBlock.DOWN, type), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, modelId)
                    .put(VariantSettings.X, VariantSettings.Rotation.R90));
        }

        generator.blockStateCollector.accept(builder);

        generator.excludeFromSimpleItemModelGeneration(CABLE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(ModItems.COMPOUND, Models.GENERATED);
    }
}
