package net.dark.spiritum.item;

import net.dark.spiritum.magic.InteractionEffects;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class GemSocketItem extends Item {
    public GemSocketItem(Settings settings) {
        super(settings);
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);
        var weapon = stack.get(DataComponentTypes.WEAPON);
        if (weapon != null
                && stack.getDamage() + weapon.itemDamagePerAttack() >= stack.getMaxDamage()
                && !attacker.getEntityWorld().isClient()) {
            ItemStack gem = SpiritBinding.socket(stack);
            if (!gem.isEmpty()) {
                SpiritBinding.socket(stack, ItemStack.EMPTY);
                if (attacker instanceof PlayerEntity player) player.getInventory().offerOrDrop(gem);
                else
                    attacker.getEntityWorld()
                            .spawnEntity(
                                    new ItemEntity(
                                            attacker.getEntityWorld(),
                                            attacker.getX(),
                                            attacker.getY(),
                                            attacker.getZ(),
                                            gem));
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack holder = user.getStackInHand(hand);
        ItemStack gem = SpiritBinding.socket(holder);
        if (user.isSneaking() && !gem.isEmpty()) {
            if (!world.isClient()) {
                SpiritBinding.socket(holder, ItemStack.EMPTY);
                InteractionEffects.atHand(user, false);
                user.getInventory().offerOrDrop(gem);
                user.sendMessage(Text.translatable("message.spiritum.gem_removed"), true);
            }
            return ActionResult.SUCCESS;
        }
        ItemStack other =
                user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (!gem.isEmpty() || !other.isOf(ModContent.SPIRIT_GEM)) return ActionResult.PASS;
        if (!world.isClient()) {
            SpiritBinding.socket(holder, other);
            InteractionEffects.atHand(user, true);
            if (!user.isCreative()) other.decrement(1);
            user.sendMessage(Text.translatable("message.spiritum.gem_inserted"), true);
        }
        return ActionResult.SUCCESS;
    }
}
