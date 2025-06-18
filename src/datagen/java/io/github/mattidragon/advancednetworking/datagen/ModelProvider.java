package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.render.model.json.MultipartModelConditionBuilder;

import java.util.Map;

import static io.github.mattidragon.advancednetworking.registry.ModBlocks.CABLE;
import static io.github.mattidragon.advancednetworking.registry.ModBlocks.CONTROLLER;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        var activeModel = TexturedModel.CUBE_ALL.upload(CONTROLLER, generator.modelCollector);
        var errorModel = generator.createSubModel(CONTROLLER, "_error", Models.CUBE_ALL, TextureMap::all);
        var inactiveModel = generator.createSubModel(CONTROLLER, "_inactive", Models.CUBE_ALL, TextureMap::all);

        generator.blockStateCollector
                .accept(VariantsBlockModelDefinitionCreator.of(CONTROLLER)
                        .with(BlockStateVariantMap.models(ControllerBlock.POWERED, ControllerBlock.SUCCESS).generate((powered, successful) -> {
                            if (!powered)
                                return BlockStateModelGenerator.createWeightedVariant(inactiveModel);
                            if (!successful)
                                return BlockStateModelGenerator.createWeightedVariant(errorModel);
                            return BlockStateModelGenerator.createWeightedVariant(activeModel);
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
        var builder = MultipartBlockModelDefinitionCreator.create(CABLE);

        for (var type : CableBlock.ConnectionType.values()) {
            var modelId = models.get(type);

            builder.with(new MultipartModelConditionBuilder().put(CableBlock.NORTH, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId));
            builder.with(new MultipartModelConditionBuilder().put(CableBlock.EAST, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId)
                            .apply(BlockStateModelGenerator.ROTATE_Y_90));
            builder.with(new MultipartModelConditionBuilder().put(CableBlock.SOUTH, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId)
                            .apply(BlockStateModelGenerator.ROTATE_Y_180));
            builder.with(new MultipartModelConditionBuilder().put(CableBlock.WEST, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId)
                            .apply(BlockStateModelGenerator.ROTATE_Y_270));
            builder.with(new MultipartModelConditionBuilder().put(CableBlock.UP, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId)
                            .apply(BlockStateModelGenerator.ROTATE_X_270));
            builder.with(new MultipartModelConditionBuilder().put(CableBlock.DOWN, type),
                    BlockStateModelGenerator.createWeightedVariant(modelId)
                            .apply(BlockStateModelGenerator.ROTATE_X_90));
        }

        generator.blockStateCollector.accept(builder);

//        generator.excludeFromSimpleItemModelGeneration(CABLE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(ModItems.COMPOUND, Models.GENERATED);
        generator.register(ModItems.CABLE);
    }
}
