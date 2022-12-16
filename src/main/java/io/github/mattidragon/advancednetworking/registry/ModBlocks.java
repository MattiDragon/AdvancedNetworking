package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {
    public static final ControllerBlock CONTROLLER = new ControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.5F));
    public static final CableBlock CABLE = new CableBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.5F));
    public static final BlockEntityType<ControllerBlockEntity> CONTROLLER_BLOCK_ENTITY = BlockEntityType.Builder.create(ControllerBlockEntity::new, ModBlocks.CONTROLLER).build(null);
    public static final BlockEntityType<CableBlockEntity> CABLE_BLOCK_ENTITY = BlockEntityType.Builder.create(CableBlockEntity::new, ModBlocks.CABLE).build(null);

    private ModBlocks() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registries.BLOCK, AdvancedNetworking.id("controller"), CONTROLLER);
        Registry.register(Registries.BLOCK, AdvancedNetworking.id("cable"), CABLE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, AdvancedNetworking.id("controller"), CONTROLLER_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, AdvancedNetworking.id("cable"), CABLE_BLOCK_ENTITY);
    }
}
