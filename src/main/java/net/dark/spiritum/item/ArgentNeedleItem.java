package net.dark.spiritum.item;

import net.dark.spiritum.magic.InteractionEffects;
import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ArgentNeedleItem extends Item {
    public ArgentNeedleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack needle = user.getStackInHand(hand);
        ItemStack poppet =
                user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (!poppet.isOf(ModContent.VOODOO_POPPET)) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.SUCCESS;
        var bound = SpiritBinding.player(SpiritBinding.socket(poppet));
        var target =
                bound.map(id -> world.getServer().getPlayerManager().getPlayer(id)).orElse(null);
        if (target == null || !target.isAlive()) {
            user.sendMessage(Text.translatable("message.spiritum.target_unavailable"), true);
            return ActionResult.FAIL;
        }
        ServerWorld targetWorld = (ServerWorld) target.getEntityWorld();
        if (RingMagic.warded(target)) {
            user.sendMessage(Text.translatable("message.spiritum.target_warded"), true);
            return ActionResult.FAIL;
        }
        target.damage(targetWorld, targetWorld.getDamageSources().indirectMagic(user, user), 1f);
        PotionContentsComponent potion =
                needle.getOrDefault(
                        DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        for (StatusEffectInstance effect : potion.getEffects()) {
            if (effect.getEffectType().value().isInstant())
                effect.getEffectType()
                        .value()
                        .applyInstantEffect(
                                targetWorld, user, user, target, effect.getAmplifier(), 1.0);
            else
                target.addStatusEffect(
                        new StatusEffectInstance(
                                effect.getEffectType(),
                                effect.isInfinite() ? -1 : Math.max(1, effect.getDuration() / 4),
                                effect.getAmplifier(),
                                effect.isAmbient(),
                                effect.shouldShowParticles(),
                                effect.shouldShowIcon()),
                        user);
        }
        InteractionEffects.atHand(user, false);
        InteractionEffects.atHand(target, true);
        if (poppet.getDamage() + 1 >= poppet.getMaxDamage()) {
            ItemStack gem = SpiritBinding.socket(poppet);
            SpiritBinding.socket(poppet, ItemStack.EMPTY);
            user.getInventory().offerOrDrop(gem);
        }
        poppet.damage(1, user, hand == Hand.MAIN_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
        user.getItemCooldownManager().set(needle, 20);
        needle.decrement(1);
        user.sendMessage(
                Text.translatable("message.spiritum.voodoo_applied", target.getName()), true);
        return ActionResult.SUCCESS;
    }
}
