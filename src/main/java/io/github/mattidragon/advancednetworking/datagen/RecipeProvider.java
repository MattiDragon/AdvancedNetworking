package io.github.mattidragon.advancednetworking.datagen;

import io.github.mattidragon.advancednetworking.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;

import java.util.function.Consumer;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        ShapelessRecipeJsonBuilder.create(ModItems.COMPOUND)
                .input(Items.AMETHYST_SHARD)
                .input(Items.CLAY_BALL)
                .input(ItemTags.COALS)
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .criterion(hasItem(Items.AMETHYST_SHARD), conditionsFromItem(Items.AMETHYST_SHARD))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(ModItems.CABLE, 8)
                .input('#', ModItems.COMPOUND)
                .pattern("###")
                .criterion(hasItem(ModItems.CABLE), conditionsFromItem(ModItems.CABLE))
                .criterion(hasItem(ModItems.COMPOUND), conditionsFromItem(ModItems.COMPOUND))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(ModItems.CONTROLLER)
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
