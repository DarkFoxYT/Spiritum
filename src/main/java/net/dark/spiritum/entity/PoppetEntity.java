package net.dark.spiritum.entity;

import net.dark.spiritum.entity.poppet.RigidBodyPiece;
import net.dark.spiritum.item.*;
import net.dark.spiritum.magic.RingMagic;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

/** A thrown poppet, with limb poses synced from the server. */
public class PoppetEntity extends Entity {
    private static final TrackedData<ItemStack> STACK =
            DataTracker.registerData(PoppetEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final List<TrackedData<Vector3fc>> POSITIONS = new ArrayList<>();
    private static final List<TrackedData<Quaternionfc>> ROTATIONS = new ArrayList<>();

    static {
        for (int i = 0; i < 6; i++) {
            POSITIONS.add(
                    DataTracker.registerData(
                            PoppetEntity.class, TrackedDataHandlerRegistry.VECTOR_3F));
            ROTATIONS.add(
                    DataTracker.registerData(
                            PoppetEntity.class, TrackedDataHandlerRegistry.QUATERNION_F));
        }
    }

    // Torso, head, arms, legs. Dimensions are in blocks, with Y pointing upwards.
    private static final Vec3d[] REST = {
        Vec3d.ZERO,
        new Vec3d(0, .23, 0),
        new Vec3d(-.2, 0, 0),
        new Vec3d(.2, 0, 0),
        new Vec3d(-.08, -.3, 0),
        new Vec3d(.08, -.3, 0)
    };
    private static final Vec3d[] HALF = {
        new Vec3d(.12, .15, .07),
        new Vec3d(.1, .08, .08),
        new Vec3d(.06, .14, .06),
        new Vec3d(.06, .14, .06),
        new Vec3d(.055, .15, .06),
        new Vec3d(.055, .15, .06)
    };
    private final RigidBodyPiece[] pieces = new RigidBodyPiece[6];
    private UUID thrower;
    private int impactCooldown;

    public PoppetEntity(EntityType<? extends PoppetEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(STACK, ItemStack.EMPTY);
        for (int i = 0; i < 6; i++) {
            builder.add(POSITIONS.get(i), new Vector3f());
            builder.add(ROTATIONS.get(i), new Quaternionf());
        }
    }

    public ItemStack getStack() {
        return dataTracker.get(STACK);
    }

    public Vector3fc partPosition(int index) {
        return dataTracker.get(POSITIONS.get(index));
    }

    public Quaternionfc partRotation(int index) {
        return dataTracker.get(ROTATIONS.get(index));
    }

    public void launch(ItemStack stack, PlayerEntity user, Vec3d velocity) {
        dataTracker.set(STACK, stack.copyWithCount(1));
        thrower = user.getUuid();
        initializeBodies(velocity);
        syncPose();
    }

    private void initializeBodies(Vec3d velocity) {
        for (int i = 0; i < 6; i++) {
            pieces[i] =
                    new RigidBodyPiece(
                            getEntityPos().add(REST[i]),
                            velocity,
                            HALF[i],
                            i == 0 ? .8 : .2,
                            new Vec3d(.12, .03 * (i - 2), .08 * (i % 2 == 0 ? 1 : -1)));
            if (i > 0) {
                pieces[i].parentAnchor =
                        i == 1
                                ? new Vec3d(0, .15, 0)
                                : i < 4
                                        ? new Vec3d(REST[i].x < 0 ? -.12 : .12, .1, 0)
                                        : new Vec3d(REST[i].x, -.15, 0);
                pieces[i].childAnchor = pieces[i].parentAnchor.subtract(REST[i]);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld world)) {
            Box box = null;
            for (int i = 0; i < 6; i++) {
                Vector3fc p = partPosition(i);
                var body =
                        new RigidBodyPiece(
                                getEntityPos().add(p.x(), p.y(), p.z()),
                                Vec3d.ZERO,
                                HALF[i],
                                1,
                                Vec3d.ZERO);
                body.orientation.set(partRotation(i));
                box = box == null ? body.bounds() : box.union(body.bounds());
            }
            setBoundingBox(box.expand(.08));
            return;
        }
        if (getStack().isEmpty()) {
            discard();
            return;
        }
        if (pieces[0] == null) initializeBodies(getVelocity());
        if (impactCooldown > 0) impactCooldown--;
        double impact = 0;
        // Substeps keep fast throws from tunneling through walls.
        for (int step = 0; step < 4; step++) {
            for (RigidBodyPiece part : pieces) {
                part.velocity = part.velocity.add(0, -.04 / 4, 0).multiply(.997);
                Quaternionf oldOrientation = new Quaternionf(part.orientation);
                part.orientation
                        .rotateLocalX((float) (part.angularVelocity.x / 4))
                        .rotateLocalY((float) (part.angularVelocity.y / 4))
                        .rotateLocalZ((float) (part.angularVelocity.z / 4))
                        .normalize();
                if (!world.isSpaceEmpty(this, part.bounds())) part.orientation.set(oldOrientation);
                part.angularVelocity = part.angularVelocity.multiply(.985);
                Vec3d requested = part.velocity.multiply(.25);
                Vec3d allowed =
                        adjustMovementForCollisions(
                                this, requested, part.bounds(), world, List.of());
                part.position = part.position.add(allowed);
                Vec3d normal = Vec3d.ZERO;
                if (Math.abs(allowed.x - requested.x) > 1e-6) {
                    impact = Math.max(impact, Math.abs(part.velocity.x));
                    normal = new Vec3d(-Math.signum(requested.x), 0, 0);
                }
                if (Math.abs(allowed.z - requested.z) > 1e-6) {
                    impact = Math.max(impact, Math.abs(part.velocity.z));
                    normal = new Vec3d(0, 0, -Math.signum(requested.z));
                }
                if (Math.abs(allowed.y - requested.y) > 1e-6) {
                    part.velocity =
                            new Vec3d(
                                    part.velocity.x * .8,
                                    -part.velocity.y * .15,
                                    part.velocity.z * .8);
                    part.angularVelocity = part.angularVelocity.multiply(.7);
                }
                if (normal.lengthSquared() > 0) {
                    Vec3d before = part.velocity;
                    part.velocity = RigidBodyPiece.reflect(before, normal);
                    part.impulse(
                            part.position.add(0, .06, 0),
                            normal.multiply(Math.abs(before.dotProduct(normal)) * .025));
                }
            }
            for (int iteration = 0; iteration < 6; iteration++) solveJoints(world);
        }
        setPosition(pieces[0].position);
        setVelocity(pieces[0].velocity);
        if (impact > .15 && impactCooldown == 0) {
            wallImpact(world, impact);
            impactCooldown = 10;
        }
        syncPose();
        if (getY() < world.getBottomY() - 32) {
            world.spawnEntity(
                    new ItemEntity(
                            world, getX(), world.getBottomY() + 1, getZ(), getStack().copy()));
            discard();
        }
    }

    /**
     * Adapted from Asterion DismembermentEngine.solveJoints: sockets and inverse-mass corrections.
     */
    private void solveJoints(ServerWorld world) {
        RigidBodyPiece parent = pieces[0];
        for (int i = 1; i < 6; i++) {
            RigidBodyPiece child = pieces[i];
            Vec3d delta =
                    child.position
                            .add(child.rotate(child.childAnchor))
                            .subtract(parent.position.add(parent.rotate(child.parentAnchor)));
            double length = Math.max(1e-5, delta.length());
            Vec3d correction = delta.multiply(Math.min(length * .82, .025) / length);
            double childWeight = (1 / child.mass) / (1 / child.mass + 1 / parent.mass);
            Vec3d childMove = correction.multiply(-childWeight),
                    parentMove = correction.multiply(1 - childWeight);
            child.position =
                    child.position.add(
                            adjustMovementForCollisions(
                                    this, childMove, child.bounds(), world, List.of()));
            parent.position =
                    parent.position.add(
                            adjustMovementForCollisions(
                                    this, parentMove, parent.bounds(), world, List.of()));
            Vec3d axis = delta.multiply(1 / length);
            double speed = child.velocity.subtract(parent.velocity).dotProduct(axis);
            Vec3d impulse = axis.multiply(-speed * .12);
            child.impulse(child.position.add(child.rotate(child.childAnchor)), impulse);
            parent.impulse(
                    parent.position.add(parent.rotate(child.parentAnchor)), impulse.multiply(-1));
            Vec3d relative = child.angularVelocity.subtract(parent.angularVelocity);
            child.angularVelocity =
                    child.angularVelocity.subtract(relative.multiply(.012)).multiply(.9985);
            // Asterion's angular projection keeps limbs inside a loose cone/twist range.
            Vector3f angles =
                    new Quaternionf(parent.orientation)
                            .conjugate()
                            .mul(child.orientation)
                            .getEulerAnglesXYZ(new Vector3f());
            float limit = i == 1 ? .7f : 1.5f;
            Quaternionf legal =
                    new Quaternionf(parent.orientation)
                            .mul(
                                    new Quaternionf()
                                            .rotationXYZ(
                                                    MathHelper.clamp(angles.x, -limit, limit),
                                                    MathHelper.clamp(angles.y, -limit, limit),
                                                    MathHelper.clamp(angles.z, -limit, limit)));
            Quaternionf original = new Quaternionf(child.orientation);
            child.orientation.slerp(legal, .14f).normalize();
            if (!world.isSpaceEmpty(this, child.bounds())) child.orientation.set(original);
        }
    }

    private void syncPose() {
        Box bounds = pieces[0].bounds();
        for (int i = 0; i < 6; i++) {
            Vec3d offset = pieces[i].position.subtract(getEntityPos());
            dataTracker.set(
                    POSITIONS.get(i),
                    new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
            dataTracker.set(ROTATIONS.get(i), new Quaternionf(pieces[i].orientation));
            bounds = bounds.union(pieces[i].bounds());
        }
        setBoundingBox(bounds.expand(.08));
    }

    private void wallImpact(ServerWorld world, double speed) {
        var id = SpiritBinding.player(SpiritBinding.socket(getStack()));
        var target =
                id.map(uuid -> world.getServer().getPlayerManager().getPlayer(uuid)).orElse(null);
        if (target == null || !target.isAlive() || RingMagic.warded(target)) return;
        var owner =
                thrower == null ? null : world.getServer().getPlayerManager().getPlayer(thrower);
        ServerWorld targetWorld = (ServerWorld) target.getEntityWorld();
        target.damage(
                targetWorld,
                targetWorld.getDamageSources().indirectMagic(this, owner),
                (float) Math.min(10, speed * 4));
    }

    @Override
    public boolean isInteractable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return !isRemoved();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.isSpectator() || isRemoved() || getStack().isEmpty()) return ActionResult.PASS;
        if (player.isSneaking()) {
            if (!getEntityWorld().isClient()) {
                player.getInventory().offerOrDrop(getStack().copy());
                discard();
            }
            return ActionResult.SUCCESS;
        }
        if (player.getStackInHand(hand).getItem() instanceof ArgentNeedleItem needle) {
            if (getEntityWorld().isClient()) return ActionResult.SUCCESS;
            ItemStack stack = getStack().copy();
            ActionResult result = needle.stab((ServerWorld) getEntityWorld(), player, hand, stack);
            dataTracker.set(STACK, stack);
            if (stack.isEmpty()) discard();
            return result;
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("Poppet", ItemStack.CODEC, getStack());
        view.putNullable("Thrower", Uuids.CODEC, thrower);
        view.putInt("ImpactCooldown", impactCooldown);
        if (pieces[0] != null)
            for (int i = 0; i < 6; i++) {
                WriteView part = view.get("Part" + i);
                part.put("Position", Vec3d.CODEC, pieces[i].position);
                part.put("Velocity", Vec3d.CODEC, pieces[i].velocity);
                part.put("Spin", Vec3d.CODEC, pieces[i].angularVelocity);
                Quaternionf q = pieces[i].orientation;
                part.putFloat("X", q.x);
                part.putFloat("Y", q.y);
                part.putFloat("Z", q.z);
                part.putFloat("W", q.w);
            }
    }

    @Override
    protected void readCustomData(ReadView view) {
        dataTracker.set(STACK, view.read("Poppet", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        thrower = view.read("Thrower", Uuids.CODEC).orElse(null);
        impactCooldown = view.getInt("ImpactCooldown", 0);
        initializeBodies(getVelocity());
        for (int i = 0; i < 6; i++) {
            ReadView part = view.getReadView("Part" + i);
            pieces[i].position = part.read("Position", Vec3d.CODEC).orElse(pieces[i].position);
            pieces[i].velocity = part.read("Velocity", Vec3d.CODEC).orElse(Vec3d.ZERO);
            pieces[i].angularVelocity = part.read("Spin", Vec3d.CODEC).orElse(Vec3d.ZERO);
            pieces[i]
                    .orientation
                    .set(
                            part.getFloat("X", 0),
                            part.getFloat("Y", 0),
                            part.getFloat("Z", 0),
                            part.getFloat("W", 1))
                    .normalize();
        }
        syncPose();
    }
}
