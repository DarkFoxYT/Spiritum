package net.dark.spiritum.client.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.platform.Services;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidStackListFactory;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.dark.spiritum.registry.SpiritumContent;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.LiquidPotion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.joml.Matrix3x2f;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public final class SpiritumJeiPlugin implements IModPlugin {
    @SuppressWarnings("unused")
    private static mezz.jei.api.runtime.IJeiRuntime runtime;


    public static final class RecipeHolderTypes {
        public static final IRecipeHolderType<AlchemyRecipe> ALCHEMY =
            IRecipeHolderType.create(AlchemyRecipe.TYPE);
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime available) {
        runtime = available;
        if (FabricLoader.getInstance().isModLoaded("emi")) return;
        hideUpgradeRecipe(
                available.getRecipeManager(),
                mezz.jei.api.constants.RecipeTypes.CRAFTING,
                "exalted_ingot");
        hideUpgradeRecipe(
                available.getRecipeManager(),
                RecipeHolderTypes.ALCHEMY,
                "alchemy/exalted_ingot");
        hideUpgradeRecipe(
                available.getRecipeManager(),
                mezz.jei.api.constants.RecipeTypes.SMITHING,
                "retrosonus");
    }

    private static <T extends net.minecraft.recipe.Recipe<?>> void hideUpgradeRecipe(
            mezz.jei.api.recipe.IRecipeManager manager,
            mezz.jei.api.recipe.types.IRecipeHolderType<T> type,
            String id) {
        var recipes =
                manager.createRecipeLookup(type)
                        .includeHidden()
                        .get()
                        .filter(recipe -> recipe.id().getValue().equals(SpiritumContent.id(id)))
                        .toList();
        manager.hideRecipes(type, recipes);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static final IRecipeType<OfferingRecipeView> RITUALS =
            IRecipeType.create("spiritum", "rituals", OfferingRecipeView.class);
    public static final IRecipeType<AlchemyRecipeView> ALCHEMY =
            IRecipeType.create("spiritum", "alchemy", AlchemyRecipeView.class);

    @Override
    public Identifier getPluginUid() {
        return SpiritumContent.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new OfferingCategory(), new AlchemyCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (FabricLoader.getInstance().isModLoaded("emi")) return;
        registration.addRecipes(RITUALS, OfferingRecipeView.rituals());
        registration.addRecipes(ALCHEMY, AlchemyRecipeView.alchemyRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (FabricLoader.getInstance().isModLoaded("emi")) return;
        registration.addCraftingStation(RITUALS, new ItemStack(SpiritumContent.RITUAL_PEDESTAL), new ItemStack(SpiritumContent.HEXED_CANDLE));
        registration.addCraftingStation(ALCHEMY, new ItemStack(SpiritumContent.ALCHEMY_VAT));
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        IModPlugin.super.registerExtraIngredients(registration);
        registration.addExtraIngredients(FabricTypes.FLUID_STACK,
            (List<IJeiFluidIngredient>)FluidStackListFactory.create(SpiritumRegistries.VAT_LIQUID,Services.PLATFORM.getFluidHelper())
        );
    }

    private record OfferingCategory() implements IRecipeCategory<OfferingRecipeView> {

        @Override
        public IRecipeType<OfferingRecipeView> getRecipeType() {
                return RITUALS;
        }

        @Override
        public Text getTitle() {
            return Text.translatable("viewer.spiritum.rituals");
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
        public Identifier getIdentifier(OfferingRecipeView recipe) {
            //if (IRecipeCategory.super.getIdentifier(recipe) != null)
            //return IRecipeCategory.super.getIdentifier(recipe);
            return recipe.recipe().id();
        }

        @Override
        public void setRecipe(
            IRecipeLayoutBuilder builder, OfferingRecipeView view, IFocusGroup focuses) {
            var inputs = view.inputs();
            for (int i = 0; i < inputs.size(); i++)
                builder.addSlot(RecipeIngredientRole.INPUT, 4 + (i % 4) * 24, 22 + (i / 4) * 24)
                    .addItemStacks(List.of(inputs.get(i)));

            var length = view.output().isEmpty() ? getWidth()-24 : 96;
            var spirits = view.spirits();
            if (spirits.size() == 1)
                builder.addSlot(RecipeIngredientRole.INPUT, 4, 46)
                    .addItemStacks(List.of(spirits.getFirst()));
            else
                for (int i = 0; i < spirits.size(); i++) {
                    if (spirits.get(i).isOf(SpiritumContent.SPIRIT_GEM) && view.recipe().ownerCandles().minInclusive() != view.recipe().ownerCandles().maxInclusive()) {
                        builder.addSlot(RecipeIngredientRole.INPUT, 4 + Math.round(i * length / (7f)), 46)
                            .add(spirits.get(i));
                    } else
                        builder.addSlot(RecipeIngredientRole.INPUT, 4 + Math.round(i * length / (7f)), 46)
                            .addItemStacks(List.of(spirits.get(i),ItemStack.EMPTY));
                }

            if (!view.output().isEmpty())
                builder.addSlot(RecipeIngredientRole.OUTPUT, 160, 34)
                    .addItemStacks(List.of(view.output()));
        }

        @Override
        public void draw(
            OfferingRecipeView view,
            IRecipeSlotsView slots,
            DrawContext draw,
            double mouseX,
            double mouseY
        )
        {
            var font = MinecraftClient.getInstance().textRenderer;
            draw.drawText(font, view.title(), 2, 2, 0xff555555, false);

            var spirits = view.spirits();
            var length = view.output().isEmpty() ? getWidth()-24 : 96;
            if (spirits.isEmpty()); // skips if empty
            else if (spirits.size() == 1)
                drawCandle(0, 53, draw);
            else
                for (int i = 0; i < spirits.size(); i++)
                        drawCandle(Math.round(i * length / (7f)), 53, draw);

            if (!view.output().isEmpty()) draw.drawText(font, "→", 130, 38, 0xff555555, false);
            int y = 76;
            for (Text note : view.notes()) {
                for (var line : font.wrapLines(note, 186)) {
                    draw.drawText(font, line, 2, y, 0xff555555, false);
                    y += 10;
                }
            }
        }

        private static void drawCandle(int x, int y, DrawContext draw) {
            var stack = SpiritumContent.HEXED_CANDLE.asItem().getDefaultStack();
            if (!stack.isEmpty()) {
                KeyedItemRenderState keyedItemRenderState = new KeyedItemRenderState();
                MinecraftClient.getInstance().getItemModelManager().clearAndUpdate(keyedItemRenderState, stack, ItemDisplayContext.GUI,
                    MinecraftClient.getInstance().world,
                    MinecraftClient.getInstance().player,
                    0
                );

                try {
                    draw.state.addItem(new ItemGuiElementRenderState(stack.getItem().getName().toString(),
                        new Matrix3x2f(draw.getMatrices()).scale(1.5f), keyedItemRenderState, Math.round(x / 1.5f), Math.round(y / 1.5f), draw.scissorStack.peekLast()));
                } catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.create(throwable, "Rendering item");
                    CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                    crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                    crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                    crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                    throw new CrashException(crashReport);
                }
            }
        }
    }

    private record AlchemyCategory() implements IRecipeCategory<AlchemyRecipeView> {

        @Override
        public IRecipeType<AlchemyRecipeView> getRecipeType() {
                return ALCHEMY;
        }

        @Override
        public Text getTitle() {
            return Text.translatable("viewer.spiritum.alchemy");
        }

        @Override
        public int getWidth() {
                return 190;
        }

        @Override
        public int getHeight() {
            return 86;
        }

        @Override
        public IDrawable getIcon() {
            return null;
        }

        @Override
        public Identifier getIdentifier(AlchemyRecipeView recipe) {
            return recipe.recipe().id().getValue();
        }

        @Override
        public void setRecipe(
            IRecipeLayoutBuilder builder, AlchemyRecipeView view, IFocusGroup focuses) {
            var inputs = view.inputs();
            for (int i = 0; i < inputs.size(); i++)
                builder.addSlot(RecipeIngredientRole.INPUT, 4 + (i % 4) * 24, 22 + (i / 4) * 24)
                    .addItemStacks(List.of(inputs.get(i)));

            if (view.fluid() != Fluids.EMPTY) {
                builder.addSlot(RecipeIngredientRole.INPUT, 4, 46)
                    .setFluidRenderer(Services.PLATFORM.getFluidHelper().bucketVolume(), false, 16, 16)
                    .add(view.fluid());
                if (view.fluid().getBucketItem() != null && !view.fluid().getBucketItem().getDefaultStack().isEmpty())
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                        .add(view.fluid().getBucketItem());
                else if (view.fluid() instanceof LiquidPotion liquidPotion)
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                        .add(PotionContentsComponent.createStack(Items.POTION, Optional.ofNullable(LiquidPotion.ofFluid(liquidPotion)).orElse(Potions.WATER)));
            }

            if (!view.output().isEmpty())
                builder.addSlot(RecipeIngredientRole.OUTPUT, 160, 34)
                    .setOutputSlotBackground()
                    .addItemStacks(List.of(view.output()));
        }

        @Override
        public void createRecipeExtras(IRecipeExtrasBuilder builder, AlchemyRecipeView view, IFocusGroup focuses) {
            IRecipeCategory.super.createRecipeExtras(builder, view, focuses);
            if (!view.output().isEmpty())
                builder.addAnimatedRecipeArrow(view.recipe().value().duration())
                    .setPosition(120, 34);
        }

        @Override
        public void draw(
            AlchemyRecipeView view,
            IRecipeSlotsView slots,
            DrawContext draw,
            double mouseX,
            double mouseY
        )
        {
            var font = MinecraftClient.getInstance().textRenderer;
            draw.drawText(font, view.title(), 2, 2, 0xff555555, false);

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
