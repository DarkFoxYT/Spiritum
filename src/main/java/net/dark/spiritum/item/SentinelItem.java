package net.dark.spiritum.item;

import net.dark.spiritum.registry.ModEntities;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class SentinelItem extends Item {
    public SentinelItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld world)) return ActionResult.SUCCESS;
        var sentinel = ModEntities.SENTINEL.create(world, SpawnReason.TRIGGERED);
        if (sentinel == null) return ActionResult.FAIL;
        var pos = context.getBlockPos().offset(context.getSide());
        sentinel.refreshPositionAndAngles(
                pos.getX() + .5,
                pos.getY(),
                pos.getZ() + .5,
                context.getPlayer() == null ? 0 : context.getPlayer().getYaw(),
                0);
        if (!world.isSpaceEmpty(sentinel, sentinel.getBoundingBox())
                || !world.spawnEntity(sentinel)) return ActionResult.FAIL;
        if (context.getPlayer() == null || !context.getPlayer().isCreative())
            context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
