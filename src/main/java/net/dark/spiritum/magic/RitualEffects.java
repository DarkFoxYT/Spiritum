package net.dark.spiritum.magic;

import net.dark.spiritum.entity.OwnedDemonEntity;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.registry.ModEntities;
import net.dark.spiritum.registry.ModParticles;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

import java.util.*;

public final class RitualEffects {
    public static void trigger(ServerWorld world, BlockPos pos, OfferingRecipe rite) {
        trigger(world, pos, rite, Set.of());
    }

    public static void trigger(
            ServerWorld world, BlockPos pos, OfferingRecipe rite, Set<UUID> boundPlayers) {
        ServerWorld overworld = world.getServer().getWorld(World.OVERWORLD);
        if (overworld == null) overworld = world;
        switch (rite.id()) {
            case "rain" -> overworld.setWeather(0, 12000, true, false);
            case "clear_skies" -> overworld.setWeather(12000, 0, false, false);
            case "thunder" -> overworld.setWeather(0, 12000, true, true);
            case "daytime" ->
                    overworld.setTimeOfDay(overworld.getTimeOfDay() / 24000 * 24000 + 6000);
            case "nighttime" ->
                    overworld.setTimeOfDay(overworld.getTimeOfDay() / 24000 * 24000 + 18000);
            case "zombie_summoning" ->
                    EntityType.ZOMBIE.spawn(world, pos.up(), SpawnReason.TRIGGERED);
            case "skeleton_summoning" ->
                    EntityType.SKELETON.spawn(world, pos.up(), SpawnReason.TRIGGERED);
            case "argentic_transmutation" ->
                    world.spawnEntity(
                            new ItemEntity(
                                    world,
                                    pos.getX() + .5,
                                    pos.getY() + 1.2,
                                    pos.getZ() + .5,
                                    new ItemStack(ModContent.ARGENT_NUGGET)));
            case "leech_binding" -> summon(world, pos, ModEntities.LEECH, boundPlayers);
            case "imp_binding" -> summon(world, pos, ModEntities.IMP, boundPlayers);
            case "lemure_binding" -> summon(world, pos, ModEntities.LEMURE, boundPlayers);
            case "calling" -> {
                if (!boundPlayers.isEmpty()) {
                    var player =
                            world.getServer()
                                    .getPlayerManager()
                                    .getPlayer(boundPlayers.iterator().next());
                    if (player != null) {
                        player.teleport(
                                world,
                                pos.getX() + .5,
                                pos.getY() + 1.05,
                                pos.getZ() + .5,
                                Set.of(),
                                player.getYaw(),
                                player.getPitch(),
                                true);

                    }
                }
            }
            default -> throw new IllegalArgumentException("Unknown instant ritual: " + rite.id());
        }
        world.spawnParticles(
                ModParticles.BIG_HEXFLAME,
                pos.getX() + .5,
                pos.getY() + 1.5,
                pos.getZ() + .5,
                45,
                .4,
                .4,
                .4,
                .03);
        world.playSound(
                null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1, .75f);
    }

    public static void sustain(ServerWorld world, BlockPos pos, OfferingRecipe rite, int elapsed) {
        sustain(world, pos, rite, elapsed, Set.of());
    }

    public static void sustain(
            ServerWorld world,
            BlockPos pos,
            OfferingRecipe rite,
            int elapsed,
            Set<UUID> boundPlayers) {
        switch (rite.id()) {
            case "warding" -> Warding.add(world, pos);
            case "libido" -> {
                if (elapsed % 1200 == 0) breed(world, pos);
            }
            case "abundance" -> grow(world, pos);
            case "withering" -> {
                if (elapsed % 20 == 0) wither(world, pos, boundPlayers);
            }
            default ->
                    throw new IllegalArgumentException("Unknown persistent ritual: " + rite.id());
        }
    }

    private static <T extends OwnedDemonEntity> void summon(
            ServerWorld world, BlockPos pos, EntityType<T> type, Set<UUID> players) {
        if (players.size() != 1)
            throw new IllegalArgumentException("Demon binding needs exactly one summoner");
        T demon = type.create(world, SpawnReason.TRIGGERED);
        if (demon != null) {
            demon.setOwnerUuid(players.iterator().next());
            demon.refreshPositionAndAngles(
                    pos.getX() + .5, pos.getY() + 1.1, pos.getZ() + .5, 0, 0);
            world.spawnEntity(demon);
        }
    }

    private static void wither(ServerWorld world, BlockPos pos, Set<UUID> protectedPlayers) {
        Vec3d center = Vec3d.ofCenter(pos);
        for (LivingEntity entity :
                world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(pos).expand(50),
                        e -> e.isAlive() && e.squaredDistanceTo(center) <= 2500)) {
            if (entity instanceof PlayerEntity && protectedPlayers.contains(entity.getUuid()))
                continue;
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
        }
    }

    private static void breed(ServerWorld world, BlockPos pos) {
        List<AnimalEntity> animals =
                new ArrayList<>(
                        world.getEntitiesByClass(
                                AnimalEntity.class,
                                new Box(pos).expand(15),
                                a ->
                                        a.isAlive()
                                                && a.getBreedingAge() == 0
                                                && a.squaredDistanceTo(Vec3d.ofCenter(pos))
                                                        <= 225));
        Collections.shuffle(animals, new Random(world.random.nextLong()));
        for (int i = 0; i < animals.size(); i++)
            for (int j = i + 1; j < animals.size(); j++) {
                AnimalEntity first = animals.get(i), second = animals.get(j);
                if (first.getType() != second.getType()) continue;
                first.lovePlayer(null);
                second.lovePlayer(null);
                if (first.canBreedWith(second)) {
                    first.breed(world, second);
                    return;
                }
                first.resetLoveTicks();
                second.resetLoveTicks();
            }
    }

    private static void grow(ServerWorld world, BlockPos pos) {
        // Vanilla random ticks sample each block randomTickSpeed / 4096 times per tick.
        // Add two independent passes for exactly 3x expected crop ticks (respecting the gamerule).
        int speed = world.getGameRules().getValue(GameRules.RANDOM_TICK_SPEED);
        if (speed <= 0) return;
        for (BlockPos cropPos : BlockPos.iterate(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
            if (cropPos.getSquaredDistance(pos) > 16 || !world.isChunkLoaded(cropPos)) continue;
            for (int pass = 0; pass < 2; pass++) {
                int ticks =
                        speed / 4096
                                + (world.random.nextDouble() < (speed % 4096) / 4096.0 ? 1 : 0);
                for (int tick = 0; tick < ticks; tick++) {
                    BlockState crop = world.getBlockState(cropPos);
                    if (crop.getBlock() instanceof CropBlock
                            || crop.getBlock() instanceof StemBlock
                            || crop.getBlock() instanceof CocoaBlock
                            || crop.getBlock() instanceof SweetBerryBushBlock
                            || crop.getBlock() instanceof NetherWartBlock)
                        crop.randomTick(world, cropPos, world.random);
                }
            }
        }
    }

    public static void sigil(ServerWorld world, BlockPos pos, OfferingRecipe rite, int elapsed) {
        var sprite = ModParticles.sigil(rite.id());
        if (sprite != null)
            world.spawnParticles(
                    sprite, pos.getX() + .5, pos.getY() + 2.6, pos.getZ() + .5, 1, 0, 0, 0, 0);
        double angle = elapsed * .12;
        world.spawnParticles(
                isHighRitual(rite.id()) ? ModParticles.BIG_HEXFLAME : ModParticles.HEXFLAME,
                pos.getX() + .5 + Math.cos(angle) * .7,
                pos.getY() + 1.75,
                pos.getZ() + .5 + Math.sin(angle) * .7,
                3,
                .025,
                .025,
                .025,
                .01);
        world.spawnParticles(
                ParticleTypes.ENCHANT,
                pos.getX() + .5,
                pos.getY() + 1.25,
                pos.getZ() + .5,
                4,
                .35,
                .25,
                .35,
                .05);
    }

    public static boolean isHighRitual(String id) {
        return id.endsWith("_binding") || id.equals("calling") || id.equals("withering");
    }

    private RitualEffects() {}
}
