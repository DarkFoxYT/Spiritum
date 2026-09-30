package net.dark.spiritum.client.compat;

import mezz.jei.api.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

@JeiPlugin
public final class SpiritumJeiPlugin implements IModPlugin {
    private static mezz.jei.api.runtime.IJeiRuntime runtime;

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime available) {
        runtime = available;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static final IRecipeType<MagicRecipeView> RITUALS =
            IRecipeType.create("spiritum", "rituals", MagicRecipeView.class);
    public static final IRecipeType<MagicRecipeView> ALCHEMY =
            IRecipeType.create("spiritum", "alchemy", MagicRecipeView.class);

    @Override
    public Identifier getPluginUid() {
        return ModContent.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(false), new Category(true));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RITUALS, MagicRecipeView.rituals());
        registration.addRecipes(ALCHEMY, MagicRecipeView.alchemyRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RITUALS, new ItemStack(ModContent.RITUAL_PEDESTAL));
        registration.addCraftingStation(ALCHEMY, new ItemStack(ModContent.ALCHEMY_VAT));
    }

    private static final class Category implements IRecipeCategory<MagicRecipeView> {
        private final boolean alchemy;

        Category(boolean alchemy) {
            this.alchemy = alchemy;
        }

        @Override
        public IRecipeType<MagicRecipeView> getRecipeType() {
            return alchemy ? ALCHEMY : RITUALS;
        }

        @Override
        public Text getTitle() {
            return Text.translatable(
                    alchemy ? "viewer.spiritum.alchemy" : "viewer.spiritum.rituals");
        }

        @Override
        public int getWidth() {
            return 190;
        }

        @Override
        public int getHeight() {
            return 150;
        }

        @Override
        public IDrawable getIcon() {
            return null;
        }

        @Override
        public void setRecipe(
                IRecipeLayoutBuilder builder, MagicRecipeView view, IFocusGroup focuses) {
            var inputs = view.inputs();
            for (int i = 0; i < inputs.size(); i++)
                builder.addSlot(RecipeIngredientRole.INPUT, 4 + (i % 4) * 24, 22 + (i / 4) * 24)
                        .addItemStacks(List.of(inputs.get(i)));
            if (!view.output().isEmpty())
                builder.addSlot(RecipeIngredientRole.OUTPUT, 160, 34)
                        .addItemStacks(List.of(view.output()));
        }

        @Override
        public void draw(
                MagicRecipeView view,
                IRecipeSlotsView slots,
                DrawContext draw,
                double mouseX,
                double mouseY) {
            var font = MinecraftClient.getInstance().textRenderer;
            draw.drawText(font, view.title(), 2, 2, 0xff555555, false);
            if (!view.output().isEmpty()) draw.drawText(font, "→", 130, 38, 0xff555555, false);
            int y = 76;
            for (Text note : view.notes()) {
                for (var line : font.wrapLines(note, 186)) {
                    draw.drawText(font, line, 2, y, 0xff555555, false);
                    y += 10;
                }
            }
        }
    }
}
