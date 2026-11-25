package net.dark.spiritum.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.LiquidPotion;
import net.dark.spiritum.vat.VatLiquid;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidVariantAttributes.class)
public class FluidVariantAttributesMixin {
    @WrapMethod(method = "getName")
    private static Text spiritum$correctName(FluidVariant variant, Operation<Text> original) {
        if (variant.getFluid() instanceof LiquidPotion liquidPotion)
            return Text.translatable("item.minecraft.potion.effect."+liquidPotion.getPotion().getBaseName());
        if (variant.getFluid() instanceof VatLiquid)
            return Text.translatable(SpiritumRegistries.VAT_LIQUID.getId(variant.getFluid()).toTranslationKey("vat_liquid"));
        return original.call(variant);
    }
}
