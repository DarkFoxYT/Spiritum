package net.dark.spiritum.entity;

import net.dark.spiritum.magic.Resonance;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.world.World;

import java.util.UUID;

public class SingingRodEntity extends PathAwareEntity {
    private static final TrackedData<String> GROUP =
            DataTracker.registerData(SingingRodEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> SEALED =
            DataTracker.registerData(SingingRodEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private UUID owner;

    public SingingRodEntity(EntityType<? extends SingingRodEntity> type, World world) {
        super(type, world);
        setPersistent();
        Resonance.watch(this);
    }

    public static DefaultAttributeContainer.Builder attributes() {
        return createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10)
                .add(EntityAttributes.ARMOR, 15)
                .add(EntityAttributes.MOVEMENT_SPEED, 0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(GROUP, "");
        builder.add(SEALED, false);
    }

    public void assign(UUID owner, String group) {
        this.owner = owner;
        dataTracker.set(GROUP, group);
    }

    public UUID owner() {
        return owner;
    }

    public String group() {
        return dataTracker.get(GROUP);
    }

    public boolean sealed() {
        return dataTracker.get(SEALED);
    }

    public void seal() {
        dataTracker.set(SEALED, true);
    }

    @Override
    public void tick() {
        if (getEntityWorld() instanceof ServerWorld server
                && !group().isEmpty()
                && server.getPersistentStateManager()
                        .getOrCreate(net.dark.spiritum.magic.DissolvedRodGroups.TYPE)
                        .contains(group())) {
            discard();
            return;
        }
        setNoGravity(true);
        setVelocity(0, 0, 0);
        super.tick();
        setVelocity(0, 0, 0);
        Resonance.watch(this);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        getNavigation().stop();
        setVelocity(0, 0, 0);
        if (age % 10 == 0) Resonance.particles(world, this);
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        super.onDeath(source);
        Resonance.dissolve(this);
    }

    @Override
    public void move(MovementType type, net.minecraft.util.math.Vec3d movement) {}

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void pushAwayFrom(Entity entity) {}

    @Override
    protected void pushAway(Entity entity) {}

    @Override
    public boolean canImmediatelyDespawn(double distance) {
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (owner != null) view.putString("RodOwner", owner.toString());
        view.putString("RodGroup", group());
        view.putBoolean("RodSealed", sealed());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        try {
            owner = UUID.fromString(view.getString("RodOwner", ""));
        } catch (IllegalArgumentException ignored) {
            owner = null;
        }
        dataTracker.set(GROUP, view.getString("RodGroup", ""));
        dataTracker.set(SEALED, view.getBoolean("RodSealed", false));
    }
}
