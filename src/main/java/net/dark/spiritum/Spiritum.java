package net.dark.spiritum;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.magic.CandleHarvest;
import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.magic.Warding;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.registry.ModEntities;
import net.dark.spiritum.registry.ModParticles;
import net.dark.spiritum.world.SpiritumWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;

public class Spiritum implements ModInitializer {
    public static final String MOD_ID = "spiritum";

    @Override
    public void onInitialize() {
        ModContent.initialize();
        ModEntities.initialize();
        ModParticles.initialize();
        SpiritumWorld.initialize();
        CandleHarvest.initialize();
        Warding.initialize();
        RingMagic.initialize();
        // Sneaking with a held item normally bypasses block use; support bulk offerings explicitly.
        UseBlockCallback.EVENT.register(
                (player, world, hand, hit) -> {
                    if (player.isSpectator()
                            || !player.isSneaking()
                            || player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;
                    if (world.getBlockEntity(hit.getBlockPos())
                            instanceof PedestalBlockEntity pedestal) {
                        if (!world.isClient()) pedestal.insert(player.getStackInHand(hand), player);
                        return ActionResult.SUCCESS;
                    }
                    return ActionResult.PASS;
                });
    }
}
