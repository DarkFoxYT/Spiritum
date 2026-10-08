package net.dark.spiritum.entity.poppet;

import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Geometry and joint pivots imported from tools/models/Poppet1.bbmodel. */
public final class PoppetModelDefinition {
    public record Part(
            String name,
            Vec3d offset,
            Vec3d half,
            Vec3d joint,
            float restRoll,
            int u,
            int v,
            boolean mirrored) {}

    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;
    public static final List<Part> PARTS =
            List.of(
                    new Part(
                            "body",
                            new Vec3d(0.000000000, 0.000000000, -0.000000000),
                            new Vec3d(0.125000000, 0.187500000, 0.093750000),
                            new Vec3d(0.000000000, 0.054687500, -0.007812500),
                            -0.000000000f,
                            0,
                            0,
                            false),
                    new Part(
                            "head",
                            new Vec3d(0.000000000, 0.375000000, -0.031250000),
                            new Vec3d(0.187500000, 0.187500000, 0.187500000),
                            new Vec3d(0.000000000, 0.187500000, -0.000000000),
                            -0.000000000f,
                            0,
                            12,
                            false),
                    new Part(
                            "leftarm",
                            new Vec3d(-0.250000000, 0.031250000, -0.000000000),
                            new Vec3d(0.125000000, 0.093750000, 0.093750000),
                            new Vec3d(-0.125000000, 0.062500000, -0.000000000),
                            -0.000000000f,
                            0,
                            24,
                            false),
                    new Part(
                            "rightarm",
                            new Vec3d(0.250000000, 0.031250000, -0.000000000),
                            new Vec3d(0.125000000, 0.093750000, 0.093750000),
                            new Vec3d(0.125000000, 0.062500000, -0.000000000),
                            -0.000000000f,
                            0,
                            24,
                            true),
                    new Part(
                            "leftleg",
                            new Vec3d(-0.107011338, -0.334984571, -0.000000000),
                            new Vec3d(0.093750000, 0.156250000, 0.093750000),
                            new Vec3d(-0.062500000, -0.187500000, 0.031250000),
                            -0.087266463f,
                            15,
                            4,
                            false),
                    new Part(
                            "rightleg",
                            new Vec3d(0.107011338, -0.334984571, -0.000000000),
                            new Vec3d(0.093750000, 0.156250000, 0.093750000),
                            new Vec3d(0.062500000, -0.187500000, 0.031250000),
                            0.087266463f,
                            15,
                            4,
                            true));

    private PoppetModelDefinition() {}
}
