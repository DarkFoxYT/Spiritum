package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.RingMagic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class RingDamageMixin {
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float spiritum$defense(
            float amount, ServerWorld world, DamageSource source, float original) {
        return RingMagic.adjust((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
    private void spiritum$share(
            DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(
                RingMagic.share((LivingEntity) (Object) this, source, cir.getReturnValue()));
    }
}
