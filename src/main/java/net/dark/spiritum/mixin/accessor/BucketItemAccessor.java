package net.dark.spiritum.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public interface BucketItemAccessor {
    @Invoker("playEmptyingSound")
    void spiritum$playEmptyingSound(LivingEntity user, WorldAccess world, BlockPos pos);
}
