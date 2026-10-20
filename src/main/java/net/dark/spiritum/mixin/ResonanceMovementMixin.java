package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.Resonance;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ResonanceMovementMixin {
    @Inject(
            method =
                    "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("RETURN"),
            cancellable = true)
    private void spiritum$resonance(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        cir.setReturnValue(Resonance.constrain((Entity) (Object) this, cir.getReturnValue()));
    }
}
