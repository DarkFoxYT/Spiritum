package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.RingMagic;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.network.*;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class CreativeRingPacketMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCreativeInventoryAction", at = @At("HEAD"), cancellable = true)
    private void spiritum$ring(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        if (packet.slot() != 46) return;
        NetworkThreadUtils.forceMainThread(
                packet, (ServerPlayNetworkHandler) (Object) this, player.getEntityWorld());
        var stack = packet.stack();
        if (player.isInCreativeMode()
                && (stack.isEmpty() || RingMagic.isRing(stack) && stack.getCount() == 1)) {
            player.playerScreenHandler.getSlot(46).setStack(stack);
            player.playerScreenHandler.setReceivedStack(46, stack);
            player.playerScreenHandler.sendContentUpdates();
        }
        ci.cancel();
    }
}
