package net.dark.spiritum.item;

import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.registry.ModEntities;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PoppetItem extends GemSocketItem {
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (SpiritBinding.socket(stack).isEmpty()) return super.use(world, user, hand);
        if (world.isClient()) return ActionResult.SUCCESS;
        var doll = ModEntities.POPPET.create(world, SpawnReason.TRIGGERED);
        if (doll == null) return ActionResult.FAIL;
        var direction = user.getRotationVec(1);
        var pos = user.getEyePos().add(direction.multiply(.6));
        doll.setPosition(pos);
        doll.launch(stack, user, direction.multiply(1.4).add(user.getVelocity()));
        if (!world.isSpaceEmpty(doll, doll.getBoundingBox()) || !world.spawnEntity(doll))
            return ActionResult.FAIL;
        var target =
                SpiritBinding.player(SpiritBinding.socket(stack))
                        .map(id -> world.getServer().getPlayerManager().getPlayer(id))
                        .orElse(null);
        if (target != null && target.isAlive() && !RingMagic.warded(target)) {
            target.addVelocity(direction.multiply(.8));
            target.velocityDirty = true;
            target.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
        }
        stack.decrement(1);
        user.getItemCooldownManager().set(stack, 10);
        return ActionResult.SUCCESS;
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
