package net.dark.spiritum.client.compat;

import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.text.Text;

import java.util.*;

public record MagicRecipeView(OfferingRecipe recipe, boolean alchemy) {
    public List<ItemStack> inputs() {
        List<ItemStack> inputs = new ArrayList<>();
        recipe.ingredients().forEach((item, count) -> inputs.add(new ItemStack(item, count)));
        if (alchemy) inputs.add(new ItemStack(Items.WATER_BUCKET));
        else {
            inputs.add(new ItemStack(ModContent.HEXED_CANDLE, recipe.candles()));
            int ordinary =
                    RitualCandles.requiresOwner(recipe) ? recipe.candles() - 1 : recipe.candles();
            inputs.add(new ItemStack(ModContent.SPIRIT_FRAGMENT, ordinary));
            if (RitualCandles.requiresOwner(recipe)) {
                ItemStack bound = new ItemStack(ModContent.SPIRIT_GEM);
                bound.set(
                        DataComponentTypes.CUSTOM_NAME,
                        Text.translatable("viewer.spiritum.bound_gem"));
                inputs.add(bound);
            }
        }
        return inputs;
    }

    public ItemStack output() {
        if (alchemy) return recipe.outputStack();
        return recipe.id().equals("argentic_transmutation")
                ? new ItemStack(ModContent.ARGENT_NUGGET)
                : ItemStack.EMPTY;
    }

    public Text title() {
        return alchemy ? output().getName() : Text.translatable("ritual.spiritum." + recipe.id());
    }

    public List<Text> notes() {
        if (alchemy)
            return List.of(
                    Text.translatable("viewer.spiritum.boiling_water"),
                    Text.translatable("viewer.spiritum.thirty_seconds"));
        List<Text> notes = new ArrayList<>();
        if (RitualCandles.requiresOwner(recipe))
            notes.add(Text.translatable("viewer.spiritum.bound_candles", recipe.candles() - 1));
        else if (recipe.id().equals("withering"))
            notes.add(Text.translatable("viewer.spiritum.optional_candles"));
        else notes.add(Text.translatable("viewer.spiritum.candles", recipe.candles()));
        notes.add(
                Text.translatable(
                        recipe.persistent()
                                ? "viewer.spiritum.persistent"
                                : "viewer.spiritum.instant"));
        notes.add(Text.translatable("viewer.spiritum.effect." + recipe.id()));
        if (recipe.id().equals("calling"))
            notes.add(Text.translatable("viewer.spiritum.calling_movement"));
        else if (recipe.id().equals("withering"))
            notes.add(Text.translatable("viewer.spiritum.bound_protection"));
        return notes;
    }

    public static List<MagicRecipeView> rituals() {
        return MagicRecipes.RITUALS.stream().map(r -> new MagicRecipeView(r, false)).toList();
    }

    public static List<MagicRecipeView> alchemyRecipes() {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return List.of();
        return world.getRecipeManager().getSynchronizedRecipes().getAllOfType(AlchemyRecipe.TYPE)
                .stream().map(entry -> new MagicRecipeView(
                        entry.value().offering(entry.id().getValue().toString()), true)).toList();
    }
}
