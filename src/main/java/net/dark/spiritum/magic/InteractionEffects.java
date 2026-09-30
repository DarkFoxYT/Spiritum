package net.dark.spiritum.magic;

import net.dark.spiritum.registry.ModParticles;
import net.minecraft.entity.Entity;
import net.minecraft.particle.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class InteractionEffects {
    public static void magic(World world, Vec3d center, boolean strong) {
        if (world instanceof ServerWorld server)
            server.spawnParticles(
                    strong ? ModParticles.BIG_HEXFLAME : ModParticles.HEXFLAME,
                    center.x,
                    center.y,
                    center.z,
                    strong ? 24 : 12,
                    .18,
                    .15,
                    .18,
                    .015);
    }

    public static void atHand(Entity entity, boolean strong) {
        magic(
                entity.getEntityWorld(),
                entity.getEntityPos()
                        .add(0, entity.getHeight() * .65, 0)
                        .add(entity.getRotationVector().multiply(.45)),
                strong);
    }

    public static void offering(World world, BlockPos pos) {
        magic(world, Vec3d.ofCenter(pos).add(0, .7, 0), false);
    }

    public static void snuff(World world, BlockPos pos) {
        if (world instanceof ServerWorld server)
            server.spawnParticles(
                    ParticleTypes.SMOKE,
                    pos.getX() + .5,
                    pos.getY() + 1.05,
                    pos.getZ() + .5,
                    8,
                    .07,
                    .08,
                    .07,
                    .015);
    }

    public static void splash(World world, BlockPos pos) {
        if (world instanceof ServerWorld server)
            server.spawnParticles(
                    ParticleTypes.SPLASH,
                    pos.getX() + .5,
                    pos.getY() + .85,
                    pos.getZ() + .5,
                    16,
                    .2,
                    .05,
                    .2,
                    .08);
    }

    private InteractionEffects() {}
}
