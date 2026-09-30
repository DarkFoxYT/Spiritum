package net.dark.spiritum.recipe;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class NeedleCoatingRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<NeedleCoatingRecipe> SERIALIZER =
            new SpecialRecipeSerializer<>(NeedleCoatingRecipe::new);

    public NeedleCoatingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    public static void initialize() {
        Registry.register(
                Registries.RECIPE_SERIALIZER, ModContent.id("needle_coating"), SERIALIZER);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int needles = 0, potions = 0;
        for (ItemStack stack : input.getStacks()) {
            if (stack.isEmpty()) continue;
            if (stack.isOf(ModContent.ARGENT_NEEDLE)
                    && !stack.contains(DataComponentTypes.POTION_CONTENTS)) needles++;
            else if ((stack.isOf(Items.POTION)
                            || stack.isOf(Items.SPLASH_POTION)
                            || stack.isOf(Items.LINGERING_POTION))
                    && stack.contains(DataComponentTypes.POTION_CONTENTS)) potions++;
            else return false;
        }
        return needles == 1 && potions == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack needle = ItemStack.EMPTY, potion = ItemStack.EMPTY;
        for (ItemStack stack : input.getStacks()) {
            if (stack.isOf(ModContent.ARGENT_NEEDLE)) needle = stack.copyWithCount(1);
            else if (!stack.isEmpty()) potion = stack;
        }
        if (needle.isEmpty() || !potion.contains(DataComponentTypes.POTION_CONTENTS))
            return ItemStack.EMPTY;
        needle.set(
                DataComponentTypes.POTION_CONTENTS, potion.get(DataComponentTypes.POTION_CONTENTS));
        needle.set(DataComponentTypes.POTION_DURATION_SCALE, .25f);
        return needle;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++)
            if (input.getStackInSlot(i).isOf(Items.POTION)
                    || input.getStackInSlot(i).isOf(Items.SPLASH_POTION)
                    || input.getStackInSlot(i).isOf(Items.LINGERING_POTION))
                remainders.set(i, new ItemStack(Items.GLASS_BOTTLE));
        return remainders;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
