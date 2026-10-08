package net.dark.spiritum.entity;

import net.dark.spiritum.magic.Vigilance;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
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
    private Vec3d home;
    private float homeYaw;
    private static final double THRUST_SPEED = .55;

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
                .add(EntityAttributes.MOVEMENT_SPEED, .20)
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

    public void setHome(Vec3d position, float yaw) {
        home = position;
        homeYaw = yaw;
        setYaw(yaw);
        setBodyYaw(yaw);
        setHeadYaw(yaw);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.isSneaking() && !player.isSpectator() && !isRemoved()) {
            if (!getEntityWorld().isClient()) {
                player.getInventory().offerOrDrop(new ItemStack(ModContent.SENTINEL));
                discard();
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void returnHome() {
        if (getEntityPos().squaredDistanceTo(home) > .04) {
            dataTracker.set(ACTIVE, true);
            if (getEntityPos().squaredDistanceTo(home) <= 1) {
                getNavigation().stop();
                getMoveControl().moveTo(home.x, home.y, home.z, 1);
            } else getNavigation().startMovingTo(home.x, home.y, home.z, 0, 1);
            getLookControl().lookAt(home.x, home.y + getStandingEyeHeight(), home.z);
            return;
        }
        getNavigation().stop();
        setVelocity(0, getVelocity().y, 0);
        float yaw = MathHelper.stepUnwrappedAngleTowards(getYaw(), homeYaw, 10);
        setYaw(yaw);
        setBodyYaw(yaw);
        setHeadYaw(yaw);
        dataTracker.set(ACTIVE, false);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        if (home == null) setHome(getEntityPos(), getYaw());
        LivingEntity target = getTarget();
        if (target == null
                || !target.isAlive()
                || target.getEntityWorld() != world
                || !Vigilance.permits(this, target, defendedPlayer)) {
            setTarget(null);
            dataTracker.set(ATTACK, 0);
            attackTicks = 0;
            dataTracker.set(ATTACK_TICKS, 0);
            defendedPlayer = null;
            returnHome();
            return;
        }
        getLookControl().lookAt(target, 30, 30);
        if (cooldown > 0) cooldown--;
        double distance = squaredDistanceTo(target);
        if (attackTicks > 0) {
            attackTicks--;
            dataTracker.set(ATTACK_TICKS, attackTicks);
            if (attackPose() == 2 && !thrustHit)
                setVelocity(thrustDirection.multiply(THRUST_SPEED).add(0, getVelocity().y, 0));
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
            setVelocity(direction.multiply(THRUST_SPEED).add(0, .12, 0));
            velocityDirty = true;
            playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1, 1.2f);
        } else getNavigation().startMovingTo(target, 1);
    }

    private void hit(ServerWorld world, LivingEntity target, float damage, double knockback) {
        if (target.damage(world, world.getDamageSources().mobAttack(this), damage))
            target.takeKnockback(knockback, getX() - target.getX(), getZ() - target.getZ());
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (home != null) view.put("Placement", Vec3d.CODEC, home);
        view.putFloat("PlacementYaw", homeYaw);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        home = view.read("Placement", Vec3d.CODEC).orElse(getEntityPos());
        homeYaw = view.getFloat("PlacementYaw", getYaw());
    }

    @Override
    public boolean canImmediatelyDespawn(double distance) {
        return false;
    }
}
