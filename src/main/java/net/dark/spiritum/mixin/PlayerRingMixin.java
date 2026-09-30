package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.world.rule.GameRules;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerRingMixin implements RingBearer {
    @Unique private ItemStack spiritum$ring = ItemStack.EMPTY;

    public ItemStack spiritum$getRing() {
        return spiritum$ring == null ? ItemStack.EMPTY : spiritum$ring;
    }

    public void spiritum$setRing(ItemStack stack) {
        spiritum$ring = stack;
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void spiritum$save(WriteView view, CallbackInfo ci) {
        view.put("SpiritumRing", ItemStack.OPTIONAL_CODEC, spiritum$getRing());
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void spiritum$load(ReadView view, CallbackInfo ci) {
        ItemStack saved =
                view.read("SpiritumRing", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        spiritum$ring = RingMagic.isRing(saved) ? saved.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void spiritum$drop(ServerWorld world, CallbackInfo ci) {
        if (!world.getGameRules().getValue(GameRules.KEEP_INVENTORY)) {
            ((PlayerEntity) (Object) this).dropItem(spiritum$getRing(), true, false);
            spiritum$ring = ItemStack.EMPTY;
        }
    }
}
