package net.dark.spiritum.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.UUID;

public abstract class OwnedDemonEntity extends PathAwareEntity {
    private static final TrackedData<String> OWNER =
            DataTracker.registerData(OwnedDemonEntity.class, TrackedDataHandlerRegistry.STRING);

    protected OwnedDemonEntity(EntityType<? extends OwnedDemonEntity> type, World world) {
        super(type, world);
        setPersistent();
        if (isFlyingDemon()) {
            setNoGravity(true);
            moveControl = new FlightMoveControl(this, 20, true);
        }
    }

    protected boolean isFlyingDemon() {
        return false;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        if (isFlyingDemon()) {
            BirdNavigation navigation = new BirdNavigation(this, world);
            navigation.setCanSwim(true);
            return navigation;
        }
        return super.createNavigation(world);
    }

    public static DefaultAttributeContainer.Builder attributes(double health, double attack) {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, health)
                .add(EntityAttributes.ATTACK_DAMAGE, attack)
                .add(EntityAttributes.MOVEMENT_SPEED, .3)
                .add(EntityAttributes.FLYING_SPEED, .45)
                .add(EntityAttributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OWNER, "");
    }

    public UUID getOwnerUuid() {
        try {
            String value = dataTracker.get(OWNER);
            return value.isEmpty() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException badData) {
            return null;
        }
    }

    public void setOwnerUuid(UUID id) {
        dataTracker.set(OWNER, id == null ? "" : id.toString());
        setPersistent();
    }

    public PlayerEntity getOwner() {
        UUID id = getOwnerUuid();
        return id == null ? null : getEntityWorld().getPlayerByUuid(id);
    }

    public boolean allied(LivingEntity target) {
        UUID owner = getOwnerUuid();
        return target == this
                || owner != null
                        && (owner.equals(target.getUuid())
                                || target instanceof OwnedDemonEntity demon
                                        && owner.equals(demon.getOwnerUuid())
                                || target instanceof TameableEntity tame
                                        && tame.getOwner() != null
                                        && owner.equals(tame.getOwner().getUuid()));
    }

    protected LivingEntity combatTarget(PlayerEntity owner) {
        LivingEntity target = getTarget();
        if (target != null
                && target.isAlive()
                && !allied(target)
                && target.squaredDistanceTo(owner) < 4096
                && (!(target instanceof PlayerEntity)
                        || !(getEntityWorld() instanceof ServerWorld server)
                        || server.isPvpEnabled())) return target;
        target = owner.age - owner.getLastAttackTime() <= 200 ? owner.getAttacking() : null;
        if (target == null || !target.isAlive())
            target = owner.age - owner.getLastAttackedTime() <= 200 ? owner.getAttacker() : null;
        if (target == null || !target.isAlive())
            target = age - getLastAttackedTime() <= 200 ? getAttacker() : null;
        if (target == null || !target.isAlive() || allied(target)) {
            target =
                    getEntityWorld()
                            .getEntitiesByClass(
                                    MobEntity.class,
                                    owner.getBoundingBox().expand(12),
                                    m ->
                                            m.isAlive()
                                                    && m.getType().getSpawnGroup()
                                                            == SpawnGroup.MONSTER
                                                    && !allied(m))
                            .stream()
                            .min(Comparator.comparingDouble(m -> m.squaredDistanceTo(owner)))
                            .orElse(null);
        }
        if (target != null && allied(target)) target = null;
        if (target instanceof PlayerEntity
                && getEntityWorld() instanceof ServerWorld server
                && !server.isPvpEnabled()) target = null;
        setTarget(target);
        return target;
    }

    protected void approach(Vec3d destination, double speed) {
        if (age % 10 == 0 || getNavigation().isIdle())
            getNavigation().startMovingTo(destination.x, destination.y, destination.z, speed);
        getLookControl().lookAt(destination.x, destination.y, destination.z);
    }

    protected void follow(PlayerEntity owner) {
        double distance = squaredDistanceTo(owner);
        if (distance > 576) {
            BlockPos candidate = owner.getBlockPos().add(2, isFlyingDemon() ? 1 : 0, 0);
            Vec3d location = Vec3d.ofBottomCenter(candidate);
            if (getEntityWorld()
                    .isSpaceEmpty(
                            this, getBoundingBox().offset(location.subtract(getEntityPos())))) {
                refreshPositionAndAngles(location.x, location.y, location.z, getYaw(), getPitch());
                getNavigation().stop();
                return;
            }
        }
        double angle = age * .025 + (getId() % 8) * Math.PI / 4;
        Vec3d destination =
                isFlyingDemon()
                        ? owner.getEntityPos()
                                .add(
                                        Math.cos(angle) * 1.8,
                                        1.1 + Math.sin(angle) * .2,
                                        Math.sin(angle) * 1.8)
                        : owner.getEntityPos();
        if (isFlyingDemon() || distance > 4) approach(destination, 1.1);
        else getNavigation().stop();
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (getOwnerUuid() == null && player.isCreative()) {
            if (!getEntityWorld().isClient()) {
                setOwnerUuid(player.getUuid());
                player.sendMessage(Text.translatable("message.spiritum.demon_claimed"), true);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean handleFallDamage(double distance, float multiplier, DamageSource source) {
        return !isFlyingDemon() && super.handleFallDamage(distance, multiplier, source);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putNullable("SpiritumOwner", Uuids.CODEC, getOwnerUuid());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        setOwnerUuid(view.read("SpiritumOwner", Uuids.CODEC).orElse(null));
    }
}
