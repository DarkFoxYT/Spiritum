package net.dark.spiritum.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.*;

import java.util.*;

public final class ModParticles {
    public static final SimpleParticleType HEXFLAME = register("hexflame_particle");
    public static final SimpleParticleType BIG_HEXFLAME = register("big_hexflame_particle");
    public static final SimpleParticleType DOMINION_RUNE = register("dominion_rune");
    public static final Map<String, SimpleParticleType> SIGILS = new LinkedHashMap<>();

    static {
        for (String name :
                List.of(
                        "weather",
                        "time",
                        "warding",
                        "libido",
                        "summoning",
                        "abundance",
                        "binding",
                        "calling",
                        "withering", "dominion", "vigilance")) SIGILS.put(name, register(name + "_rite"));
    }

    private static SimpleParticleType register(String name) {
        return Registry.register(
                Registries.PARTICLE_TYPE, ModContent.id(name), FabricParticleTypes.simple());
    }

    public static SimpleParticleType sigil(String ritual) {
        String family =
                switch (ritual) {
                    case "rain", "clear_skies", "thunder" -> "weather";
                    case "daytime", "nighttime" -> "time";
                    case "zombie_summoning", "skeleton_summoning" -> "summoning";
                    case "leech_binding",
                            "imp_binding",
                            "lemure_binding",
                            "argentic_transmutation" ->
                            "binding";
                    default -> ritual;
                };
        return SIGILS.get(family);
    }

    public static void initialize() {}

    private ModParticles() {}
}
