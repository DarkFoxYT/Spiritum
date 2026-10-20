package net.dark.spiritum.item;

import net.minecraft.item.ItemStack;

public class RetrosonusItem extends TunedBladeItem {
    public RetrosonusItem(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean acceptsGem(ItemStack stack) {
        return stack.isOf(net.dark.spiritum.registry.ModContent.SPIRIT_GEM);
    }
}
