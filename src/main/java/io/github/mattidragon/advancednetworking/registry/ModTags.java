package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> WRENCHES = TagKey.of(Registry.ITEM_KEY, AdvancedNetworking.id("wrenches"));
    }
}
