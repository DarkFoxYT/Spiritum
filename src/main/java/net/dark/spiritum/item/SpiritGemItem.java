package net.dark.spiritum.item;

import net.dark.spiritum.magic.InteractionEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
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
            SpiritBinding.bind(user.getStackInHand(hand), user);
            InteractionEffects.atHand(user, true);
            user.sendMessage(Text.translatable("message.spiritum.gem_bound", user.getName()), true);
        }
        return ActionResult.SUCCESS;
    }
}
