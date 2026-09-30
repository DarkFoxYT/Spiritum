package net.dark.spiritum.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeRingScreenMixin extends HandledScreen<ScreenHandler> {
    protected CreativeRingScreenMixin(
            ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @ModifyArgs(
            method = "setSelectedTab",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeSlot;<init>(Lnet/minecraft/screen/slot/Slot;III)V"))
    private void spiritum$position(Args args) {
        if ((int) args.get(1) == 46) {
            args.set(2, 35);
            args.set(3, 38);
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void spiritum$background(
            DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (handler.slots.size() != 48) return;
        int left = x + 34, top = y + 37;
        context.fill(left, top, left + 18, top + 18, 0xFF373737);
        context.fill(left + 1, top + 1, left + 18, top + 18, 0xFFFFFFFF);
        context.fill(left + 1, top + 1, left + 17, top + 17, 0xFF8B8B8B);
    }
}
