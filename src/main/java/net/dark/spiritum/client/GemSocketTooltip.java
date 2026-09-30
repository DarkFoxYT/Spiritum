package net.dark.spiritum.client;

import net.dark.spiritum.item.GemSocketTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public record GemSocketTooltip(GemSocketTooltipData data) implements TooltipComponent {
    @Override public int getHeight(TextRenderer font) { return 22; }
    @Override public int getWidth(TextRenderer font) { return 22; }
    @Override
    public void drawItems(TextRenderer font, int x, int y, int width, int height, DrawContext context) {
        context.fill(x, y, x + 20, y + 20, 0xFF6C5882);
        context.fill(x + 1, y + 1, x + 19, y + 19, 0xFF20152B);
        if (!data.gem().isEmpty()) {
            context.drawItem(data.gem(), x + 2, y + 2);
            context.drawStackOverlay(font, data.gem(), x + 2, y + 2);
        }
    }
}
