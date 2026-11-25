package net.dark.spiritum.vat;

import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public class LiquidPotion extends VatLiquid {

    private final Potion potion;

    private final RegistryEntry.Reference<Fluid> registryEntry = SpiritumRegistries.VAT_LIQUID.createEntry(this);

    public LiquidPotion(Potion potion) {
        super(ItemStack.EMPTY.getItem());
        this.potion = potion;
    }

    @Override
    public RegistryEntry.Reference<Fluid> getRegistryEntry() {
        return registryEntry;
    }

    public LiquidPotion() {
        this(Potions.WATER.value());
    }

    @Override
    public Item getBucketItem() {
        return Items.AIR;
    }

    public Potion getPotion() {
        return potion;
    }

    public static Fluid ofPotion(RegistryEntry<Potion> potion) {
        if (potion == Potions.WATER)
            return Fluids.WATER;
        return SpiritumRegistries.VAT_LIQUID.get(Registries.POTION.getId(potion.value()));
    }

    public static RegistryEntry<Potion> ofFluid(Fluid fluid) {
        if (fluid == Fluids.WATER)
            return Potions.WATER;
        return fluid instanceof LiquidPotion potionLiquid ? Registries.POTION.getEntry(potionLiquid.potion) : null;
    }
}
