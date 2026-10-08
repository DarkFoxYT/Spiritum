package net.dark.spiritum;

import net.dark.spiritum.block.entity.*;
import net.dark.spiritum.entity.*;
import net.dark.spiritum.item.*;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

public class GameplayTests {
    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 130)
    public void attachedLeechDoesNotPushItsHost(TestContext test) {
        floor(test);
        var owner = player(test);
        var host = test.spawnMob(EntityType.COW, new Vec3d(3, 1, 3));
        host.setAiDisabled(true);
        host.setNoGravity(true);
        var leech = test.spawnEntity(ModEntities.LEECH, new Vec3d(3.1, 1.5, 3));
        leech.setAiDisabled(true);
        leech.setOwnerUuid(owner.getUuid());
        leech.setTarget(host);
        Vec3d original = host.getEntityPos();
        test.runAtTick(
                10,
                () -> {
                    test.assertTrue(leech.isLatched(), "Leech attaches to its selected host");
                    test.assertTrue(
                            host.getEntityPos().squaredDistanceTo(original) < .000001,
                            "Attaching does not displace the host");
                });
        test.runAtTick(
                110,
                () -> {
                    test.assertTrue(leech.isLatched(), "Leech remains attached while draining");
                    test.assertTrue(
                            host.getEntityPos().squaredDistanceTo(original) < .000001,
                            "An attached leech does not push its host over time");
                    test.assertTrue(
                            host.getVelocity().horizontalLengthSquared() < .000001,
                            "Leech collision does not impart velocity to its host");
                    test.assertEquals(
                            8f, host.getHealth(), "Attached leech still drains two health");
                    test.getWorld().getServer().getPlayerManager().remove(owner);
                    test.complete();
                });
    }

    private PedestalBlockEntity ritual(
            TestContext test, ServerPlayerEntity player, String id, int ordinary, int flame) {
        floor(test);
        test.setBlockState(3, 1, 3, ModContent.RITUAL_PEDESTAL);
        var pedestal =
                (PedestalBlockEntity)
                        test.getWorld().getBlockEntity(test.getAbsolutePos(new BlockPos(3, 1, 3)));
        int[][] positions = {{2, 2}, {2, 3}, {2, 4}, {3, 2}, {3, 4}, {4, 2}, {4, 3}, {4, 4}};
        for (int i = 0; i <= ordinary; i++) {
            int[] p = positions[i];
            test.setBlockState(p[0], 1, p[1], ModContent.HEXED_CANDLE);
            var candle =
                    (CandleBlockEntity)
                            test.getWorld()
                                    .getBlockEntity(
                                            test.getAbsolutePos(new BlockPos(p[0], 1, p[1])));
            candle.light(i == ordinary ? 5 : flame);
            if (i == ordinary) {
                ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
                SpiritBinding.bind(gem, player);
                gem.set(
                        net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                        net.minecraft.text.Text.literal("Returned test gem"));
                candle.bind(player.getUuid());
                candle.setFuelGem(gem);
            }
        }
        player.setSneaking(true);
        MagicRecipes.ritual(id)
                .ingredients()
                .forEach((item, count) -> pedestal.insert(new ItemStack(item, count), player));
        player.setSneaking(false);
        return pedestal;
    }

    private void binding(TestContext test, String id, Class<? extends OwnedDemonEntity> kind) {
        var player = player(test);
        ritual(test, player, id, 3, 4);
        test.runAtTick(
                120,
                () -> {
                    var demons =
                            test.getWorld()
                                    .getEntitiesByClass(
                                            kind,
                                            test.getAbsolute(new Box(0, 0, 0, 8, 8, 8)),
                                            e -> player.getUuid().equals(e.getOwnerUuid()));
                    test.assertEquals(
                            1, demons.size(), "Binding summons exactly the correct owned demon");
                    var gems =
                            test.getWorld()
                                    .getEntitiesByClass(
                                            net.minecraft.entity.ItemEntity.class,
                                            test.getAbsolute(new Box(0, 0, 0, 8, 8, 8)),
                                            e -> e.getStack().isOf(ModContent.SPIRIT_GEM));
                    java.util.List<ItemStack> returnedGems = new java.util.ArrayList<>();
                    gems.forEach(gem -> returnedGems.add(gem.getStack()));
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        var stack = player.getInventory().getStack(i);
                        if (stack.isOf(ModContent.SPIRIT_GEM)) returnedGems.add(stack);
                    }
                    for (var demon : demons)
                        if (demon instanceof ImpDemonEntity imp
                                && imp.getCarriedStack().isOf(ModContent.SPIRIT_GEM))
                            returnedGems.add(imp.getCarriedStack());
                    test.assertEquals(
                            1,
                            returnedGems.size(),
                            "Binding returns exactly one gem, including one fetched by the imp");
                    var returned = returnedGems.getFirst();
                    test.assertEquals(
                            player.getUuid(),
                            SpiritBinding.player(returned).orElse(null),
                            "Returned gem retains player binding");
                    test.assertEquals(
                            "Returned test gem",
                            returned.getName().getString(),
                            "Returned gem retains its components");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 150)
    public void leechBindingReturnsGem(TestContext test) {
        binding(test, "leech_binding", LeechDemonEntity.class);
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 150)
    public void impBindingReturnsGem(TestContext test) {
        binding(test, "imp_binding", ImpDemonEntity.class);
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 150)
    public void lemureBindingReturnsGem(TestContext test) {
        binding(test, "lemure_binding", LemureDemonEntity.class);
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 140)
    public void callingCompletesAfterPlayerMoves(TestContext test) {
        var player = player(test);
        var pedestal = ritual(test, player, "calling", 2, 4);
        test.runAtTick(
                20,
                () -> {
                    test.assertEquals(
                            "calling",
                            pedestal.getActiveRitual(),
                            "Calling starts for an online binding");
                    player.setPosition(test.getAbsolute(new Vec3d(7, 2, 7)));
                });
        test.runAtTick(
                100,
                () -> {
                    Vec3d destination = Vec3d.ofCenter(pedestal.getPos()).add(0, .55, 0);
                    test.assertTrue(
                            player.getEntityPos().squaredDistanceTo(destination) < .1,
                            "Moving must not cancel Calling");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 110)
    public void dominionIsDisabled(TestContext test) {
        floor(test);
        var player = player(test);
        test.setBlockState(3, 1, 3, ModContent.RITUAL_PEDESTAL);
        BlockPos center = test.getAbsolutePos(new BlockPos(3, 1, 3));
        var pedestal = (PedestalBlockEntity) test.getWorld().getBlockEntity(center);
        int[][] positions = {{2, 2}, {2, 3}, {2, 4}, {3, 2}, {3, 4}};
        for (int i = 0; i < positions.length; i++) {
            int[] p = positions[i];
            test.setBlockState(p[0], 1, p[1], ModContent.HEXED_CANDLE);
            var candle =
                    (CandleBlockEntity)
                            test.getWorld()
                                    .getBlockEntity(
                                            test.getAbsolutePos(new BlockPos(p[0], 1, p[1])));
            candle.light(5);
            if (i == 4) candle.bind(player.getUuid());
        }
        player.setSneaking(true);
        pedestal.insert(new ItemStack(Items.NETHER_STAR), player);
        pedestal.insert(new ItemStack(ModContent.CALX_OF_HADES, 6), player);
        pedestal.insert(new ItemStack(ModContent.ARGENT_INGOT, 3), player);
        player.setSneaking(false);
        test.runAtTick(
                90,
                () -> {
                    test.assertTrue(
                            MagicRecipes.ritual("dominion") == null,
                            "Dominion has no available recipe");
                    test.assertTrue(
                            MagicRecipes.ritual("withering") != null,
                            "Withering remains available");
                    test.assertEquals(
                            "",
                            pedestal.getActiveRitual(),
                            "Former Dominion offerings cannot activate a rite");
                    test.assertFalse(pedestal.isSustained(), "Dominion cannot sustain");
                    Dominion.add(test.getWorld(), center);
                    test.assertFalse(
                            Dominion.restricted(test.getWorld(), center, player),
                            "Old Dominion registrations cannot restrict players");
                    Dominion.remove(test.getWorld(), center);
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 125)
    public void sentinelThrustLaunchesAndDealsTenDamage(TestContext test) {
        var player = player(test);
        ritual(test, player, "vigilance", 4, 4);
        var sentinel = test.spawnEntity(ModEntities.SENTINEL, new Vec3d(6, 1, 2));
        var attacker = test.spawnMob(EntityType.COW, new Vec3d(6, 1, 7));
        attacker.setAiDisabled(true);
        test.runAtTick(
                85,
                () ->
                        player.damage(
                                test.getWorld(),
                                test.getWorld().getDamageSources().mobAttack(attacker),
                                1));
        test.runAtTick(
                87,
                () -> {
                    test.assertEquals(
                            2, sentinel.attackPose(), "Medium-range attacker triggers thrust");
                    test.assertTrue(
                            sentinel.getVelocity().horizontalLengthSquared() > .1,
                            "Thrust launches armor forward");
                });
        test.runAtTick(
                100,
                () -> {
                    test.assertEquals(
                            0f,
                            attacker.getHealth(),
                            "Cow's ten health is exhausted by one thrust");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    private ServerPlayerEntity player(TestContext test) {
        test.getWorld()
                .getGameRules()
                .setValue(
                        net.minecraft.world.rule.GameRules.PVP, true, test.getWorld().getServer());
        var player = test.createMockCreativeServerPlayerInWorld();
        player.changeGameMode(GameMode.SURVIVAL);
        player.setPosition(test.getAbsolute(new Vec3d(1, 2, 1)));
        return player;
    }

    private ItemStack poppet(ServerPlayerEntity player) {
        ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        ItemStack stack = new ItemStack(ModContent.VOODOO_POPPET);
        SpiritBinding.socket(stack, gem);
        return stack;
    }

    private void floor(TestContext test) {
        for (int x = 0; x < 8; x++)
            for (int z = 0; z < 8; z++) test.setBlockState(x, 0, z, Blocks.STONE);
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 110)
    public void thrownPoppetHitsWallWithCappedDamage(TestContext test) {
        floor(test);
        var player = player(test);
        for (int z = 0; z < 8; z++)
            for (int y = 1; y < 7; y++) test.setBlockState(5, y, z, Blocks.STONE);
        final PoppetEntity[] dollRef = new PoppetEntity[1];
        test.runAtTick(
                70,
                () -> {
                    var doll = test.spawnEntity(ModEntities.POPPET, new Vec3d(2, 3, 3));
                    doll.launch(poppet(player), player, new Vec3d(4, 0, 0));
                    dollRef[0] = doll;
                });
        test.runAtTick(
                74,
                () -> {
                    var doll = dollRef[0];
                    test.assertEquals(
                            10f,
                            player.getHealth(),
                            "Fast wall impact must deal at most five hearts");
                    test.assertTrue(
                            test.getRelative(doll.getEntityPos()).x < 5,
                            "Ragdoll must collide with the wall");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 110)
    public void groundedPoppetSupportsNeedleAndPickup(TestContext test) {
        floor(test);
        var player = player(test);
        var doll = test.spawnEntity(ModEntities.POPPET, new Vec3d(3, 2, 3));
        doll.launch(poppet(player), player, Vec3d.ZERO);
        test.runAtTick(
                85,
                () -> {
                    Vec3d center = doll.getBoundingBox().getCenter();
                    Vec3d from = center.add(0, 0, 2);
                    var hit =
                            net.minecraft.entity.projectile.ProjectileUtil.raycast(
                                    player,
                                    from,
                                    center.add(0, 0, -2),
                                    doll.getBoundingBox().expand(2),
                                    net.minecraft.entity.Entity::canHit,
                                    16);
                    test.assertTrue(
                            hit != null && hit.getEntity() == doll,
                            "Client interaction raycast can target the grounded poppet");
                    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModContent.ARGENT_NEEDLE));
                    test.assertEquals(
                            ActionResult.SUCCESS,
                            doll.interact(player, Hand.MAIN_HAND),
                            "Needle must work on world poppet");
                    test.assertEquals(
                            19f, player.getHealth(), "Needle deals half a heart to bound player");
                    test.assertEquals(
                            1, doll.getStack().getDamage(), "Needle wears the same poppet");
                    player.setSneaking(true);
                    doll.interact(player, Hand.MAIN_HAND);
                    test.assertTrue(doll.isRemoved(), "Sneaking pickup removes world doll");
                    ItemStack recovered = ItemStack.EMPTY;
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        var stack = player.getInventory().getStack(i);
                        if (stack.isOf(ModContent.VOODOO_POPPET)) recovered = stack;
                    }
                    test.assertEquals(1, recovered.getDamage(), "Pickup preserves wear");
                    test.assertEquals(
                            player.getUuid(),
                            SpiritBinding.player(SpiritBinding.socket(recovered)).orElse(null),
                            "Pickup preserves binding");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 30)
    public void dormantSentinelDoesNotRetaliate(TestContext test) {
        floor(test);
        var sentinel = test.spawnEntity(ModEntities.SENTINEL, new Vec3d(3, 1, 3));
        var attacker = test.spawnMob(EntityType.ZOMBIE, new Vec3d(5, 1, 3));
        sentinel.defend(attacker, null);
        test.assertFalse(sentinel.isAwake(), "Sentinel requires vigilance");
        test.assertEquals(40f, sentinel.getMaxHealth(), "Sentinel health");
        test.assertEquals(
                20.0, sentinel.getAttributeValue(EntityAttributes.ARMOR), "Sentinel armor");
        test.assertEquals(
                12.0,
                sentinel.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS),
                "Sentinel toughness");
        test.runAtTick(
                10,
                () -> {
                    test.assertTrue(
                            sentinel.getTarget() == null, "Unarmed sentinel remains dormant");
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 150)
    public void vigilanceAcceptsFourBindingsAndRevokesCombat(TestContext test) {
        floor(test);
        var player = player(test);
        test.setBlockState(3, 1, 3, ModContent.RITUAL_PEDESTAL);
        BlockPos center = test.getAbsolutePos(new BlockPos(3, 1, 3));
        var pedestal = (PedestalBlockEntity) test.getWorld().getBlockEntity(center);
        int[][] positions = {{2, 2}, {2, 3}, {2, 4}, {3, 2}, {3, 4}, {4, 2}, {4, 3}, {4, 4}};
        for (int i = 0; i < 8; i++) {
            int[] p = positions[i];
            test.setBlockState(p[0], 1, p[1], ModContent.HEXED_CANDLE);
            var candle =
                    (CandleBlockEntity)
                            test.getWorld()
                                    .getBlockEntity(
                                            test.getAbsolutePos(new BlockPos(p[0], 1, p[1])));
            candle.light(4);
            if (i >= 4) candle.bind(i == 4 ? player.getUuid() : java.util.UUID.randomUUID());
        }
        player.setSneaking(true);
        pedestal.insert(new ItemStack(ModContent.HEX_ASH, 6), player);
        pedestal.insert(new ItemStack(Items.ECHO_SHARD, 2), player);
        pedestal.insert(new ItemStack(ModContent.CALX_OF_HADES, 2), player);
        player.setSneaking(false);
        var sentinel = test.spawnEntity(ModEntities.SENTINEL, new Vec3d(6, 1, 3));
        var attacker = test.spawnMob(EntityType.ZOMBIE, new Vec3d(6, 1, 5));
        attacker.setAiDisabled(true);
        test.runAtTick(
                85,
                () -> {
                    test.assertTrue(
                            pedestal.isSustained(),
                            "Vigilance must sustain with four ordinary and four bound candles");
                    test.assertEquals(
                            4,
                            pedestal.getBoundPlayers().size(),
                            "All four bindings must be accepted");
                    player.damage(
                            test.getWorld(),
                            test.getWorld().getDamageSources().mobAttack(attacker),
                            1);
                    test.assertTrue(
                            sentinel.isAwake() && sentinel.getTarget() == attacker,
                            "Bound player's attacker awakens nearby sentinel");
                });
        test.runAtTick(
                105,
                () -> {
                    test.assertTrue(
                            attacker.getHealth() < 20, "Awakened sentinel must slash its target");
                    test.removeBlock(new BlockPos(3, 1, 3));
                });
        test.runAtTick(
                108,
                () -> {
                    test.assertFalse(sentinel.isAwake(), "Removing rite makes sentinel dormant");
                    test.assertTrue(
                            sentinel.getTarget() == null, "Removing rite clears aggression");
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }
}
