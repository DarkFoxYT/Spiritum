package net.dark.spiritum.client.compat;

import net.dark.spiritum.recipe.AlchemyRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public record AlchemyRecipeView(RecipeEntry<AlchemyRecipe> recipe) {
    public List<ItemStack> inputs() {
        List<ItemStack> inputs = new ArrayList<>();
        recipe.value().ingredients().forEach((item, count) -> inputs.add(new ItemStack(item, count)));
        return inputs;
    }

    public Fluid fluid() {
        return recipe.value().fluid();
    }

    public ItemStack output() {
        return recipe.value().result();
    }

    public Text title() {
        return output().getName();
    }

    public List<Text> notes() {
        if (recipe.value().duration() == 0)
            return List.of(Text.translatable("viewer.spiritum.vat_instant"));
        String duration = recipe.value().duration() % 20 == 0 ?
            Integer.toString(recipe.value().duration()/20) :
            Float.toString(recipe.value().duration()/20f);
        return List.of(Text.translatable("viewer.spiritum.vat_duration", duration));
    }

    public static List<AlchemyRecipeView> alchemyRecipes() {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return List.of();
        return world.getRecipeManager().getSynchronizedRecipes().getAllOfType(AlchemyRecipe.TYPE)
                .stream().map(AlchemyRecipeView::new).toList();
    }
}
