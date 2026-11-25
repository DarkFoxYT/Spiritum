package net.dark.spiritum.mixin.jei$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.fabric.platform.FluidHelper;
import net.dark.spiritum.vat.LiquidPotion;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidHelper.class)
public class FluidHelperMixin {
    @WrapOperation(method = "getDisplayName(Lmezz/jei/api/fabric/ingredients/fluids/IJeiFluidIngredient;)Lnet/minecraft/text/Text;",at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariantAttributes;getName(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;)Lnet/minecraft/text/Text;"))
    private Text spiritum$correctName(FluidVariant variant, Operation<Text> original) {
        if (variant.getFluid() instanceof LiquidPotion liquidPotion)
            return Text.translatable("item.minecraft.potion.effect."+liquidPotion.getPotion().getBaseName());
        return original.call(variant);
    }
}
