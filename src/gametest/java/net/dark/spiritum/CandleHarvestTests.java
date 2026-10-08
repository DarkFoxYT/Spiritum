package net.dark.spiritum;

import net.dark.spiritum.block.entity.CandleBlockEntity;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.*;

public class CandleHarvestTests {
    private CandleBlockEntity candle(TestContext test, int x, int flame) {
        test.setBlockState(x, 1, 3, ModContent.HEXED_CANDLE);
        var candle =
                (CandleBlockEntity)
                        test.getWorld().getBlockEntity(test.getAbsolutePos(new BlockPos(x, 1, 3)));
        candle.light(flame);
        return candle;
    }

    private void killMob(TestContext test, Vec3d position) {
        var mob = test.spawnMob(EntityType.COW, position);
        mob.setAiDisabled(true);
        mob.damage(test.getWorld(), test.getWorld().getDamageSources().generic(), 1000);
    }

    private int fragmentDrops(TestContext test) {
        return test.getWorld()
                .getEntitiesByClass(
                        ItemEntity.class,
                        test.getAbsolute(new Box(0, 0, 0, 16, 8, 8)),
                        item -> item.getStack().isOf(ModContent.SPIRIT_FRAGMENT))
                .size();
    }

    @GameTest(structure = "spiritum-test:ritual_space")
    public void litGemCandlesGuaranteeOneDropPerMob(TestContext test) {
        candle(test, 2, 3);
        var gemCandle = candle(test, 4, 5);
        candle(test, 5, 5);
        for (int i = 0; i < 20; i++) {
            if (i == 10) gemCandle.bind(java.util.UUID.randomUUID());
            killMob(test, new Vec3d(3.5, 1, 3.5));
            test.assertEquals(
                    i + 1,
                    fragmentDrops(test),
                    "Every mob creates exactly one fragment drop even with overlapping candles");
        }
        test.complete();
    }

    @GameTest(structure = "spiritum-test:ritual_space")
    public void unlitGemCandlesDoNotHarvest(TestContext test) {
        candle(test, 2, 5).snuff();
        killMob(test, new Vec3d(3.5, 1, 3.5));
        test.assertEquals(0, fragmentDrops(test), "Snuffed gem candles do not drop fragments");
        test.complete();
    }

    @GameTest(structure = "spiritum-test:ritual_space")
    public void gemCandleHarvestStaysWithinFiveBlocks(TestContext test) {
        candle(test, 2, 5);
        killMob(test, new Vec3d(11.5, 1, 3.5));
        test.assertEquals(0, fragmentDrops(test), "Gem candles do not harvest distant deaths");
        test.complete();
    }
}
