package net.dark.spiritum.magic;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.registry.ModParticles;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

/** Shared client/server lookup so mining progress agrees with server enforcement. */
public final class Dominion {
    private static final Map<World, Set<BlockPos>> RITES = new WeakHashMap<>();

    public static void add(World world, BlockPos pos) {
        RITES.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    public static void remove(World world, BlockPos pos) {
        var rites = RITES.get(world);
        if (rites != null) rites.remove(pos);
    }

    public static boolean restricted(World world, BlockPos block, PlayerEntity player) {
        if (MagicRecipes.ritual("dominion") == null) return false;
        boolean covered = false;
        for (BlockPos pos : RITES.getOrDefault(world, Set.of())) {
            if (!world.isChunkLoaded(pos) || pos.getSquaredDistance(block) > 2500) continue;
            if (world.getBlockEntity(pos) instanceof PedestalBlockEntity rite
                    && rite.isSustained()
                    && rite.getActiveRitual().equals("dominion")) {
                if (rite.getBoundPlayers().contains(player.getUuid())) return false;
                covered = true;
            }
        }
        return covered;
    }

    public static boolean storage(World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof Inventory
                || world.getBlockState(pos).getBlock() instanceof EnderChestBlock;
    }

    public static void feedback(World world, PlayerEntity player, BlockPos pos, Direction face) {
        if (!(world instanceof ServerWorld server)) return;
        Vec3d point = Vec3d.ofCenter(pos).add(Vec3d.of(face.getVector()).multiply(.501));
        if (player.raycast(player.getBlockInteractionRange(), 1, false)
                        instanceof BlockHitResult hit
                && hit.getBlockPos().equals(pos)) {
            face = hit.getSide();
            point = hit.getPos().add(Vec3d.of(face.getVector()).multiply(.008));
        }
        Vec3d normal = Vec3d.of(face.getVector());
        server.spawnParticles(
                ModParticles.DOMINION_RUNE,
                point.x,
                point.y,
                point.z,
                0,
                normal.x,
                normal.y,
                normal.z,
                1);
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register(
                (player, world, hand, hit) -> {
                    if (player.isSpectator()
                            || !storage(world, hit.getBlockPos())
                            || !restricted(world, hit.getBlockPos(), player))
                        return ActionResult.PASS;
                    feedback(world, player, hit.getBlockPos(), hit.getSide());
                    return ActionResult.FAIL;
                });
        AttackBlockCallback.EVENT.register(
                (player, world, hand, pos, face) -> {
                    if (!player.isSpectator() && restricted(world, pos, player))
                        feedback(world, player, pos, face);
                    return ActionResult.PASS;
                });
    }

    private Dominion() {}
}
