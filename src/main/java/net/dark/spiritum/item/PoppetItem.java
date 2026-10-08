package net.dark.spiritum.item;

import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.registry.ModEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PoppetItem extends GemSocketItem {
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (SpiritBinding.socket(stack).isEmpty()) return super.use(world, user, hand);
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return true;
    }

    public static float throwSpeed(int chargeTicks) {
        return .25f + .65f * Math.min(1, Math.max(0, chargeTicks) / 20f);
    }

    @Override
    public boolean onStoppedUsing(
            ItemStack stack, World world, LivingEntity entity, int remainingUseTicks) {
        if (world.isClient()
                || !(entity instanceof PlayerEntity user)
                || SpiritBinding.socket(stack).isEmpty()) return false;
        float speed = throwSpeed(getMaxUseTime(stack, user) - remainingUseTicks);
        var doll = ModEntities.POPPET.create(world, SpawnReason.TRIGGERED);
        if (doll == null) return false;
        var direction = user.getRotationVec(1);
        var pos = user.getEyePos().add(direction.multiply(.6));
        doll.setPosition(pos);
        doll.launch(stack, user, direction.multiply(speed).add(user.getVelocity().multiply(.3)));
        if (!world.isSpaceEmpty(doll, doll.getBoundingBox()) || !world.spawnEntity(doll))
            return false;
        var target =
                SpiritBinding.player(SpiritBinding.socket(stack))
                        .map(id -> world.getServer().getPlayerManager().getPlayer(id))
                        .orElse(null);
        if (target != null && target.isAlive() && !RingMagic.warded(target)) {
            target.addVelocity(direction.multiply(speed * .45));
            target.velocityDirty = true;
            target.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
        }
        user.getItemCooldownManager().set(stack, 10);
        stack.decrement(1);
        return true;
    }

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
