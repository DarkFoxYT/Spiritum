package net.dark.spiritum.client;

import net.dark.spiritum.item.*;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

final class BackslotChecks {
    static void verifyRingPersistence(net.minecraft.server.network.ServerPlayerEntity player) {
        var server = player.getEntityWorld().getServer();
        var ring = RingMagic.equipped(player).copy();
        server.getPlayerManager().saveAllPlayerData();
        var data =
                server.getPlayerManager()
                        .loadPlayerData(player.getPlayerConfigEntry())
                        .orElseThrow();
        var reloaded =
                new net.minecraft.server.network.ServerPlayerEntity(
                        server,
                        player.getEntityWorld(),
                        player.getGameProfile(),
                        net.minecraft.network.packet.c2s.common.SyncedClientOptions
                                .createDefault());
        reloaded.readData(
                net.minecraft.storage.NbtReadView.create(
                        net.minecraft.util.ErrorReporter.EMPTY, server.getRegistryManager(), data));
        if (!ItemStack.areEqual(ring, RingMagic.equipped(reloaded)))
            throw new AssertionError("Ring survives player file reload with BackSlot installed");
    }

    static void equip(PlayerEntity player, ItemStack stack) {
        try {
            var type = Class.forName("net.medecoole.backslot.registry.BackslotItemComponent");
            Object component = type.getMethod("get", PlayerEntity.class).invoke(null, player);
            type.getMethod("setIsHolding", Boolean.class).invoke(component, false);
            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            type.getMethod("setStack", ItemStack.class).invoke(component, stack);
            type.getMethod("setIsHolding", Boolean.class).invoke(component, true);
            if (!ItemStack.areEqual(stack, player.getMainHandStack()))
                throw new AssertionError("Selecting BackSlot exposes the weapon as the held item");
            if (stack.getItem() instanceof TunedBladeItem) {
                stack.getItem().use(player.getEntityWorld(), player, Hand.MAIN_HAND);
                if (!player.isUsingItem())
                    throw new AssertionError("BackSlot blade can start tracking");
                player.stopUsingItem();
            }
            type.getMethod("setIsHolding", Boolean.class).invoke(component, false);
            if (!RingMagic.equipped(player).isOf(ModContent.WARDING_RING))
                throw new AssertionError("BackSlot and equipped ring stay independent");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot test BackSlot integration", e);
        }
    }
}
