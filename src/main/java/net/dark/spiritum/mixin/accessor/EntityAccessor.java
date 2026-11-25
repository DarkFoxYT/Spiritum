package net.dark.spiritum.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("collisionHandler")
    EntityCollisionHandler.Impl spiritum$getCollisionHandler();
}
