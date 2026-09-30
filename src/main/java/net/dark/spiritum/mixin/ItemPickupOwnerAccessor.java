package net.dark.spiritum.mixin;

import net.minecraft.entity.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ItemEntity.class)
public interface ItemPickupOwnerAccessor {
    @Accessor("owner")
    UUID spiritum$getPickupOwner();
}
