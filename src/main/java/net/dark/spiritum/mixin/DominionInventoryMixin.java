package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.Dominion;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Also revokes menus that were opened before the protection started. */
@Mixin(Inventory.class)
public interface DominionInventoryMixin {
    @Inject(
            method =
                    "canPlayerUse(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/player/PlayerEntity;F)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void spiritum$storage(
            BlockEntity block,
            PlayerEntity player,
            float range,
            CallbackInfoReturnable<Boolean> cir) {
        if (block.getWorld() != null
                && Dominion.restricted(block.getWorld(), block.getPos(), player))
            cir.setReturnValue(false);
    }
}
