package net.dark.spiritum.item;

import net.dark.spiritum.registry.SpiritumContent;
import net.minecraft.item.ItemStack;

public class RetrosonusItem extends TunedBladeItem {
    public RetrosonusItem(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean acceptsGem(ItemStack stack) {
        return stack.isOf(SpiritumContent.SPIRIT_GEM);
    }
}
