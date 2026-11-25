package net.dark.spiritum.mixin.jei$;

import mezz.jei.common.util.RegistryUtil;
import net.dark.spiritum.registry.SpiritumRegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RegistryUtil.class,remap = false)
public class RegistryUtilMixin {
    @ModifyVariable(method = "getRegistry", at = @At("HEAD"), argsOnly = true, name = "key")
    private static RegistryKey<?> spiritum$change(RegistryKey<?> key) {
        if (key == RegistryKeys.FLUID)
            return SpiritumRegistryKeys.VAT_LIQUID_KEY;
        return key;
    }
}
