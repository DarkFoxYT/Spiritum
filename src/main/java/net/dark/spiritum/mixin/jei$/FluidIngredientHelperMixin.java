package net.dark.spiritum.mixin.jei$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import net.dark.spiritum.registry.SpiritumRegistryKeys;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidIngredientHelper.class,remap = false)
public class FluidIngredientHelperMixin {
    @WrapOperation(method = {"<init>","isIngredientOnServer"},at = @At(value = "FIELD", target = "Lnet/minecraft/registry/RegistryKeys;FLUID:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<Registry<Fluid>> spiritum$countVatLiquid(Operation<RegistryKey<Registry<Fluid>>> original) {
        return SpiritumRegistryKeys.VAT_LIQUID_KEY;
    }
}
