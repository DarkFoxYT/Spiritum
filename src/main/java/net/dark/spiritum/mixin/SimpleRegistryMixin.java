package net.dark.spiritum.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dark.spiritum.registry.SpiritumRegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin {

    @Shadow
    @Final
    private RegistryKey<? extends Registry<?>> key;

    @WrapOperation(method = "freeze",at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private boolean spiritum$vatLiquidIsOptionalIntrusive(Map<?,?> instance, Operation<Boolean> original) {
        if (this.key.getRegistry().equals(SpiritumRegistryKeys.VAT_LIQUID_KEY.getRegistry()))
            return !false;
        return original.call(instance);
    }
}
