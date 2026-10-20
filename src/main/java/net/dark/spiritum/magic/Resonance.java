package net.dark.spiritum.magic;

import net.dark.spiritum.entity.SingingRodEntity;
import net.dark.spiritum.registry.ModEntities;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public final class Resonance {
    private static final Map<World, Set<Integer>> LOADED_RODS =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static final RegistryKey<Enchantment> ENCHANTMENT =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("spiritum", "resonance"));

    private Resonance() {}

    public static void watch(SingingRodEntity rod) {
        LOADED_RODS
                .computeIfAbsent(rod.getEntityWorld(), world -> new HashSet<>())
                .add(rod.getId());
    }

    public static boolean enchanted(ItemStack stack) {
        return stack.getEnchantments().getEnchantmentEntries().stream()
                .anyMatch(
                        entry -> entry.getKey().matchesKey(ENCHANTMENT) && entry.getIntValue() > 0);
    }

    private static List<SingingRodEntity> nearby(World world, Box area) {
        Set<Integer> ids = LOADED_RODS.get(world);
        if (ids == null || ids.isEmpty()) return List.of();
        Box bounds = area.expand(64);
        List<SingingRodEntity> result = new ArrayList<>();
        ids.removeIf(
                id -> {
                    var entity = world.getEntityById(id);
                    if (!(entity instanceof SingingRodEntity rod) || rod.isRemoved()) return true;
                    if (rod.isAlive() && bounds.intersects(rod.getBoundingBox())) result.add(rod);
                    return false;
                });
        return result;
    }

    public static ActionResult place(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player == null) return ActionResult.PASS;
        if (player.getItemCooldownManager().isCoolingDown(context.getStack()))
            return ActionResult.FAIL;
        if (!(context.getWorld() instanceof ServerWorld world)) return ActionResult.SUCCESS;
        if (context.getSide() != Direction.UP
                || !player.canModifyBlocks()
                || !world.canEntityModifyAt(player, context.getBlockPos()))
            return ActionResult.FAIL;
        Vec3d position = context.getHitPos();
        var rod = ModEntities.SINGING_ROD.create(world, SpawnReason.TRIGGERED);
        if (rod == null) return ActionResult.FAIL;
        rod.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);
        if (!world.isSpaceEmpty(rod, rod.getBoundingBox())) return ActionResult.FAIL;
        var candidates = nearby(world, rod.getBoundingBox());
        if (candidates.stream().anyMatch(r -> r.squaredDistanceTo(position) < 1))
            return ActionResult.FAIL;
        List<SingingRodEntity> group = new ArrayList<>();
        for (var other : candidates) {
            if (!other.sealed()
                    && player.getUuid().equals(other.owner())
                    && !other.group().isEmpty()
                    && other.squaredDistanceTo(position) <= 32 * 32
                    && Math.abs(other.getY() - position.y) <= 4) {
                group = candidates.stream().filter(r -> r.group().equals(other.group())).toList();
                if (group.size() < 4
                        && group.stream()
                                .allMatch(
                                        r ->
                                                r.squaredDistanceTo(position) <= 32 * 32
                                                        && Math.abs(r.getY() - position.y) <= 4))
                    break;
                group = List.of();
            }
        }
        String id = group.isEmpty() ? UUID.randomUUID().toString() : group.getFirst().group();
        rod.assign(player.getUuid(), id);
        List<SingingRodEntity> completed = new ArrayList<>(group);
        completed.add(rod);
        if (completed.size() == 4 && polygon(completed).isEmpty()) {
            player.sendMessage(Text.translatable("message.spiritum.resonance_shape"), true);
            return ActionResult.FAIL;
        }
        if (!world.spawnEntity(rod)) return ActionResult.FAIL;
        watch(rod);
        if (completed.size() == 4) completed.forEach(SingingRodEntity::seal);
        player.getItemCooldownManager().set(context.getStack(), 10);
        context.getStack()
                .damage(
                        1,
                        player,
                        context.getHand() == Hand.MAIN_HAND
                                ? EquipmentSlot.MAINHAND
                                : EquipmentSlot.OFFHAND);
        world.playSound(
                null,
                rod.getBlockPos(),
                net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                net.minecraft.sound.SoundCategory.PLAYERS,
                1,
                completed.size() * .2f + .6f);
        return ActionResult.SUCCESS;
    }

    public static List<Vec3d> polygon(List<SingingRodEntity> rods) {
        if (rods.size() != 4) return List.of();
        double x = rods.stream().mapToDouble(Entity::getX).average().orElse(0);
        double z = rods.stream().mapToDouble(Entity::getZ).average().orElse(0);
        var points =
                rods.stream()
                        .map(Entity::getEntityPos)
                        .sorted(Comparator.comparingDouble(p -> Math.atan2(p.z - z, p.x - x)))
                        .toList();
        for (int i = 0; i < 4; i++)
            if (cross(points.get(i), points.get((i + 1) % 4), points.get((i + 2) % 4)) < .1)
                return List.of();
        return points;
    }

    private static double cross(Vec3d a, Vec3d b, Vec3d p) {
        return (b.x - a.x) * (p.z - a.z) - (b.z - a.z) * (p.x - a.x);
    }

    private static boolean inside(List<Vec3d> polygon, Vec3d point) {
        for (int i = 0; i < 4; i++)
            if (cross(polygon.get(i), polygon.get((i + 1) % 4), point) < -.00001) return false;
        return true;
    }

    public static Vec3d constrain(Entity entity, Vec3d movement) {
        if (!(entity instanceof LivingEntity) && !entity.hasPassengers()
                || entity.isSpectator()
                || entity.noClip
                || movement.horizontalLengthSquared() < .00000001) return movement;
        var groups = new HashMap<String, List<SingingRodEntity>>();
        for (var rod : nearby(entity.getEntityWorld(), entity.getBoundingBox()))
            if (rod.sealed() && !rod.group().isEmpty())
                groups.computeIfAbsent(rod.group(), id -> new ArrayList<>()).add(rod);
        for (var rods : groups.values()) {
            var polygon = polygon(rods);
            if (polygon.isEmpty()) continue;
            Vec3d start = entity.getEntityPos();
            if (!inside(polygon, start)) continue;
            double fraction = 1;
            for (int i = 0; i < 4; i++) {
                Vec3d a = polygon.get(i), b = polygon.get((i + 1) % 4);
                double before = cross(a, b, start), after = cross(a, b, start.add(movement));
                if (after < 0)
                    fraction = Math.min(fraction, Math.max(0, before / (before - after) - .0001));
            }
            movement = new Vec3d(movement.x * fraction, movement.y, movement.z * fraction);
        }
        return movement;
    }

    public static void particles(ServerWorld world, SingingRodEntity rod) {
        if (!rod.sealed()) return;
        var rods =
                nearby(world, rod.getBoundingBox()).stream()
                        .filter(r -> r.group().equals(rod.group()))
                        .toList();
        if (rods.stream().anyMatch(r -> r.getId() < rod.getId())) return;
        var polygon = polygon(rods);
        if (polygon.isEmpty()) return;
        for (int i = 0; i < 4; i++) {
            Vec3d a = polygon.get(i), b = polygon.get((i + 1) % 4);
            int steps = Math.min(32, Math.max(2, (int) a.distanceTo(b) * 2));
            for (int j = 0; j <= steps; j++) {
                Vec3d p = a.lerp(b, (double) j / steps);
                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        p.x,
                        p.y + 1 + (world.getTime() / 10 % 4),
                        p.z,
                        1,
                        0,
                        .05,
                        0,
                        0);
            }
        }
    }
}
