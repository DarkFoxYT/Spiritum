package net.dark.spiritum;

import net.dark.spiritum.entity.*;
import net.dark.spiritum.item.*;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

public class RingAndBottleGameTests {
    @GameTest
    public void gemSocketsSupportInventoryClicks(TestContext test) {
        var player = test.createMockPlayer(GameMode.SURVIVAL);
        var inventory = new net.minecraft.inventory.SimpleInventory(2);
        var holder = new ItemStack(ModContent.HEXBLADE);
        var gem = new ItemStack(ModContent.SPIRIT_GEM, 2);
        inventory.setStack(0, holder);
        inventory.setStack(1, gem);
        var slot = new net.minecraft.screen.slot.Slot(inventory, 0, 0, 0);
        var cursor = net.minecraft.inventory.StackReference.of(() -> inventory.getStack(1), stack -> inventory.setStack(1, stack));
        test.assertTrue(holder.getItem().onClicked(holder, gem, slot, net.minecraft.util.ClickType.LEFT, player, cursor), "Inventory click must insert a gem");
        test.assertTrue(gem.getCount() == 1 && SpiritBinding.socket(holder).getCount() == 1, "A socket must take exactly one gem");
        inventory.setStack(1, ItemStack.EMPTY);
        test.assertTrue(holder.getItem().onClicked(holder, ItemStack.EMPTY, slot, net.minecraft.util.ClickType.RIGHT, player, cursor), "Right-click must return the socketed gem to the cursor");
        test.assertTrue(SpiritBinding.socket(holder).isEmpty() && cursor.get().isOf(ModContent.SPIRIT_GEM), "Removing a gem must preserve it");
        var ring = new ItemStack(ModContent.SOULBIND_RING);
        inventory.setStack(0, ring);
        test.assertTrue(!ring.getItem().onClicked(ring, cursor.get(), slot, net.minecraft.util.ClickType.LEFT, player, cursor), "Soulbind sockets must reject an unbound gem");
        var poppet = new ItemStack(ModContent.VOODOO_POPPET);
        inventory.setStack(0, poppet);
        test.assertTrue(poppet.getMaxDamage() == 6, "Poppet must have six durability");
        test.assertTrue(!poppet.getItem().onClicked(poppet, cursor.get(), slot, ClickType.LEFT, player, cursor), "Poppets reject unbound gems");
        SpiritBinding.bind(cursor.get(), player);
        test.assertTrue(poppet.getItem().onClicked(poppet, cursor.get(), slot, ClickType.LEFT, player, cursor), "Poppets accept bound gems");
        inventory.setStack(1, ItemStack.EMPTY);
        test.assertTrue(!poppet.getItem().onClicked(poppet, ItemStack.EMPTY, slot, ClickType.RIGHT, player, cursor), "Poppet gems cannot be removed to the cursor");
        test.assertTrue(!poppet.getItem().onStackClicked(poppet, new net.minecraft.screen.slot.Slot(inventory, 1, 0, 0), ClickType.RIGHT, player), "Poppet gems cannot be removed into a slot");
        player.setStackInHand(Hand.MAIN_HAND, poppet);
        player.setSneaking(true);
        poppet.getItem().use(test.getWorld(), player, Hand.MAIN_HAND);
        test.assertTrue(!SpiritBinding.socket(poppet).isEmpty(), "Sneak-use must leave the gem locked");
        test.complete();
    }

    @GameTest
    public void breakingPoppetReturnsBoundGemAndConsumesNeedle(TestContext test) {
        var bound = player(test);
        var caster = test.createMockPlayer(GameMode.SURVIVAL);
        var gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, bound);
        var poppet = new ItemStack(ModContent.VOODOO_POPPET);
        poppet.setDamage(5);
        SpiritBinding.socket(poppet, gem);
        var needle = new ItemStack(ModContent.ARGENT_NEEDLE);
        caster.setStackInHand(Hand.MAIN_HAND, needle);
        caster.setStackInHand(Hand.OFF_HAND, poppet);
        needle.getItem().use(test.getWorld(), caster, Hand.MAIN_HAND);
        test.assertTrue(needle.isEmpty() && poppet.isEmpty(), "The final use must consume the needle and break the worn poppet");
        test.assertTrue(!caster.getInventory().contains(stack -> SpiritBinding.player(stack).filter(bound.getUuid()::equals).isPresent()), "A poppet must never return its locked gem");
        cleanup(test, bound);
    }

    @GameTest
    public void vatWaterColorChangesOnlyWithIngredients(TestContext test) {
        var pos = new BlockPos(3, 2, 3);
        test.setBlockState(pos, ModContent.ALCHEMY_VAT);
        var vat = test.getBlockEntity(pos, net.dark.spiritum.block.entity.VatBlockEntity.class);
        vat.fill();
        test.assertTrue(vat.getWaterColor() == 0x3F76E4, "Clean water must have a stable base color");
        var item = new net.minecraft.entity.ItemEntity(test.getWorld(), vat.getPos().getX() + .5, vat.getPos().getY() + .8, vat.getPos().getZ() + .5, new ItemStack(Items.REDSTONE));
        test.getWorld().spawnEntity(item);
        net.dark.spiritum.block.entity.VatBlockEntity.tick(test.getWorld(), vat.getPos(), vat.getCachedState(), vat);
        test.assertTrue(vat.getWaterColor() == 0xC73643 && item.isRemoved(), "Accepted redstone must tint only the water red");
        for (int i = 0; i < 40; i++) net.dark.spiritum.block.entity.VatBlockEntity.tick(test.getWorld(), vat.getPos(), vat.getCachedState(), vat);
        test.assertTrue(vat.getWaterColor() == 0xC73643, "Boiling must not pulse or cycle the ingredient color");
        test.complete();
    }

    private static ServerPlayerEntity player(TestContext test) {
        var world = test.getWorld();
        var data =
                net.minecraft.server.network.ConnectedClientData.createDefault(
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "spiritum-ring-test"),
                        false);
        var player =
                new ServerPlayerEntity(
                        world.getServer(), world, data.gameProfile(), data.syncedOptions());
        var connection =
                new net.minecraft.network.ClientConnection(
                        net.minecraft.network.NetworkSide.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        world.getServer().getPlayerManager().onPlayerConnect(connection, player, data);
        player.changeGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 100; i++) player.networkHandler.tickLoading();
        Vec3d pos = Vec3d.ofCenter(test.getAbsolutePos(new BlockPos(3, 3, 3)));
        player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        player.setNoGravity(true);
        return player;
    }

    private static void equip(ServerPlayerEntity player, ItemStack ring) {
        ((RingBearer) player).spiritum$setRing(ring);
        RingMagic.update(player);
    }

    private static void cleanup(TestContext test, ServerPlayerEntity... players) {
        for (var player : players) test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }

    @GameTest
    public void ringSlotAcceptsOnlyRingsAndShiftClicks(TestContext test) {
        var player = player(test);
        var screen = player.playerScreenHandler;
        test.assertTrue(
                screen.slots.size() == 47, "Inventory must expose an extra synchronized ring slot");
        test.assertTrue(
                !screen.slots.get(46).canInsert(new ItemStack(Items.STONE)),
                "Ring slot must reject normal items");
        player.getInventory().setStack(0, new ItemStack(ModContent.SUMMONERS_RING));
        screen.quickMove(player, 36);
        RingMagic.update(player);
        test.assertTrue(
                RingMagic.equipped(player).isOf(ModContent.SUMMONERS_RING)
                        && player.getInventory().getStack(0).isEmpty(),
                "Shift-click must equip the ring");
        test.assertTrue(
                Math.abs(player.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE) - 2)
                        < .001,
                "Summoner reach must fall from three to two blocks");
        screen.quickMove(player, 46);
        RingMagic.update(player);
        test.assertTrue(
                RingMagic.equipped(player).isEmpty()
                        && Math.abs(
                                        player.getAttributeValue(
                                                        EntityAttributes.ENTITY_INTERACTION_RANGE)
                                                - 3)
                                < .001,
                "Unequipping must remove the reach penalty");
        cleanup(test, player);
    }

    @GameTest
    public void ringsModifyRealDemonDamage(TestContext test) {
        var owner = player(test);
        equip(owner, new ItemStack(ModContent.SUMMONERS_RING));
        var imp = test.spawnEntity(ModEntities.IMP, 3, 3, 3);
        imp.setOwnerUuid(owner.getUuid());
        imp.setAiDisabled(true);
        var cow = test.spawnEntity(EntityType.COW, 3, 3, 3);
        cow.setAiDisabled(true);
        cow.damage(test.getWorld(), DemonDamage.source(test.getWorld(), DemonDamage.IMP, imp), 4);
        test.assertTrue(
                Math.abs(cow.getHealth() - 5.2f) < .001,
                "Summoner attacks must receive twenty percent extra damage");
        imp.damage(test.getWorld(), test.getWorld().getDamageSources().generic(), 3);
        test.assertTrue(
                Math.abs(imp.getHealth() - 8) < .001,
                "Fifty percent extra defense must reduce three damage to two");
        equip(owner, ItemStack.EMPTY);
        var victim = player(test);
        equip(victim, new ItemStack(ModContent.WARDING_RING));
        victim.damage(
                test.getWorld(), DemonDamage.source(test.getWorld(), DemonDamage.IMP, imp), 4);
        test.assertTrue(victim.getHealth() == 18, "Warding must halve demon damage");
        cleanup(test, owner, victim);
    }

    @GameTest
    public void soulbindSharesDamageWithoutLoopsAndBoostsNearbyAttack(TestContext test) {
        var bound = player(test);
        var wearer = player(test);
        ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, bound);
        ItemStack ring = new ItemStack(ModContent.SOULBIND_RING);
        SpiritBinding.socket(ring, gem);
        equip(wearer, ring);
        test.assertTrue(
                Math.abs(wearer.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) - 1.15) < .001,
                "Soulbind must grant fifteen percent attack near its bound player");
        ItemStack reverse = new ItemStack(ModContent.SOULBIND_RING);
        ItemStack reverseGem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(reverseGem, wearer);
        SpiritBinding.socket(reverse, reverseGem);
        equip(bound, reverse);
        bound.damage(test.getWorld(), test.getWorld().getDamageSources().generic(), 8);
        test.assertTrue(
                bound.getHealth() == 16 && wearer.getHealth() == 16,
                "Reciprocal bindings must split eight damage once and avoid recursion");
        wearer.refreshPositionAndAngles(wearer.getX() + 20, wearer.getY(), wearer.getZ(), 0, 0);
        RingMagic.update(wearer);
        test.assertTrue(
                wearer.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) == 1,
                "Attack bonus must end beyond sixteen blocks");
        cleanup(test, bound, wearer);
    }

    @GameTest
    public void wardingPreventsVoodooWithoutSpendingNeedle(TestContext test) {
        var bound = player(test);
        var caster = player(test);
        equip(bound, new ItemStack(ModContent.WARDING_RING));
        var gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, bound);
        var poppet = new ItemStack(ModContent.VOODOO_POPPET);
        SpiritBinding.socket(poppet, gem);
        caster.setStackInHand(Hand.OFF_HAND, poppet);
        var needle = new ItemStack(ModContent.ARGENT_NEEDLE);
        caster.setStackInHand(Hand.MAIN_HAND, needle);
        var result = needle.getItem().use(test.getWorld(), caster, Hand.MAIN_HAND);
        test.assertTrue(
                result == ActionResult.FAIL && bound.getHealth() == 20 && needle.getDamage() == 0,
                "Warding must reject voodoo before applying effects or spending durability");
        cleanup(test, bound, caster);
    }

    @GameTest
    public void ringBindingSurvivesPlayerSaveAndRespawnCopy(TestContext test) {
        var player = player(test);
        var gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        var ring = new ItemStack(ModContent.SOULBIND_RING);
        SpiritBinding.socket(ring, gem);
        equip(player, ring);
        try (var errors =
                new net.minecraft.util.ErrorReporter.Logging(
                        org.slf4j.LoggerFactory.getLogger("Spiritum ring test"))) {
            var output =
                    net.minecraft.storage.NbtWriteView.create(
                            errors, test.getWorld().getRegistryManager());
            player.writeData(output);
            equip(player, ItemStack.EMPTY);
            player.readData(
                    net.minecraft.storage.NbtReadView.create(
                            errors, test.getWorld().getRegistryManager(), output.getNbt()));
            test.assertTrue(
                    SpiritBinding.player(SpiritBinding.socket(RingMagic.equipped(player)))
                            .filter(player.getUuid()::equals)
                            .isPresent(),
                    "Saving and loading must preserve the equipped ring and bound gem");
        }
        var restored = player(test);
        restored.copyFrom(player, true);
        test.assertTrue(
                ItemStack.areItemsAndComponentsEqual(
                        RingMagic.equipped(player), RingMagic.equipped(restored)),
                "Player replacement must retain the equipped ring");
        cleanup(test, player, restored);
    }

    @GameTest
    public void equippedRingDropsOnDeath(TestContext test) {
        var player = player(test);
        equip(player, new ItemStack(ModContent.SUMMONERS_RING));
        player.damage(test.getWorld(), test.getWorld().getDamageSources().genericKill(), 100);
        test.assertTrue(
                RingMagic.equipped(player).isEmpty(),
                "Death without keepInventory must clear the equipped ring");
        test.assertTrue(
                !test.getWorld()
                        .getEntitiesByClass(
                                net.minecraft.entity.ItemEntity.class,
                                player.getBoundingBox().expand(3),
                                e -> e.getStack().isOf(ModContent.SUMMONERS_RING))
                        .isEmpty(),
                "Equipped ring must drop with the rest of the inventory");
        cleanup(test, player);
    }

    @GameTest
    public void blockedBottleReleaseRetainsItsDemon(TestContext test) {
        var owner = player(test);
        var demon = test.spawnEntity(ModEntities.IMP, 3, 3, 3);
        demon.setOwnerUuid(owner.getUuid());
        var bottle = new ItemStack(ModContent.BOTTLE_OF_HADES);
        test.assertTrue(
                BottleOfHadesItem.capture(test.getWorld(), owner, bottle) == 1,
                "Capture should store the owned imp");
        for (int x = 0; x <= 6; x++)
            for (int z = 0; z <= 6; z++)
                for (int y = 3; y <= 6; y++)
                    test.setBlockState(new BlockPos(x, y, z), net.minecraft.block.Blocks.STONE);
        test.assertTrue(
                BottleOfHadesItem.release(
                                        test.getWorld(),
                                        test.getAbsolutePos(new BlockPos(3, 3, 3)),
                                        bottle)
                                == 0
                        && BottleOfHadesItem.count(bottle) == 1,
                "Blocked ground must retain its demon instead of deleting it");
        cleanup(test, owner);
    }

    @GameTest
    public void bottleCapacityOwnershipAndReleasePreserveHealth(TestContext test) {
        var owner = player(test);
        var foreign = test.spawnEntity(ModEntities.LEECH, 3, 3, 3);
        foreign.setOwnerUuid(java.util.UUID.randomUUID());
        for (int i = 0; i < 11; i++) {
            var demon = test.spawnEntity(ModEntities.LEMURE, 3, 3, 3);
            demon.setOwnerUuid(owner.getUuid());
            demon.setHealth(7);
        }
        var bottle = new ItemStack(ModContent.BOTTLE_OF_HADES);
        test.assertTrue(
                BottleOfHadesItem.capture(test.getWorld(), owner, bottle) == 10
                        && BottleOfHadesItem.count(bottle) == 10
                        && !foreign.isRemoved(),
                "Bottle must hold ten owned demons and leave other owners' demons alone");
        var copy = bottle.copy();
        test.assertTrue(
                BottleOfHadesItem.count(copy) == 10,
                "Bottle contents must survive component copying");
        int released =
                BottleOfHadesItem.release(
                        test.getWorld(), test.getAbsolutePos(new BlockPos(3, 3, 3)), copy);
        test.assertTrue(
                released == 10 && BottleOfHadesItem.count(copy) == 0,
                "Ground release must restore all ten demons");
        var restored =
                test.getWorld()
                        .getEntitiesByClass(
                                LemureDemonEntity.class,
                                new Box(test.getAbsolutePos(new BlockPos(3, 3, 3))).expand(5),
                                e ->
                                        owner.getUuid().equals(e.getOwnerUuid())
                                                && e.getHealth() == 7);
        test.assertTrue(
                restored.size() == 11, "Released demons must retain health and their summoner");
        cleanup(test, owner);
    }
}
