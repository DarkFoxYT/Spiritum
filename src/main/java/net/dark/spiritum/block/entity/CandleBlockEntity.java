package net.dark.spiritum.block.entity;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.magic.InteractionEffects;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.registry.ModParticles;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.*;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.UUID;

public class CandleBlockEntity extends BlockEntity {
    private int remainingTicks;
    private BlockPos ritualOwner;
    private UUID boundPlayer;

    public CandleBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CANDLE_ENTITY, pos, state);
    }

    public int flame() {
        return getCachedState().get(HexedCandleBlock.FLAME);
    }

    public UUID getBoundPlayer() {
        return boundPlayer;
    }

    public void bind(UUID player) {
        boundPlayer = player;
        markDirty();
    }

    public void light(int flame) {
        if (world == null) return;
        remainingTicks = flame == 3 ? 1200 : 0;
        ritualOwner = null;
        boundPlayer = null;
        world.setBlockState(
                pos, getCachedState().with(HexedCandleBlock.FLAME, flame), Block.NOTIFY_ALL);
        markDirty();
        InteractionEffects.magic(world, Vec3d.ofCenter(pos).add(0, .55, 0), flame == 5);
    }

    public boolean claim(BlockPos owner) {
        if (flame() < 4 || ritualOwner != null && !ritualOwner.equals(owner)) return false;
        ritualOwner = owner.toImmutable();
        markDirty();
        return true;
    }

    public boolean availableFor(BlockPos owner) {
        return flame() >= 4 && (ritualOwner == null || ritualOwner.equals(owner));
    }

    public void release(BlockPos owner) {
        if (owner.equals(ritualOwner)) {
            ritualOwner = null;
            markDirty();
        }
    }

    public void snuff() {
        if (world == null) return;
        boolean fragile = flame() == 3;
        InteractionEffects.snuff(world, pos);
        ritualOwner = null;
        remainingTicks = 0;
        boundPlayer = null;
        world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
        if (fragile) world.removeBlock(pos, false);
        else
            world.setBlockState(
                    pos, getCachedState().with(HexedCandleBlock.FLAME, 0), Block.NOTIFY_ALL);
        markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, CandleBlockEntity candle) {
        int flame = candle.flame();
        if (flame == 0) return;
        ServerWorld server = (ServerWorld) world;
        if (flame == 3 && --candle.remainingTicks <= 0) {
            candle.snuff();
            return;
        }
        if (flame == 3 && world.getTime() % 20 == 0) candle.markDirty();
        if (world.getTime() % 5 == 0)
            server.spawnParticles(
                    flame == 5
                            ? ModParticles.BIG_HEXFLAME
                            : flame == 2 ? ParticleTypes.SOUL_FIRE_FLAME : flame >= 4
                                    ? ModParticles.HEXFLAME
                                    : ParticleTypes.FLAME,
                    pos.getX() + .5,
                    pos.getY() + .97,
                    pos.getZ() + .5,
                    flame == 5 ? 4 : 2,
                    .025,
                    .03,
                    .025,
                    0);
        if (flame == 5 && world.getTime() % 10 == 0)
            server.spawnParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + .5,
                    pos.getY() + 1.12,
                    pos.getZ() + .5,
                    1,
                    .06,
                    .1,
                    .06,
                    .01);
        if (world.getTime() % 40 == 0 && (flame == 1 || flame == 2)) {
            Vec3d center = Vec3d.ofCenter(pos);
            for (LivingEntity entity :
                    world.getEntitiesByClass(
                            LivingEntity.class,
                            new Box(pos).expand(3),
                            e -> e.isAlive() && e.squaredDistanceTo(center) <= 9)) {
                if (flame == 1) entity.setOnFireFor(3);
                else
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0));
            }
        }
        if (candle.ritualOwner != null
                && world.getTime() % 40 == 0
                && world.isChunkLoaded(candle.ritualOwner)
                && !(world.getBlockEntity(candle.ritualOwner) instanceof PedestalBlockEntity)) {
            candle.ritualOwner = null;
            candle.markDirty();
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        remainingTicks = view.getInt("RemainingTicks", 0);
        ritualOwner = view.read("RitualOwner", BlockPos.CODEC).orElse(null);
        boundPlayer = view.read("BoundPlayer", Uuids.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("RemainingTicks", remainingTicks);
        view.putNullable("RitualOwner", BlockPos.CODEC, ritualOwner);
        view.putNullable("BoundPlayer", Uuids.CODEC, boundPlayer);
    }
}
