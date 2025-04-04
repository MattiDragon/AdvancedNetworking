package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlock;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModBlocks {
    public static final ControllerBlock CONTROLLER = new ControllerBlock(AbstractBlock.Settings.create().registryKey(key("controller")).mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5F));
    public static final CableBlock CABLE = new CableBlock(AbstractBlock.Settings.create().registryKey(key("cable")).mapColor(MapColor.DEEPSLATE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5F));
    public static final BlockEntityType<ControllerBlockEntity> CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ControllerBlockEntity::new, ModBlocks.CONTROLLER).build();
    public static final BlockEntityType<CableBlockEntity> CABLE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, ModBlocks.CABLE).build();

    private ModBlocks() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registries.BLOCK, AdvancedNetworking.id("controller"), CONTROLLER);
        Registry.register(Registries.BLOCK, AdvancedNetworking.id("cable"), CABLE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, AdvancedNetworking.id("controller"), CONTROLLER_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, AdvancedNetworking.id("cable"), CABLE_BLOCK_ENTITY);
    }

    private static RegistryKey<Block> key(String path) {
        return RegistryKey.of(RegistryKeys.BLOCK, AdvancedNetworking.id(path));
    }
}
