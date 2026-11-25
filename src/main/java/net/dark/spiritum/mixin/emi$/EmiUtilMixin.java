package net.dark.spiritum.mixin.emi$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.EmiUtil;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiUtil.class)
public class EmiUtilMixin {
    @WrapOperation(method = "subId(Lnet/minecraft/fluid/Fluid;)Ljava/lang/String;",at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private static @Nullable Identifier spiritum$allowVatLiquid(Registry<?> instance, Object t, Operation<Identifier> original) {
        var id = original.call(instance,t);
        if (id == null && t instanceof Fluid fluid)
            return SpiritumRegistries.VAT_LIQUID.getId(fluid);
        return id;
    }
}
