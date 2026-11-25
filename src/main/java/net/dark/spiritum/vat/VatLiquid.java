package net.dark.spiritum.vat;

import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class VatLiquid extends FlowableFluid {

    private final Item bucket;

    public VatLiquid(Item bucket) {
        super();
        this.bucket = bucket;
    }

    private final RegistryEntry.Reference<Fluid> registryEntry = SpiritumRegistries.VAT_LIQUID.createEntry(this);

    @Override
    public RegistryEntry.Reference<Fluid> getRegistryEntry() {
        return registryEntry;
    }

    @Override
    public Fluid getFlowing() {
        return this;
    }

    @Override
    public Fluid getStill() {
        return this;
    }

    @Override
    public boolean isIn(TagKey<Fluid> tag) {
        return false;
    }

    @Override
    public Item getBucketItem() {
        return bucket;
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {

    }

    @Nullable
    @Override
    public ParticleEffect getParticle() {
        return null;
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
    }

    @Override
    protected void onEntityCollision(World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        handler.addEvent(CollisionEvent.EXTINGUISH);
    }

    @Override
    public int getMaxFlowDistance(WorldView world) {
        return 4;
    }

    @Override
    public BlockState toBlockState(FluidState state) {
        return Blocks.WATER.getDefaultState();
    }

    @Override
    public boolean isStill(FluidState state) {
        return true;
    }

    @Override
    public int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 7;
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
    }
}
