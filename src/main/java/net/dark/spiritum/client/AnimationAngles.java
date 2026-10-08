package net.dark.spiritum.client;

public final class AnimationAngles {
    private AnimationAngles() {}

    public static float pedestal(long ticks, float tickProgress) {
        // Reduce the world clock before converting to float, preserving sub-tick motion.
        return (float) ((ticks % 180L + (double) tickProgress) * 2.0);
    }

    public static float sigil(long ticks) {
        return (float) ((ticks * .008d) % (Math.PI * 2));
    }
}
