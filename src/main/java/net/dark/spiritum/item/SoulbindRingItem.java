package net.dark.spiritum.item;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SoulbindRingItem extends GemSocketItem {
    public SoulbindRingItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack ring = user.getStackInHand(hand);
        ItemStack other =
                user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (SpiritBinding.socket(ring).isEmpty()
                && other.isOf(ModContent.SPIRIT_GEM)
                && SpiritBinding.player(other).isEmpty()) {
            if (!world.isClient())
                user.sendMessage(Text.translatable("message.spiritum.bound_gem_required"), true);
            return ActionResult.FAIL;
        }
        return super.use(world, user, hand);
    }
}
