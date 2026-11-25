package net.dark.spiritum;

import net.dark.spiritum.block.entity.OfferingBlockEntity;
import net.dark.spiritum.magic.CandleHarvest;
import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.magic.Warding;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.dark.spiritum.registry.*;
import net.dark.spiritum.world.SpiritumWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class Spiritum implements ModInitializer {
    public static final String MOD_ID = "spiritum";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID,path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C()
                .register(
                        net.dark.spiritum.network.TunedBladeTargetPayload.ID,
                        net.dark.spiritum.network.TunedBladeTargetPayload.CODEC);

        SpiritumRegistryKeys.initialize();
        SpiritumRegistries.initialize();

        SpiritumVatLiquids.initialize();
        SpiritumContent.initialize();
        AlchemyRecipe.initialize();
        SpiritumEntities.initialize();
        SpiritumParticles.initialize();
        SpiritumWorld.initialize();
        CandleHarvest.initialize();
        Warding.initialize();
        RingMagic.initialize();
        // Sneaking with a held block item normally bypasses block use; support bulk offerings explicitly.
        UseBlockCallback.EVENT.register(
                (player, world, hand, hit) -> {
                    if (player.isSpectator()
                            || !player.isSneaking()
                            || player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;
                    if (world.getBlockEntity(hit.getBlockPos()) instanceof OfferingBlockEntity)
                        if (world.getBlockState(hit.getBlockPos()).onUseWithItem(player.getStackInHand(hand),world,player,hand,hit).isAccepted()) {
                            player.swingHand(hand);
                            return ActionResult.SUCCESS; // SUCCESS still sends C2S packets, unlike FAIL
                        }
                    return ActionResult.PASS;
                });
    }
}
