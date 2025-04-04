package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModItems;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

public class ANRecipeGenerator extends RecipeGenerator {
    protected ANRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
    }

    @Override
    public void generate() {
        createShapeless(RecipeCategory.MISC, ModItems.COMPOUND)
                .input(Items.AMETHYST_SHARD)
                .input(Items.CLAY_BALL)
                .input(ItemTags.COALS)
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .criterion(hasItem(Items.AMETHYST_SHARD), conditionsFromItem(Items.AMETHYST_SHARD))
                .offerTo(exporter);

        createShaped(RecipeCategory.REDSTONE, ModItems.CABLE, 8)
                .input('#', ModItems.COMPOUND)
                .pattern("###")
                .criterion(hasItem(ModItems.CABLE), conditionsFromItem(ModItems.CABLE))
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .offerTo(exporter);

        createShaped(RecipeCategory.REDSTONE, ModItems.CONTROLLER)
                .input('#', ModItems.COMPOUND)
                .input('D', Items.DIAMOND)
                .input('I', Items.IRON_INGOT)
                .pattern("I#I")
                .pattern("#D#")
                .pattern("I#I")
                .criterion(hasItem(ModItems.CONTROLLER), conditionsFromItem(ModItems.CONTROLLER))
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .offerTo(exporter);
    }
}
