package net.dark.spiritum.magic;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.entity.SentinelEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public final class Vigilance {
    private static final Map<World, Set<BlockPos>> RITES = new WeakHashMap<>();

    public static void add(World world, BlockPos pos) {
        RITES.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    public static void remove(World world, BlockPos pos) {
        Set<BlockPos> rites = RITES.get(world);
        if (rites != null) rites.remove(pos);
    }

    private static PedestalBlockEntity active(World world, BlockPos pos) {
        return world.isChunkLoaded(pos)
                        && world.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal
                        && pedestal.getActiveRitual().equals("vigilance")
                        && pedestal.isSustained()
                ? pedestal
                : null;
    }

    public static boolean permits(
            SentinelEntity sentinel, LivingEntity target, UUID defendedPlayer) {
        World world = sentinel.getEntityWorld();
        boolean armed = false;
        for (BlockPos pos : RITES.getOrDefault(world, Set.of())) {
            var rite = active(world, pos);
            if (rite != null && sentinel.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 2500) {
                if (rite.getBoundPlayers().contains(target.getUuid())) return false;
                if (defendedPlayer == null || rite.getBoundPlayers().contains(defendedPlayer))
                    armed = true;
            }
        }
        return armed;
    }

    public static void attacked(PlayerEntity victim, LivingEntity attacker) {
        if (!(victim.getEntityWorld() instanceof ServerWorld world)
                || attacker == victim
                || !attacker.isAlive()
                || attacker instanceof PlayerEntity player
                        && (player.isCreative() || player.isSpectator() || !world.isPvpEnabled()))
            return;
        for (BlockPos pos : RITES.getOrDefault(world, Set.of())) {
            var rite = active(world, pos);
            if (rite == null
                    || !rite.getBoundPlayers().contains(victim.getUuid())
                    || rite.getBoundPlayers().contains(attacker.getUuid())) continue;
            for (SentinelEntity sentinel :
                    world.getEntitiesByClass(
                            SentinelEntity.class,
                            new Box(pos).expand(50),
                            s -> s.isAlive() && s.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 2500))
                sentinel.defend(attacker, victim.getUuid());
        }
    }

    private Vigilance() {}
}
