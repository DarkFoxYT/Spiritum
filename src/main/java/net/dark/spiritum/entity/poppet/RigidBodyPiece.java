package net.dark.spiritum.entity.poppet;

import net.minecraft.util.math.*;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Rigid-body math adapted from Asterion's ragdolls. */
public final class RigidBodyPiece {
    public Vec3d position, velocity, angularVelocity;
    public final Vec3d half;
    public final double mass;
    public final Quaternionf orientation = new Quaternionf();
    public Vec3d childAnchor = Vec3d.ZERO, parentAnchor = Vec3d.ZERO;

    public RigidBodyPiece(Vec3d position, Vec3d velocity, Vec3d half, double mass, Vec3d spin) {
        this.position = position;
        this.velocity = velocity;
        this.half = half;
        this.mass = mass;
        this.angularVelocity = spin;
    }

    public Vec3d rotate(Vec3d local) {
        Vector3f v =
                orientation.transform(
                        new Vector3f((float) local.x, (float) local.y, (float) local.z));
        return new Vec3d(v.x, v.y, v.z);
    }

    public Box bounds() {
        Vec3d x = rotate(new Vec3d(half.x, 0, 0)),
                y = rotate(new Vec3d(0, half.y, 0)),
                z = rotate(new Vec3d(0, 0, half.z));
        Vec3d h =
                new Vec3d(
                        Math.abs(x.x) + Math.abs(y.x) + Math.abs(z.x),
                        Math.abs(x.y) + Math.abs(y.y) + Math.abs(z.y),
                        Math.abs(x.z) + Math.abs(y.z) + Math.abs(z.z));
        return new Box(position.subtract(h), position.add(h));
    }

    public Vec3d inverseInertia(Vec3d torque) {
        Vector3f local =
                new Quaternionf(orientation)
                        .conjugate()
                        .transform(
                                new Vector3f((float) torque.x, (float) torque.y, (float) torque.z));
        double ix = Math.max(.002, mass / 3 * (half.y * half.y + half.z * half.z));
        double iy = Math.max(.002, mass / 3 * (half.x * half.x + half.z * half.z));
        double iz = Math.max(.002, mass / 3 * (half.x * half.x + half.y * half.y));
        local.set((float) (local.x / ix), (float) (local.y / iy), (float) (local.z / iz));
        orientation.transform(local);
        return new Vec3d(local.x, local.y, local.z);
    }

    public void impulse(Vec3d point, Vec3d impulse) {
        velocity = velocity.add(impulse.multiply(1 / mass));
        angularVelocity =
                angularVelocity.add(inverseInertia(point.subtract(position).crossProduct(impulse)));
        if (angularVelocity.lengthSquared() > .81)
            angularVelocity = angularVelocity.normalize().multiply(.9);
    }

    public static Vec3d reflect(Vec3d velocity, Vec3d normal) {
        Vec3d normalPart = normal.multiply(velocity.dotProduct(normal));
        return velocity.subtract(normalPart).multiply(.78).subtract(normalPart.multiply(.25));
    }
}
