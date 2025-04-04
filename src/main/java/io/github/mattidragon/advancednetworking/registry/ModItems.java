package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
    public static final BlockItem CONTROLLER = new BlockItem(ModBlocks.CONTROLLER, new Item.Settings().registryKey(key("controller")));
    public static final BlockItem CABLE = new BlockItem(ModBlocks.CABLE, new Item.Settings().registryKey(key("cable")));
    public static final Item COMPOUND = new Item(new Item.Settings().registryKey(key("compound")));

    private ModItems() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registries.ITEM, AdvancedNetworking.id("controller"), CONTROLLER);
        Registry.register(Registries.ITEM, AdvancedNetworking.id("cable"), CABLE);
        Registry.register(Registries.ITEM, AdvancedNetworking.id("compound"), COMPOUND);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addBefore(Items.RAIL, CONTROLLER);
            entries.addBefore(Items.RAIL, CABLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.LECTERN, CONTROLLER);
            entries.addAfter(Items.LECTERN, CABLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.addAfter(Items.DISC_FRAGMENT_5, COMPOUND));
    }

    private static RegistryKey<Item> key(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, AdvancedNetworking.id(path));
    }
}
