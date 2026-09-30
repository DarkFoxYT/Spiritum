package net.dark.spiritum.entity;

import net.dark.spiritum.registry.ModEntities;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LeechDemonEntity extends OwnedDemonEntity {
    private static final TrackedData<Boolean> LATCHED =
            DataTracker.registerData(LeechDemonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private LivingEntity latchedTarget;
    private int drainTicks;
    private int latchCooldown;

    public LeechDemonEntity(EntityType<? extends LeechDemonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isFlyingDemon() {
        return true;
    }

    public boolean isLatched() {
        return dataTracker.get(LATCHED);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LATCHED, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld server) || !isAlive()) return;
        if (age % 5 == 0) server.spawnParticles(net.dark.spiritum.registry.ModParticles.HEXFLAME, getX(), getY() + .2, getZ(), 2, .1, .1, .1, 0);
        if (latchCooldown > 0) latchCooldown--;
        var owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            unlatch();
            getNavigation().stop();
            setVelocity(Vec3d.ZERO);
            return;
        }
        LivingEntity target = combatTarget(owner);
        if (latchedTarget != null
                && (target != latchedTarget || !latchedTarget.isAlive() || allied(latchedTarget)))
            unlatch();
        if (latchedTarget != null) {
            getNavigation().stop();
            setVelocity(Vec3d.ZERO);
            Vec3d anchor =
                    latchedTarget
                            .getEntityPos()
                            .add(0, Math.min(1, latchedTarget.getHeight() * .65), 0);
            refreshPositionAndAngles(anchor.x, anchor.y, anchor.z, getYaw(), getPitch());
            if (++drainTicks >= 100) {
                drainTicks = 0;
                if (latchedTarget.damage(
                        server, DemonDamage.source(server, DemonDamage.LEECH, this), 2f)) {
                    SpiritEnergyEntity orb =
                            ModEntities.SPIRIT_ENERGY.create(server, SpawnReason.TRIGGERED);
                    if (orb != null) {
                        orb.setOwnerUuid(getOwnerUuid());
                        orb.refreshPositionAndAngles(anchor.x + .4, anchor.y + .2, anchor.z, 0, 0);
                        server.spawnEntity(orb);
                    }
                }
            }
        } else if (target != null) {
            approach(target.getEntityPos().add(0, target.getHeight() * .5, 0), 1.4);
            if (latchCooldown == 0 && squaredDistanceTo(target) <= 2.25 && canSee(target)) {
                latchedTarget = target;
                dataTracker.set(LATCHED, true);
                drainTicks = 0;
                getNavigation().stop();
            }
        } else follow(owner);
    }

    private void unlatch() {
        latchedTarget = null;
        dataTracker.set(LATCHED, false);
        drainTicks = 0;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        boolean damaged = super.damage(world, source, amount);
        if (damaged) {
            unlatch();
            latchCooldown = 40;
            setVelocity(getVelocity().add(0, .15, 0));
        }
        return damaged;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("LatchCooldown", latchCooldown);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        latchCooldown = view.getInt("LatchCooldown", 0);
        unlatch();
    }
}
