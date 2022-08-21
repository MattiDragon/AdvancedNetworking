package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public final class ModBlocks {
    public static final ControllerBlock CONTROLLER = new ControllerBlock(FabricBlockSettings.of(Material.STONE));
    public static final BlockEntityType<ControllerBlockEntity> CONTROLLER_BLOCK_ENTITY = BlockEntityType.Builder.create(ControllerBlockEntity::new, ModBlocks.CONTROLLER).build(null);

    private ModBlocks() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registry.BLOCK, AdvancedNetworking.id("controller"), CONTROLLER);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, AdvancedNetworking.id("controller"), CONTROLLER_BLOCK_ENTITY);
    }
}
