package net.dark.spiritum.event.vanilla;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public interface ItemPreTooltipCallback {
    /**
     * Fired after the game has appended all base tooltip lines to the list.
     */
    Event<ItemPreTooltipCallback> BEFORE = EventFactory.createArrayBacked(ItemPreTooltipCallback.class, callbacks -> (stack, context, type, lines) -> {
        for (ItemPreTooltipCallback callback : callbacks) {
            callback.getTooltip(stack, context, type, lines);
        }
    });

    /**
     * Called when an item stack's tooltip is rendered. Text added to {@code lines} will be
     * rendered with the tooltip.
     *
     * @param lines the list containing the lines of text displayed on the stack's tooltip
     */
    void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines);
}
