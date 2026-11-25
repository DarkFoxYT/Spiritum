package net.dark.spiritum.client.compat;

import net.dark.spiritum.magic.MagicRecipes;
import net.dark.spiritum.magic.OfferingRecipe;
import net.dark.spiritum.registry.SpiritumContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OfferingRecipeView(OfferingRecipe recipe) {
    public List<ItemStack> inputs() {
        List<ItemStack> inputs = new ArrayList<>();
        recipe.ingredients().forEach((item, count) -> inputs.add(new ItemStack(item, count)));
        return inputs;
    }

    public List<ItemStack> spirits() {
        List<ItemStack> inputs = new ArrayList<>();
        int count = recipe.ownerCandles().maxInclusive();

        for (int i = 0; i < recipe.candles()-recipe.ownerCandles().minInclusive(); i++)
            inputs.add(new ItemStack(SpiritumContent.SPIRIT_FRAGMENT));

        if (count>0) {
            for (int i = 0; i < count; i++) {
                ItemStack gem = new ItemStack(SpiritumContent.SPIRIT_GEM);
                NbtComponent.set(
                    DataComponentTypes.CUSTOM_DATA,
                    gem,
                    nbt -> {
                        nbt.putString("SpiritumPlayer", UUID.randomUUID().toString());
                        nbt.putString("SpiritumPlayerName", "Desired Player");
                    });
                gem.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

                inputs.add(gem);
            }
        }

        return inputs;
    }

    public ItemStack output() {
        return recipe.outputStack();
    }

    public Text title() {
        return Text.translatable(recipe.id().toTranslationKey("ritual"));
    }

    public List<Text> notes() {
        List<Text> notes = new ArrayList<>();
        if (recipe.ownerCandles().minInclusive() == recipe.ownerCandles().maxInclusive() && recipe.ownerCandles().minInclusive() != 0)
            notes.add(Text.translatable("viewer.spiritum.bound_candles", recipe.candles()-recipe.ownerCandles().minInclusive(),recipe.ownerCandles().minInclusive()));
        else if (recipe.ownerCandles().maxInclusive()>0)
            notes.add(Text.translatable("viewer.spiritum.optional_candles", recipe.candles()-recipe.ownerCandles().minInclusive(),recipe.ownerCandles().minInclusive(),recipe.ownerCandles().maxInclusive()));
        else notes.add(Text.translatable("viewer.spiritum.candles", recipe.candles()));
        notes.add(
                Text.translatable(
                        recipe.persistent()
                                ? "viewer.spiritum.persistent"
                                : "viewer.spiritum.instant"));
        notes.add(Text.translatable("viewer.spiritum.effect." + recipe.id().getPath()));
        if (recipe.id().getPath().endsWith("_binding"))
            notes.add(Text.translatable("viewer.spiritum.binding_return"));
        else if (recipe.id().getPath().equals("withering"))
            notes.add(Text.translatable("viewer.spiritum.bound_protection"));
        return notes;
    }

    public static List<OfferingRecipeView> rituals() {
        return MagicRecipes.RITUALS.stream().map(OfferingRecipeView::new).toList();
    }
}
