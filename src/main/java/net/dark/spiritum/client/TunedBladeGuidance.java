package net.dark.spiritum.client;

import net.dark.spiritum.network.TunedBladeTargetPayload;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class TunedBladeGuidance {
    private static float targetYaw, targetPitch;
    private static long receivedAt, lastFrame;

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                TunedBladeTargetPayload.ID,
                (payload, context) -> {
                    targetYaw = payload.yaw();
                    targetPitch = payload.pitch();
                    receivedAt = System.nanoTime();
                });
    }

    public static void update(MinecraftClient client) {
        long now = System.nanoTime();
        float seconds = lastFrame == 0 ? 0 : Math.min(.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        var player = client.player;
        if (client.isPaused()
                || player == null
                || !player.isUsingItem()
                || !player.getActiveItem().isOf(ModContent.TUNED_BLADE)
                || now - receivedAt > 250_000_000L) return;
        // Rotate the local camera each frame; ordinary movement packets report the resulting look.
        float step = 30 * seconds;
        player.setYaw(MathHelper.stepUnwrappedAngleTowards(player.getYaw(), targetYaw, step));
        player.setPitch(MathHelper.stepTowards(player.getPitch(), targetPitch, step));
        player.setHeadYaw(player.getYaw());
    }

    private TunedBladeGuidance() {}
}
