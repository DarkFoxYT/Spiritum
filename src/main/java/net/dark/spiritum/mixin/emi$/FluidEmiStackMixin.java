package net.dark.spiritum.mixin.emi$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidEmiStack.class)
public abstract class FluidEmiStackMixin {
    @Shadow
    public abstract Identifier getId();

    @WrapOperation(method = "getTooltip",at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private @Nullable Identifier spritum$useId(Registry<?> instance, Object t, Operation<Identifier> original) {
        return this.getId();
    }
}
