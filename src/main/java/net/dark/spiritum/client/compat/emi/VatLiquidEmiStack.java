package net.dark.spiritum.client.compat.emi;

import dev.emi.emi.api.stack.FluidEmiStack;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.VatLiquid;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VatLiquidEmiStack extends FluidEmiStack {

    public VatLiquidEmiStack(VatLiquid fluid) {
        super(fluid, ComponentChanges.EMPTY);
    }

    public VatLiquidEmiStack(VatLiquid fluid, ComponentChanges componentChanges) {
        super(fluid, componentChanges, 0);
    }

    public VatLiquidEmiStack(VatLiquid fluid, ComponentChanges componentChanges, long amount) {
        super(fluid,componentChanges,amount);
    }

    @Override
    public Object getKey() {
        return super.getKey();
    }

    @Override
    public Text getName() {
        return FluidVariantAttributes.getName(FluidVariant.of((Fluid) getKey()));
    }

    @Override
    public Identifier getId() {
        return SpiritumRegistries.VAT_LIQUID.getId((VatLiquid) this.getKey());
    }
}
