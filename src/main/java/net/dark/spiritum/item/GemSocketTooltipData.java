package net.dark.spiritum.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;

public record GemSocketTooltipData(ItemStack gem) implements TooltipData {}
