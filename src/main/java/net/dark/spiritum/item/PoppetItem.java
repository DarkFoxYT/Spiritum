package net.dark.spiritum.item;

import net.minecraft.item.ItemStack;

public class PoppetItem extends GemSocketItem {
    public PoppetItem(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean acceptsGem(ItemStack stack) {
        return super.acceptsGem(stack) && SpiritBinding.player(stack).isPresent();
    }

    @Override
    protected boolean canRemoveGem() {
        return false;
    }
}
