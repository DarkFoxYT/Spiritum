package net.dark.spiritum.magic;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.mixin.MobGoalsAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

/** A real priority-zero flee goal prevents ordinary attack/wander AI from overriding wards. */
public final class Warding {
    private static final Map<World, Set<BlockPos>> WARDS = new WeakHashMap<>();

    public static void add(World world, BlockPos pos) {
        WARDS.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    public static void remove(World world, BlockPos pos) {
        Set<BlockPos> wards = WARDS.get(world);
        if (wards != null) wards.remove(pos);
    }

    private static Vec3d center(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal
                && pedestal.getBoundPlayer() != null) {
            var player = world.getServer().getPlayerManager().getPlayer(pedestal.getBoundPlayer());
            if (player != null && player.getEntityWorld() == world) return player.getEntityPos();
        }
        return Vec3d.ofCenter(pos);
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WARDS.clear());
        ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> {
                    if (entity instanceof MobEntity mob
                            && entity.getType().getSpawnGroup() == SpawnGroup.MONSTER)
                        ((MobGoalsAccessor) mob).spiritum$getGoals().add(0, new FleeGoal(mob));
                });
    }

    private static class FleeGoal extends Goal {
        private final MobEntity mob;
        private BlockPos ward;
        private int ticks;

        FleeGoal(MobEntity mob) {
            this.mob = mob;
            setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.TARGET));
        }

        @Override
        public boolean canStart() {
            Set<BlockPos> wards = WARDS.get(mob.getEntityWorld());
            if (wards == null) return false;
            ward =
                    wards.stream()
                            .filter(
                                    p ->
                                            mob.getEntityWorld().isChunkLoaded(p)
                                                    && mob.getEntityWorld().getBlockEntity(p)
                                                            instanceof PedestalBlockEntity pedestal
                                                    && pedestal.getActiveRitual().equals("warding")
                                                    && mob.squaredDistanceTo(
                                                                    center(mob.getEntityWorld(), p))
                                                            <= 2500)
                            .min(
                                    Comparator.comparingDouble(
                                            p ->
                                                    mob.squaredDistanceTo(
                                                            center(mob.getEntityWorld(), p))))
                            .orElse(null);
            return ward != null;
        }

        @Override
        public boolean shouldContinue() {
            return ward != null
                    && WARDS.getOrDefault(mob.getEntityWorld(), Set.of()).contains(ward)
                    && mob.getEntityWorld().isChunkLoaded(ward)
                    && mob.squaredDistanceTo(center(mob.getEntityWorld(), ward)) <= 2500;
        }

        @Override
        public void start() {
            ticks = 0;
            flee();
        }

        @Override
        public void tick() {
            mob.setTarget(null);
            if (++ticks % 10 == 0) flee();
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            ward = null;
        }

        private void flee() {
            mob.setTarget(null);
            Vec3d center = center(mob.getEntityWorld(), ward),
                    away = mob.getEntityPos().subtract(center);
            if (away.horizontalLengthSquared() < .01) away = new Vec3d(1, 0, 0);
            away = new Vec3d(away.x, 0, away.z).normalize();
            if (!mob.getNavigation()
                    .startMovingTo(center.x + away.x * 54, mob.getY(), center.z + away.z * 54, 1.5))
                mob.getNavigation()
                        .startMovingTo(
                                mob.getX() + away.x * 12,
                                mob.getY(),
                                mob.getZ() + away.z * 12,
                                1.5);
        }
    }

    private Warding() {}
}
