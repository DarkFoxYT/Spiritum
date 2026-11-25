package net.dark.spiritum.mixin.emi$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiTagKey;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiTagKey.class)
public abstract class EmiTagKeyMixin {
    @Shadow
    public abstract Registry<?> registry();

    @WrapOperation(method = "stream",at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiPort;getFluidRegistry()Lnet/minecraft/registry/Registry;"))
    private Registry<Fluid> spiritum$allowVatLiquid(Operation<Registry<Fluid>> original) {
        if (registry() == SpiritumRegistries.VAT_LIQUID)
            return SpiritumRegistries.VAT_LIQUID;
        return original.call();
    }
}
