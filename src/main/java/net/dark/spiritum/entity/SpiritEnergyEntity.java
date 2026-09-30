package net.dark.spiritum.entity;

import net.dark.spiritum.magic.InteractionEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/** A healing pickup, never an XP orb: only the recorded summoner can collect it. */
public class SpiritEnergyEntity extends Entity implements FlyingItemEntity {
    private UUID ownerUuid;
    private int lifeTicks = 200;

    public SpiritEnergyEntity(EntityType<? extends SpiritEnergyEntity> type, World world) {
        super(type, world);
        setNoGravity(true);
    }

    public void setOwnerUuid(UUID id) {
        ownerUuid = id;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getLifeTicks() {
        return lifeTicks;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    public ItemStack getStack() {
        return new ItemStack(Items.GLOWSTONE_DUST);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld world)) return;
        if (--lifeTicks <= 0) {
            discard();
            return;
        }
        var owner = ownerUuid == null ? null : world.getPlayerByUuid(ownerUuid);
        if (owner != null
                && owner.isAlive()
                && owner.getBoundingBox().expand(.6).intersects(getBoundingBox())) {
            owner.heal(2);
            InteractionEffects.atHand(owner, false);
            discard();
            return;
        }
        if (owner != null && owner.squaredDistanceTo(this) < 16) {
            Vec3d movement =
                    owner.getEntityPos()
                            .add(0, .7, 0)
                            .subtract(getEntityPos())
                            .normalize()
                            .multiply(.08);
            move(MovementType.SELF, movement);
        }
        if (age % 5 == 0)
            world.spawnParticles(
                    ParticleTypes.END_ROD, getX(), getY(), getZ(), 1, .03, .03, .03, 0);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putNullable("SpiritumOwner", Uuids.CODEC, ownerUuid);
        view.putInt("LifeTicks", lifeTicks);
    }

    @Override
    protected void readCustomData(ReadView view) {
        ownerUuid = view.read("SpiritumOwner", Uuids.CODEC).orElse(null);
        lifeTicks = view.getInt("LifeTicks", 200);
    }
}
