package net.dark.spiritum.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryRingScreenMixin extends HandledScreen<PlayerScreenHandler> {
    protected InventoryRingScreenMixin(
            PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void spiritum$background(
            DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int left = x + 76, top = y + 43;
        context.fill(left, top, left + 18, top + 18, 0xFF373737);
        context.fill(left + 1, top + 1, left + 18, top + 18, 0xFFFFFFFF);
        context.fill(left + 1, top + 1, left + 17, top + 17, 0xFF8B8B8B);
        if (!handler.slots.get(46).hasStack()) {
            context.fill(left + 6, top + 4, left + 12, top + 5, 0xFF555555);
            context.fill(left + 4, top + 6, left + 5, top + 12, 0xFF555555);
            context.fill(left + 13, top + 6, left + 14, top + 12, 0xFF555555);
            context.fill(left + 6, top + 13, left + 12, top + 14, 0xFF555555);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void spiritum$tooltip(
            DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!handler.slots.get(46).hasStack()
                && mouseX >= x + 77
                && mouseX < x + 93
                && mouseY >= y + 44
                && mouseY < y + 60)
            context.drawTooltip(
                    textRenderer, Text.translatable("container.spiritum.ring"), mouseX, mouseY);
    }
}
