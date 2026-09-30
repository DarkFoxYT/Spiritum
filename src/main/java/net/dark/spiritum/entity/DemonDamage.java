package net.dark.spiritum.entity;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.entity.damage.*;
import net.minecraft.registry.*;
import net.minecraft.server.world.ServerWorld;

public final class DemonDamage {
    public static final RegistryKey<DamageType> LEECH =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ModContent.id("leech"));
    public static final RegistryKey<DamageType> IMP =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ModContent.id("imp"));

    public static DamageSource source(
            ServerWorld world, RegistryKey<DamageType> type, OwnedDemonEntity demon) {
        return new DamageSource(
                world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(type),
                demon,
                demon.getOwner() == null ? demon : demon.getOwner());
    }

    private DemonDamage() {}
}
