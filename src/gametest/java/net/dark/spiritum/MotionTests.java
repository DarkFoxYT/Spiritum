package net.dark.spiritum;

import net.dark.spiritum.entity.PoppetEntity;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.registry.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MotionTests {
    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 180)
    public void poppetsRestAndFallWhenFloorIsRemoved(TestContext test) {
        for (int x = 0; x < 8; x++)
            for (int z = 0; z < 8; z++) test.setBlockState(x, 0, z, Blocks.STONE);
        var player = test.createMockCreativeServerPlayerInWorld();
        PoppetEntity[] dolls = new PoppetEntity[2];
        for (int i = 0; i < dolls.length; i++) {
            dolls[i] = test.spawnEntity(ModEntities.POPPET, new Vec3d(2, 3, 2 + i * 3));
            dolls[i].launch(new ItemStack(ModContent.VOODOO_POPPET), player,
                    i == 0 ? Vec3d.ZERO : new Vec3d(.2, .1, 0));
        }
        Vec3d[] roots = new Vec3d[2];
        Vector3f[][] offsets = new Vector3f[2][6];
        Quaternionf[][] rotations = new Quaternionf[2][6];
        test.runAtTick(120, () -> {
            for (int d = 0; d < dolls.length; d++) {
                roots[d] = dolls[d].getEntityPos();
                for (int p = 0; p < 6; p++) {
                    offsets[d][p] = new Vector3f(dolls[d].partPosition(p));
                    rotations[d][p] = new Quaternionf(dolls[d].partRotation(p));
                }
            }
        });
        test.runAtTick(150, () -> {
            for (int d = 0; d < dolls.length; d++) {
                test.assertEquals(roots[d], dolls[d].getEntityPos(), "Landed doll stays still: " + d);
                test.assertEquals(Vec3d.ZERO, dolls[d].getVelocity(), "Resting doll has no velocity");
                for (int p = 0; p < 6; p++) {
                    test.assertTrue(offsets[d][p].equals(dolls[d].partPosition(p)), "Resting limb stops drifting: " + p);
                    test.assertTrue(rotations[d][p].equals(dolls[d].partRotation(p)), "Resting limb stops spinning: " + p);
                }
            }
            dolls[1].setVelocity(new Vec3d(.1, .2, 0));
            for (int x = 0; x < 8; x++)
                for (int z = 0; z < 8; z++) test.removeBlock(new BlockPos(x, 0, z));
        });
        test.runAtTick(152, () ->
                test.assertTrue(dolls[1].getY() > roots[1].y + .05,
                        "An external impulse wakes and moves a sleeping doll"));
        test.runAtTick(170, () -> {
            for (int d = 0; d < dolls.length; d++)
                test.assertTrue(dolls[d].getY() < roots[d].y - .1, "Removing support wakes the doll");
            test.getWorld().getServer().getPlayerManager().remove(player);
            test.complete();
        });
    }
}
