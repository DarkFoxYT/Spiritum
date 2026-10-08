package net.dark.spiritum;

import net.dark.spiritum.client.AnimationAngles;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.MathHelper;

public class AnimationTests {
    @GameTest
    public void pedestalKeepsSubTickRotationOnOldWorlds(TestContext test) {
        for (long age : new long[] {0, 1L << 24, 1L << 30, 1L << 40}) {
            float first = AnimationAngles.pedestal(age, .25f);
            float second = AnimationAngles.pedestal(age, .75f);
            test.assertTrue(
                    Math.abs(second - first - 1f) < .0001f,
                    "Pedestal must rotate between frames at world age " + age);
            test.assertTrue(first >= 0 && second <= 360, "Rotation stays within one turn");
        }
        test.complete();
    }

    @GameTest
    public void pedestalWrapDoesNotReverseRotation(TestContext test) {
        float before = AnimationAngles.pedestal(179, .75f);
        float after = AnimationAngles.pedestal(180, .25f);
        test.assertEquals(
                1f,
                MathHelper.wrapDegrees(after - before),
                "Crossing a full turn keeps the same direction and speed");
        test.complete();
    }

    @GameTest
    public void sigilsKeepSmallRotationStepsOnOldWorlds(TestContext test) {
        for (long age : new long[] {0, 1L << 24, 1L << 30, 1L << 40}) {
            float angle = AnimationAngles.sigil(age);
            test.assertTrue(angle >= 0 && angle < Math.PI * 2, "Sigil starts within one turn");
            test.assertTrue(
                    Math.abs((angle + .008f) - angle - .008f) < .000001f,
                    "Small sigil rotation steps must survive at world age " + age);
            double next = AnimationAngles.sigil(age + 1);
            double difference = Math.atan2(Math.sin(next - angle), Math.cos(next - angle));
            test.assertTrue(
                    Math.abs(difference - .008) < .00001,
                    "Sigils created on consecutive ticks keep the same rotation phase");
        }
        test.complete();
    }
}
