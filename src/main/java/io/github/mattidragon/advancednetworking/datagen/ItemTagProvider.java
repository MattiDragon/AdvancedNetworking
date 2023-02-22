package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(ModTags.Items.WRENCHES).addOptionalTag(Identifier.of("c", "wrenches")).add(Items.STICK);
    }
}
