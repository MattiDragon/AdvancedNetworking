package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.ItemTags;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.COMPOUND)
                .input(Items.AMETHYST_SHARD)
                .input(Items.CLAY_BALL)
                .input(ItemTags.COALS)
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .criterion(hasItem(Items.AMETHYST_SHARD), conditionsFromItem(Items.AMETHYST_SHARD))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ModItems.CABLE, 8)
                .input('#', ModItems.COMPOUND)
                .pattern("###")
                .criterion(hasItem(ModItems.CABLE), conditionsFromItem(ModItems.CABLE))
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ModItems.CONTROLLER)
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
