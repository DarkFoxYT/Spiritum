package net.dark.spiritum.registry;

import net.dark.spiritum.Spiritum;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public final class SpiritumRegistryKeys {
    public static final RegistryKey<Registry<Fluid>> VAT_LIQUID_KEY = RegistryKey.ofRegistry(Spiritum.id("vat_liquid"));

    public static void initialize() {}
}
