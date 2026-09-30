package net.dark.spiritum.item;

import net.dark.spiritum.magic.InteractionEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SpiritGemItem extends Item {
    public SpiritGemItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) return ActionResult.PASS;
        if (!world.isClient()) {
            var stack = user.getStackInHand(hand);
            if (stack.getCount() == 1) {
                SpiritBinding.bind(stack, user);
            } else {
                var gem = stack.split(1);
                SpiritBinding.bind(gem, user);
                user.getInventory().offerOrDrop(gem);
            }
            InteractionEffects.atHand(user, true);
        }
        return ActionResult.SUCCESS;
    }
}
