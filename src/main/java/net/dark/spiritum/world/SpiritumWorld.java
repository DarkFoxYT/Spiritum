package net.dark.spiritum.world;

import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.*;
import net.minecraft.registry.*;
import net.minecraft.world.gen.GenerationStep;

public final class SpiritumWorld {
    public static void initialize() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, ModContent.id("hexstone_deposit")));
        LootTableEvents.MODIFY.register(
                (key, builder, source, registries) -> {
                    if (!source.isBuiltin()) return;
                    String path = key.getValue().getPath();
                    if (path.equals("chests/ancient_city")) {
                        builder.pool(
                                LootPool.builder()
                                        .rolls(ConstantLootNumberProvider.create(1))
                                        .conditionally(RandomChanceLootCondition.builder(.65f))
                                        .with(
                                                ItemEntry.builder(ModContent.ARGENT_NUGGET)
                                                        .apply(
                                                                SetCountLootFunction.builder(
                                                                        UniformLootNumberProvider
                                                                                .create(2, 6)))));
                        builder.pool(
                                LootPool.builder()
                                        .rolls(ConstantLootNumberProvider.create(1))
                                        .conditionally(RandomChanceLootCondition.builder(.15f))
                                        .with(ItemEntry.builder(ModContent.ARGENT_INGOT)));
                    } else if (path.equals("chests/simple_dungeon")
                            || path.startsWith("chests/stronghold_")) {
                        builder.pool(
                                LootPool.builder()
                                        .rolls(ConstantLootNumberProvider.create(1))
                                        .conditionally(RandomChanceLootCondition.builder(.25f))
                                        .with(
                                                ItemEntry.builder(ModContent.ARGENT_NUGGET)
                                                        .apply(
                                                                SetCountLootFunction.builder(
                                                                        UniformLootNumberProvider
                                                                                .create(1, 3)))));
                    }
                });
    }

    private SpiritumWorld() {}
}
