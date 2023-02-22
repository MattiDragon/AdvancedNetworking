package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> WRENCHES = TagKey.of(RegistryKeys.ITEM, AdvancedNetworking.id("wrenches"));
    }
}
