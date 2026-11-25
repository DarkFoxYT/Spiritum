package net.dark.spiritum.registry;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.util.Identifier;

public final class SpiritumRegistries {
    public static final DefaultedRegistry<Fluid> VAT_LIQUID = FabricRegistryBuilder
        .from(new SimpleDefaultedRegistry<>(Identifier.of("empty").toString(), SpiritumRegistryKeys.VAT_LIQUID_KEY, Lifecycle.stable(), true))
        .buildAndRegister();

    public static void initialize() {
    }
}
