package net.dark.spiritum.registry;

import net.dark.spiritum.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.registry.*;

public final class ModEntities {
    private static <T extends Entity> EntityType<T> register(
            String id, EntityType.Builder<T> builder) {
        var key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, ModContent.id(id));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    public static final EntityType<LeechDemonEntity> LEECH =
            register(
                    "leech_demon",
                    EntityType.Builder.create(LeechDemonEntity::new, SpawnGroup.CREATURE)
                            .dimensions(.45f, .45f)
                            .maxTrackingRange(8));
    public static final EntityType<ImpDemonEntity> IMP =
            register(
                    "imp_demon",
                    EntityType.Builder.create(ImpDemonEntity::new, SpawnGroup.CREATURE)
                            .dimensions(.45f, .65f)
                            .maxTrackingRange(8));
    public static final EntityType<LemureDemonEntity> LEMURE =
            register(
                    "lemure_demon",
                    EntityType.Builder.create(LemureDemonEntity::new, SpawnGroup.CREATURE)
                            .dimensions(.45f, .65f)
                            .maxTrackingRange(8));
    public static final EntityType<SpiritEnergyEntity> SPIRIT_ENERGY =
            register(
                    "spirit_energy",
                    EntityType.Builder.<SpiritEnergyEntity>create(
                                    SpiritEnergyEntity::new, SpawnGroup.MISC)
                            .dimensions(.25f, .25f)
                            .maxTrackingRange(6));

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(LEECH, OwnedDemonEntity.attributes(10, 2));
        FabricDefaultAttributeRegistry.register(IMP, OwnedDemonEntity.attributes(10, 4));
        FabricDefaultAttributeRegistry.register(LEMURE, OwnedDemonEntity.attributes(15, 0));
    }

    private ModEntities() {}
}
