package net.dark.spiritum.mixin.emi$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.stack.serializer.FluidEmiStackSerializer;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(print = true)
@Mixin(FluidEmiStackSerializer.class)
public class FluidEmiStackSerializerMixin {
    @SuppressWarnings("MixinAnnotationTarget")
    @WrapOperation(method = "create",at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;"))
    private Object spiritum$allowVatLiquid(Registry<?> instance, Identifier identifier, Operation<Object> original) {
        var entry = original.call(instance,identifier);
        if (entry == null)
            return SpiritumRegistries.VAT_LIQUID.get(identifier);
        return entry;
    }
}
