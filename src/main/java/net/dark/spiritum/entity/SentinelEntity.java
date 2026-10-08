package net.dark.spiritum.entity;

import net.dark.spiritum.magic.Vigilance;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class SentinelEntity extends PathAwareEntity {
    private static final TrackedData<Integer> ATTACK =
            DataTracker.registerData(SentinelEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> ACTIVE =
            DataTracker.registerData(SentinelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ATTACK_TICKS =
            DataTracker.registerData(SentinelEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int cooldown, attackTicks;
    private boolean thrustHit;
    private UUID defendedPlayer;
    private Vec3d thrustDirection = Vec3d.ZERO;

    public SentinelEntity(EntityType<? extends SentinelEntity> type, World world) {
        super(type, world);
        setPersistent();
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40)
                .add(EntityAttributes.ARMOR, 20)
                .add(EntityAttributes.ARMOR_TOUGHNESS, 12)
                .add(EntityAttributes.ATTACK_DAMAGE, 12)
                .add(EntityAttributes.MOVEMENT_SPEED, .28)
                .add(EntityAttributes.FOLLOW_RANGE, 50)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, .5);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACK, 0);
        builder.add(ACTIVE, false);
        builder.add(ATTACK_TICKS, 0);
    }

    public boolean isAwake() {
        return dataTracker.get(ACTIVE);
    }

    public int attackPose() {
        return dataTracker.get(ATTACK);
    }

    public int attackRemaining() {
        return dataTracker.get(ATTACK_TICKS);
    }

    public void defend(LivingEntity attacker, UUID player) {
        if (Vigilance.permits(this, attacker, player)) {
            defendedPlayer = player;
            setTarget(attacker);
            dataTracker.set(ACTIVE, true);
        }
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        LivingEntity target = getTarget();
        if (target == null
                || !target.isAlive()
                || target.getEntityWorld() != world
                || !Vigilance.permits(this, target, defendedPlayer)) {
            setTarget(null);
            getNavigation().stop();
            dataTracker.set(ACTIVE, false);
            dataTracker.set(ATTACK, 0);
            attackTicks = 0;
            dataTracker.set(ATTACK_TICKS, 0);
            defendedPlayer = null;
            setVelocity(0, getVelocity().y, 0);
            return;
        }
        getLookControl().lookAt(target, 30, 30);
        if (cooldown > 0) cooldown--;
        double distance = squaredDistanceTo(target);
        if (attackTicks > 0) {
            attackTicks--;
            dataTracker.set(ATTACK_TICKS, attackTicks);
            if (attackPose() == 2 && !thrustHit)
                setVelocity(thrustDirection.multiply(1.15).add(0, getVelocity().y, 0));
            if (attackPose() == 1 && attackTicks == 5 && distance <= 6.25 && canSee(target))
                hit(world, target, 12, .8);
            if (attackPose() == 2
                    && !thrustHit
                    && getBoundingBox().expand(.4).intersects(target.getBoundingBox())) {
                hit(world, target, 10, .5);
                thrustHit = true;
            }
            if (attackTicks == 0) dataTracker.set(ATTACK, 0);
            return;
        }
        if (cooldown == 0 && canSee(target) && distance <= 6.25) {
            getNavigation().stop();
            dataTracker.set(ATTACK, 1);
            attackTicks = 12;
            cooldown = 30;
            dataTracker.set(ATTACK_TICKS, attackTicks);
            playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1, .7f);
        } else if (cooldown == 0 && canSee(target) && distance >= 9 && distance <= 100) {
            getNavigation().stop();
            dataTracker.set(ATTACK, 2);
            attackTicks = 10;
            cooldown = 50;
            thrustHit = false;
            dataTracker.set(ATTACK_TICKS, attackTicks);
            Vec3d direction = target.getEntityPos().subtract(getEntityPos());
            direction = new Vec3d(direction.x, 0, direction.z).normalize();
            thrustDirection = direction;
            setVelocity(direction.multiply(1.15).add(0, .18, 0));
            velocityDirty = true;
            playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1, 1.2f);
        } else getNavigation().startMovingTo(target, 1.15);
    }

    private void hit(ServerWorld world, LivingEntity target, float damage, double knockback) {
        if (target.damage(world, world.getDamageSources().mobAttack(this), damage))
            target.takeKnockback(knockback, getX() - target.getX(), getZ() - target.getZ());
    }

    @Override
    public boolean canImmediatelyDespawn(double distance) {
        return false;
    }
}
