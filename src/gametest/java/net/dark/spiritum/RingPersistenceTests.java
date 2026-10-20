package net.dark.spiritum;

import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.rule.GameRules;

public class RingPersistenceTests {
    private ServerPlayerEntity freshPlayer(TestContext test, ServerPlayerEntity original) {
        return new ServerPlayerEntity(
                test.getWorld().getServer(),
                test.getWorld(),
                original.getGameProfile(),
                SyncedClientOptions.createDefault());
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 20)
    public void allRingTypesSurvivePlayerFileReload(TestContext test) {
        var player = test.createMockCreativeServerPlayerInWorld();
        var manager = test.getWorld().getServer().getPlayerManager();
        for (var item :
                new Item[] {
                    ModContent.SUMMONERS_RING, ModContent.SOULBIND_RING, ModContent.WARDING_RING
                }) {
            var ring = new ItemStack(item);
            ring.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Saved ring"));
            var gem = new ItemStack(ModContent.SPIRIT_GEM);
            SpiritBinding.bind(gem, player);
            SpiritBinding.socket(ring, gem);
            new RingInventory(player).setStack(0, ring);
            manager.saveAllPlayerData();
            var saved = manager.loadPlayerData(player.getPlayerConfigEntry()).orElseThrow();
            test.assertTrue(
                    saved.contains("SpiritumRing"), "Ring is written to the real player data file");
            var loaded = freshPlayer(test, player);
            loaded.readData(
                    NbtReadView.create(
                            ErrorReporter.EMPTY, test.getWorld().getRegistryManager(), saved));
            test.assertTrue(
                    ItemStack.areEqual(ring, RingMagic.equipped(loaded)),
                    "Ring and all components survive file reload");
            test.assertTrue(
                    ItemStack.areEqual(ring, loaded.playerScreenHandler.getSlot(46).getStack()),
                    "Loaded ring appears in the inventory slot");
        }
        new RingInventory(player).clear();
        manager.saveAllPlayerData();
        var saved = manager.loadPlayerData(player.getPlayerConfigEntry()).orElseThrow();
        var loaded = freshPlayer(test, player);
        loaded.readData(
                NbtReadView.create(
                        ErrorReporter.EMPTY, test.getWorld().getRegistryManager(), saved));
        test.assertTrue(
                RingMagic.equipped(loaded).isEmpty(),
                "Unequipping and saving does not restore an old ring");
        manager.remove(player);
        test.complete();
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 20)
    public void respawnAndDimensionCopiesKeepTheRightRingState(TestContext test) {
        var player = test.createMockCreativeServerPlayerInWorld();
        var ring = new ItemStack(ModContent.WARDING_RING);
        ((RingBearer) player).spiritum$setRing(ring);
        var aliveCopy = freshPlayer(test, player);
        aliveCopy.copyFrom(player, true);
        test.assertTrue(
                ItemStack.areEqual(ring, RingMagic.equipped(aliveCopy)),
                "Living player transfer preserves equipped ring");
        test.assertTrue(
                RingMagic.equipped(aliveCopy) != ring,
                "Player copies do not share mutable ring stacks");
        var rules = test.getWorld().getGameRules();
        boolean previous = rules.getValue(GameRules.KEEP_INVENTORY);
        try {
            rules.setValue(GameRules.KEEP_INVENTORY, true, test.getWorld().getServer());
            var kept = freshPlayer(test, player);
            kept.copyFrom(player, false);
            test.assertTrue(
                    ItemStack.areEqual(ring, RingMagic.equipped(kept)),
                    "Keep inventory preserves ring on respawn");
            rules.setValue(GameRules.KEEP_INVENTORY, false, test.getWorld().getServer());
            player.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
            var dropped = freshPlayer(test, player);
            dropped.copyFrom(player, false);
            test.assertTrue(
                    RingMagic.equipped(dropped).isEmpty(),
                    "Normal death does not duplicate ring into respawned player");
        } finally {
            rules.setValue(GameRules.KEEP_INVENTORY, previous, test.getWorld().getServer());
            test.getWorld().getServer().getPlayerManager().remove(player);
        }
        test.complete();
    }
}
