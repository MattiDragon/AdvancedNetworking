package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public final class ModItems {
    public static final BlockItem TEST_BLOCK = new BlockItem(ModBlocks.CONTROLLER, new FabricItemSettings().group(ItemGroup.REDSTONE));

    private ModItems() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registry.ITEM, AdvancedNetworking.id("test_block"), TEST_BLOCK);
    }
}
