package net.dark.spiritum;

import net.dark.spiritum.entity.PoppetEntity;
import net.dark.spiritum.item.*;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

public class ItemUseTests {
    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 50)
    public void poppetChargesThenThrowsOnRelease(TestContext test) {
        for (int x = 0; x < 8; x++)
            for (int z = 0; z < 8; z++) test.setBlockState(x, 0, z, Blocks.STONE);
        var player = test.createMockCreativeServerPlayerInWorld();
        player.changeGameMode(GameMode.SURVIVAL);
        player.setPosition(test.getAbsolute(new Vec3d(3, 1, 2)));
        player.setYaw(0);
        player.setPitch(0);
        ItemStack stack = new ItemStack(ModContent.VOODOO_POPPET);
        ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        SpiritBinding.socket(stack, gem);
        player.setStackInHand(Hand.MAIN_HAND, stack);
        ModContent.VOODOO_POPPET.use(test.getWorld(), player, Hand.MAIN_HAND);
        test.assertTrue(player.isUsingItem(), "Use starts charging the poppet");
        test.assertEquals(1, stack.getCount(), "Starting a charge keeps the held poppet");
        test.runAtTick(
                10,
                () -> {
                    var dolls =
                            test.getWorld()
                                    .getEntitiesByClass(
                                            PoppetEntity.class,
                                            new Box(
                                                    test.getAbsolute(Vec3d.ZERO),
                                                    test.getAbsolute(new Vec3d(8, 8, 8))),
                                            p -> true);
                    test.assertTrue(dolls.isEmpty(), "Holding use does not throw early");
                    player.stopUsingItem();
                    dolls =
                            test.getWorld()
                                    .getEntitiesByClass(
                                            PoppetEntity.class,
                                            new Box(
                                                    test.getAbsolute(Vec3d.ZERO),
                                                    test.getAbsolute(new Vec3d(8, 8, 8))),
                                            p -> true);
                    test.assertEquals(1, dolls.size(), "Releasing use throws exactly one doll");
                    test.assertTrue(stack.isEmpty(), "A successful throw consumes the held doll");
                    test.assertEquals(
                            player.getUuid(),
                            SpiritBinding.player(SpiritBinding.socket(dolls.getFirst().getStack()))
                                    .orElse(null),
                            "Throw preserves the socketed binding");
                    test.assertTrue(
                            PoppetItem.throwSpeed(10) < PoppetItem.throwSpeed(20),
                            "Longer charges throw farther");
                    test.assertTrue(
                            PoppetItem.throwSpeed(72000) <= .9f,
                            "Charge speed remains capped below the old 1.4 speed");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest
    public void tunedBladeUsesAndReturnsExactBoundGem(TestContext test) {
        var player = test.createMockCreativeServerPlayerInWorld();
        player.changeGameMode(GameMode.SURVIVAL);
        ItemStack blade = new ItemStack(ModContent.TUNED_BLADE);
        player.setStackInHand(Hand.MAIN_HAND, blade);
        player.setStackInHand(Hand.OFF_HAND, new ItemStack(ModContent.SPIRIT_GEM));
        test.assertEquals(
                ActionResult.PASS,
                blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND),
                "Blade rejects unbound gems");
        ItemStack gem = player.getOffHandStack();
        SpiritBinding.bind(gem, player);
        gem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Tracking gem"));
        blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND);
        test.assertEquals(
                player.isCreative() ? 1 : 0,
                gem.getCount(),
                "Socketing follows the player's creative consumption rules");
        test.assertFalse(SpiritBinding.socket(blade).isEmpty(), "The bound gem is socketed");
        player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        test.assertEquals(
                ActionResult.CONSUME,
                blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND),
                "Online binding starts guidance");
        test.assertTrue(player.isUsingItem(), "Holding use activates guidance");
        player.stopUsingItem();
        player.setSneaking(true);
        blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND);
        test.assertTrue(SpiritBinding.socket(blade).isEmpty(), "Sneak-use removes the gem");
        ItemStack returned = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack candidate = player.getInventory().getStack(i);
            if (candidate.isOf(ModContent.SPIRIT_GEM)) returned = candidate;
        }
        test.assertEquals(
                Text.literal("Tracking gem"),
                returned.get(DataComponentTypes.CUSTOM_NAME),
                "Removing preserves custom gem data");
        test.assertEquals(
                player.getUuid(),
                SpiritBinding.player(returned).orElse(null),
                "Removing preserves binding");
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }

    @GameTest
    public void tunedBladeStopsWhenBoundPlayerIsOffline(TestContext test) {
        var player = test.createMockCreativeServerPlayerInWorld();
        ItemStack blade = new ItemStack(ModContent.TUNED_BLADE);
        ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        SpiritBinding.socket(blade, gem);
        player.setStackInHand(Hand.MAIN_HAND, blade);
        player.setCurrentHand(Hand.MAIN_HAND);
        NbtComponent.set(
                DataComponentTypes.CUSTOM_DATA,
                gem,
                nbt -> nbt.putString("SpiritumPlayer", java.util.UUID.randomUUID().toString()));
        SpiritBinding.socket(blade, gem);
        blade.getItem().usageTick(test.getWorld(), player, blade, 71999);
        test.assertFalse(player.isUsingItem(), "Losing the online player stops guidance");
        test.assertEquals(
                ActionResult.PASS,
                blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND),
                "Offline binding cannot begin guidance");
        test.assertFalse(
                SpiritBinding.socket(blade).isEmpty(), "Offline target does not consume the gem");
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }
}
