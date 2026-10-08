package net.dark.spiritum.mixin;

import net.dark.spiritum.client.TunedBladeGuidance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class TunedBladeCameraMixin {
    @Inject(method = "updateCamera", at = @At("HEAD"))
    private void spiritum$guideBlade(RenderTickCounter ticks, CallbackInfo ci) {
        TunedBladeGuidance.update(MinecraftClient.getInstance());
    }
}
