package net.dark.spiritum.client.compat;

import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.*;
import me.shedaniel.rei.api.client.registry.display.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.util.*;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public final class SpiritumReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<MagicDisplay> RITUALS =
            CategoryIdentifier.of("spiritum", "rituals");
    public static final CategoryIdentifier<MagicDisplay> ALCHEMY =
            CategoryIdentifier.of("spiritum", "alchemy");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new Category(false));
        registry.add(new Category(true));
        registry.addWorkstations(RITUALS, EntryStacks.of(ModContent.RITUAL_PEDESTAL));
        registry.addWorkstations(ALCHEMY, EntryStacks.of(ModContent.ALCHEMY_VAT));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        MagicRecipeView.rituals().forEach(view -> registry.add(new MagicDisplay(view)));
        MagicRecipeView.alchemyRecipes().forEach(view -> registry.add(new MagicDisplay(view)));
    }

    public record MagicDisplay(MagicRecipeView view) implements Display {
        @Override
        public List<EntryIngredient> getInputEntries() {
            return view.inputs().stream().map(EntryIngredients::of).toList();
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return view.output().isEmpty()
                    ? List.of()
                    : List.of(EntryIngredients.of(view.output()));
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return view.alchemy() ? ALCHEMY : RITUALS;
        }

        @Override
        public Optional<Identifier> getDisplayLocation() {
            return Optional.of(
                    view.alchemy() ? Identifier.of(view.recipe().id())
                            : ModContent.id("ritual/" + view.recipe().id()));
        }

        @Override
        public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends Display>
                getSerializer() {
            return null;
        }
    }

    private static final class Category implements DisplayCategory<MagicDisplay> {
        private final boolean alchemy;

        Category(boolean alchemy) {
            this.alchemy = alchemy;
        }

        @Override
        public CategoryIdentifier<? extends MagicDisplay> getCategoryIdentifier() {
            return alchemy ? ALCHEMY : RITUALS;
        }

        @Override
        public Text getTitle() {
            return Text.translatable(
                    alchemy ? "viewer.spiritum.alchemy" : "viewer.spiritum.rituals");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(alchemy ? ModContent.ALCHEMY_VAT : ModContent.RITUAL_PEDESTAL);
        }

        @Override
        public int getDisplayHeight() {
            return 158;
        }

        @Override
        public int getDisplayWidth(MagicDisplay display) {
            return 202;
        }

        @Override
        public List<Widget> setupDisplay(MagicDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            widgets.add(
                    Widgets.createLabel(
                                    new Point(bounds.x + 6, bounds.y + 6), display.view().title())
                            .leftAligned()
                            .noShadow()
                            .color(0xff555555, 0xffcccccc));
            var inputs = display.getInputEntries();
            for (int i = 0; i < inputs.size(); i++)
                widgets.add(
                        Widgets.createSlot(
                                        new Point(
                                                bounds.x + 10 + (i % 4) * 24,
                                                bounds.y + 28 + (i / 4) * 24))
                                .entries(inputs.get(i))
                                .markInput());
            if (!display.view().output().isEmpty()) {
                widgets.add(Widgets.createArrow(new Point(bounds.x + 132, bounds.y + 40)));
                widgets.add(
                        Widgets.createSlot(new Point(bounds.x + 166, bounds.y + 40))
                                .entries(display.getOutputEntries().getFirst())
                                .markOutput());
            }
            widgets.add(
                    Widgets.createDrawableWidget(
                            (draw, mouseX, mouseY, delta) -> {
                                var font = MinecraftClient.getInstance().textRenderer;
                                int y = bounds.y + 82;
                                for (Text note : display.view().notes()) {
                                    for (var line : font.wrapLines(note, 190)) {
                                        draw.drawText(
                                                font, line, bounds.x + 6, y, 0xff555555, false);
                                        y += 10;
                                    }
                                }
                            }));
            return widgets;
        }
    }
}
