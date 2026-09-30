package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerRingSlotMixin extends ScreenHandler {
    protected PlayerRingSlotMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spiritum$slot(
            PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        addSlot(
                new Slot(new RingInventory(owner), 0, 77, 44) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return RingMagic.isRing(stack);
                    }

                    @Override
                    public int getMaxItemCount() {
                        return 1;
                    }
                });
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void spiritum$shift(
            PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot source = slots.get(index);
        if (!source.hasStack()) return;
        ItemStack stack = source.getStack();
        boolean remove = index == 46;
        boolean equip =
                index >= 9 && index <= 45 && RingMagic.isRing(stack) && !slots.get(46).hasStack();
        if (!remove && !equip) return;
        ItemStack original = stack.copy();
        if (!insertItem(stack, remove ? 9 : 46, remove ? 45 : 47, remove)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (stack.isEmpty()) source.setStack(ItemStack.EMPTY);
        else source.markDirty();
        source.onTakeItem(player, stack);
        cir.setReturnValue(original);
    }
}
