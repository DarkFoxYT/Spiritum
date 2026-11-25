package net.dark.spiritum.client.compat;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import net.dark.spiritum.Spiritum;
import net.dark.spiritum.client.compat.emi.VatLiquidEmiStack;
import net.dark.spiritum.magic.MagicRecipes;
import net.dark.spiritum.magic.OfferingRecipe;
import net.dark.spiritum.registry.SpiritumContent;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.VatLiquid;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.dynamic.Range;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class SpiritumEmiPlugin implements EmiPlugin {


    public static final EmiRecipeCategory RITUAL
        = new EmiRecipeCategory(Spiritum.id("ritual"), EmiStack.of(SpiritumContent.RITUAL_PEDESTAL), simplifiedRenderer(190, 150));

    public static final EmiRecipeCategory ALCHEMY
        = new EmiRecipeCategory(Spiritum.id("alchemy"), EmiStack.of(SpiritumContent.ALCHEMY_VAT), simplifiedRenderer(240, 120));

    @Override
    public void initialize(EmiInitRegistry registry) {
        EmiPlugin.super.initialize(registry);

        registry.addRegistryAdapter(EmiRegistryAdapter.simple(
            VatLiquid.class,
            (Registry<VatLiquid>) (Object) SpiritumRegistries.VAT_LIQUID,
            SpiritumEmiPlugin::of)
        );
    }

    @Override
    public void register(EmiRegistry registry) {
        for (Fluid fluid : SpiritumRegistries.VAT_LIQUID.stream().toList()) {
            if (Registries.FLUID.get(SpiritumRegistries.VAT_LIQUID.getId(fluid)) == Fluids.EMPTY && fluid instanceof VatLiquid liquid)
                registry.addEmiStack(of(liquid));
        }

        registry.addCategory(RITUAL);
        registry.addCategory(ALCHEMY);

        registry.addWorkstation(RITUAL, EmiIngredient.of(List.of(EmiStack.of(SpiritumContent.RITUAL_PEDESTAL),EmiStack.of(SpiritumContent.HEXED_CANDLE))));
        registry.addWorkstation(ALCHEMY, EmiStack.of(SpiritumContent.ALCHEMY_VAT));

        for (OfferingRecipe ritual : MagicRecipes.RITUALS)
            registry.addRecipe(new OfferingEmiRecipe(ritual));
    }


    public static EmiStack of(VatLiquid fluid) {
        return of(fluid, EmiPort.emptyExtraData());
    }
    public static EmiStack of(VatLiquid fluid, long amount) {
        return of(fluid, EmiPort.emptyExtraData(), amount);
    }

    public static EmiStack of(VatLiquid fluid, ComponentChanges componentChanges) {
        return of(fluid, componentChanges, 0);
    }

    public static EmiStack of(VatLiquid fluid, ComponentChanges componentChanges, long amount) {
        if (fluid == Fluids.EMPTY) {
            return EmiStack.EMPTY;
        }
        return new VatLiquidEmiStack(fluid, componentChanges, amount);
    }

    private static EmiRenderable simplifiedRenderer(int u, int v) {
        return (raw, x, y, delta) -> {
            EmiDrawContext context = EmiDrawContext.wrap(raw);
            context.drawTexture(EmiRenderHelper.WIDGETS, x, y, u, v, 16, 16);
        };
    }

    public static class OfferingEmiRecipe implements EmiRecipe {
        private final Identifier id;
        private final List<Text> notes;
        private final List<EmiIngredient> input;
        private final List<EmiStack> output;
        private final List<EmiStack> candles;
        private final Range<Integer> ownerRange;

        protected final int unique = EmiUtil.RANDOM.nextInt();

        public OfferingEmiRecipe(OfferingRecipe recipe) {
            this.id = Identifier.of(recipe.id().getNamespace(),"/"+recipe.id().getPath());
            this.notes = new OfferingRecipeView(recipe).notes();
            this.input = recipe.ingredients().entrySet().stream()
                .map(entry -> EmiIngredient.of(Ingredient.ofItem(entry.getKey()),entry.getValue()))
                .toList();

            this.output = List.of(EmiStack.of(recipe.result()));

            this.candles = new OfferingRecipeView(recipe).spirits().stream().map(EmiStack::of).toList();
            this.ownerRange = recipe.ownerCandles();
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return RITUAL;
        }

        @Override
        public Identifier getId() {
            return id;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            var inputs = new ArrayList<>(input);
            inputs.addAll(candles.stream().map(s -> EmiIngredient.of(Ingredient.ofItem(s.getItemStack().getItem()))).toList());
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return output;
        }

        @Override
        public int getDisplayWidth() {
            return 190;
        }

        @Override
        public int getDisplayHeight() {
            return 150;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            var font = MinecraftClient.getInstance().textRenderer;
            widgets.addText(Text.translatable("ritual.spiritum." + id.getPath().replace("/","")),
                2, 2, 0xffFFFFFF, true);

            for (int i = 0; i < input.size(); i++)
                widgets.addSlot(input.get(i), 4 + (i % 4) * 24, 12+(i / 4) * 24).drawBack(false);

            var length = 190-24;

            if (candles.size() == 1) {
                widgets.addSlot(candles.getFirst(), 4, 46).catalyst(true).drawBack(false);
                widgets.add(new CandleWidget(1, 54));
            } else
                for (int i = 0; i < candles.size(); i++) {
                    int x = 4 + Math.round(i * length / (7f));
                    widgets.add(new CandleWidget(x-3, 54));
                    if (candles.get(i).getItemStack().isOf(SpiritumContent.SPIRIT_GEM) && ownerRange.minInclusive() != ownerRange.maxInclusive()) {
                        int finalI = i;
                        widgets.addGeneratedSlot((r)->
                            r.nextInt(ownerRange.minInclusive(),ownerRange.maxInclusive()+1) > finalI-(candles.size()-ownerRange.maxInclusive()) ?
                                candles.get(finalI) :
                                EmiStack.EMPTY
                        ,unique, x, 46).catalyst(true).drawBack(false);
                    } else
                        widgets.addSlot(candles.get(i), x, 46).catalyst(true).drawBack(false);
                }

            if (output.stream().noneMatch(EmiStack::isEmpty)) {
                widgets.addTexture(EmiTexture.EMPTY_ARROW, Math.round(24*5), 24);
                widgets.addSlot(output.getFirst(), Math.round(24*6.5f), 20).large(true).recipeContext(this);
            }else
                widgets.addSlot(EmiStack.EMPTY, Math.round(24*6.5f), 20).drawBack(false).recipeContext(this);

            int y = 76;
            for (Text note : notes) {
                for (var line : font.wrapLines(note, 186)) {
                    widgets.addText(line, 2, y, 0xff555555, false);
                    y += 10;
                }
            }
        }
        public static class CandleWidget extends Widget {

            private final int x;
            private final int y;

            public CandleWidget(int x, int y) {
                super();
                this.x = x;
                this.y = y;
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

            @Override
            public Bounds getBounds() {
                int size = Math.round(16*1.5f);
                return new Bounds(this.x,this.y,this.x+size,this.y+size);
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                drawCandle(this.x, this.y, context);
            }
        }
    }
}
