package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public final class ModItems {
    public static final BlockItem CONTROLLER = new BlockItem(ModBlocks.CONTROLLER, new FabricItemSettings().group(ItemGroup.REDSTONE));
    public static final BlockItem CABLE = new BlockItem(ModBlocks.CABLE, new FabricItemSettings().group(ItemGroup.REDSTONE));
    public static final Item COMPOUND = new Item(new FabricItemSettings().group(ItemGroup.REDSTONE));

    private ModItems() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registry.ITEM, AdvancedNetworking.id("controller"), CONTROLLER);
        Registry.register(Registry.ITEM, AdvancedNetworking.id("cable"), CABLE);
        Registry.register(Registry.ITEM, AdvancedNetworking.id("compound"), COMPOUND);
    }
}
