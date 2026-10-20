package net.dark.spiritum;

import net.dark.spiritum.entity.SingingRodEntity;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.magic.Resonance;
import net.dark.spiritum.registry.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.TestContext;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

import java.util.*;

public class ResonanceTests {
    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 15)
    public void smithingUpgradePreservesBladeComponents(TestContext test) {
        var blade = new ItemStack(ModContent.TUNED_BLADE);
        blade.set(
                DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("A familiar blade"));
        blade.setDamage(123);
        var player = test.createMockCreativeServerPlayerInWorld();
        var gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        SpiritBinding.socket(blade, gem);
        var input =
                new net.minecraft.recipe.input.SmithingRecipeInput(
                        new ItemStack(ModContent.RETROSONUS_UPGRADE_TEMPLATE),
                        blade,
                        new ItemStack(ModContent.EXALTED_INGOT));
        var recipe =
                test.getWorld()
                        .getRecipeManager()
                        .getFirstMatch(
                                net.minecraft.recipe.RecipeType.SMITHING, input, test.getWorld())
                        .orElseThrow();
        test.assertEquals(
                ModContent.id("retrosonus"),
                recipe.id().getValue(),
                "New smithing recipe matches the template, blade and ingot");
        var result = recipe.value().craft(input, test.getWorld().getRegistryManager());
        test.assertTrue(result.isOf(ModContent.RETROSONUS), "Smithing produces Retrosonus");
        test.assertEquals(123, result.getDamage(), "Smithing preserves existing wear");
        test.assertEquals(
                blade.getCustomName(), result.getCustomName(), "Smithing preserves custom name");
        test.assertTrue(
                ItemStack.areEqual(gem, SpiritBinding.socket(result)),
                "Smithing preserves the original bound gem");
        test.assertFalse(
                recipe.value()
                        .matches(
                                new net.minecraft.recipe.input.SmithingRecipeInput(
                                        ItemStack.EMPTY,
                                        blade,
                                        new ItemStack(ModContent.EXALTED_INGOT)),
                                test.getWorld()),
                "Template is required");
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 25)
    public void rodsStandOnPartialBlockSurfaces(TestContext test) {
        test.setBlockState(2, 0, 2, net.minecraft.block.Blocks.STONE_SLAB);
        test.setBlockState(5, 0, 2, net.minecraft.block.Blocks.OAK_FENCE);
        var player = test.createMockCreativeServerPlayerInWorld();
        player.setPosition(test.getAbsolute(new Vec3d(1, 1, 1)));
        var blade = new ItemStack(ModContent.RETROSONUS);
        blade.addEnchantment(
                test.getWorld()
                        .getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(Resonance.ENCHANTMENT),
                1);
        player.setStackInHand(Hand.MAIN_HAND, blade);
        for (int x : new int[] {2, 5}) {
            var block = test.getAbsolutePos(new BlockPos(x, 0, 2));
            Vec3d point = Vec3d.ofBottomCenter(block).add(x == 5 ? .125 : 0, .5, 0);
            var hit =
                    new net.minecraft.util.hit.BlockHitResult(
                            point, x == 5 ? Direction.EAST : Direction.UP, block, false);
            player.getItemCooldownManager().set(blade, 0);
            test.assertEquals(
                    ActionResult.SUCCESS,
                    blade.getItem().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit)),
                    "Partial block supports a rod");
        }
        test.runAtTick(
                15,
                () -> {
                    var rods =
                            test.getWorld()
                                    .getEntitiesByClass(
                                            SingingRodEntity.class,
                                            new Box(test.getAbsolutePos(BlockPos.ORIGIN))
                                                    .expand(12),
                                            r -> player.getUuid().equals(r.owner()));
                    test.assertEquals(2, rods.size(), "Both partial-block rods remain placed");
                    for (var rod : rods) {
                        double expected =
                                test.getAbsolutePos(BlockPos.ORIGIN).getY()
                                        + (rod.getX() < test.getAbsolute(new Vec3d(4, 0, 0)).x
                                                ? .5
                                                : 1.5);
                        test.assertTrue(
                                Math.abs(rod.getY() - expected) < .001,
                                "Rod stays on the actual slab or fence surface");
                        test.assertEquals(
                                4f,
                                rod.getHeight(),
                                "Rod hitbox matches supplied four-block model");
                    }
                    test.getWorld().getServer().getPlayerManager().remove(player);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 100)
    public void retrosonusBindsEmptyGemOnPlayerKill(TestContext test) {
        var attacker = test.createMockCreativeServerPlayerInWorld();
        var victim = test.createMockCreativeServerPlayerInWorld();
        attacker.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
        victim.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
        test.getWorld()
                .getGameRules()
                .setValue(
                        net.minecraft.world.rule.GameRules.PVP, true, test.getWorld().getServer());
        var blade = new ItemStack(ModContent.RETROSONUS);
        SpiritBinding.socket(blade, new ItemStack(ModContent.SPIRIT_GEM));
        attacker.setStackInHand(Hand.MAIN_HAND, blade);
        test.runAtTick(
                70,
                () -> {
                    victim.setHealth(1);
                    victim.damage(
                            test.getWorld(),
                            test.getWorld().getDamageSources().playerAttack(attacker),
                            20);
                    test.assertTrue(!victim.isAlive(), "Player kill succeeds");
                    test.assertEquals(
                            victim.getUuid(),
                            SpiritBinding.player(SpiritBinding.socket(blade)).orElse(null),
                            "Retrosonus binds its empty gem to the slain player");
                    test.getWorld().getServer().getPlayerManager().remove(attacker);
                    test.getWorld().getServer().getPlayerManager().remove(victim);
                    test.complete();
                });
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 15)
    public void enchantedBladePlacesAndCompletesFourRods(TestContext test) {
        for (int x = 0; x < 8; x++)
            for (int z = 0; z < 8; z++)
                test.setBlockState(x, 0, z, net.minecraft.block.Blocks.STONE);
        var player = test.createMockCreativeServerPlayerInWorld();
        player.setPosition(test.getAbsolute(new Vec3d(1, 1, 1)));
        var blade = new ItemStack(ModContent.RETROSONUS);
        blade.addEnchantment(
                test.getWorld()
                        .getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(Resonance.ENCHANTMENT),
                1);
        player.setStackInHand(Hand.MAIN_HAND, blade);
        for (int[] p : new int[][] {{2, 2}, {6, 2}, {6, 6}, {2, 6}}) {
            var block = test.getAbsolutePos(new BlockPos(p[0], 0, p[1]));
            var hit =
                    new net.minecraft.util.hit.BlockHitResult(
                            Vec3d.ofBottomCenter(block.up()), Direction.UP, block, false);
            player.getItemCooldownManager().set(blade, 0);
            test.assertEquals(
                    ActionResult.SUCCESS,
                    blade.getItem().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hit)),
                    "Resonance places a rod on block use");
        }
        var rods =
                test.getWorld()
                        .getEntitiesByClass(
                                SingingRodEntity.class,
                                new Box(test.getAbsolutePos(BlockPos.ORIGIN)).expand(12),
                                rod -> player.getUuid().equals(rod.owner()));
        test.assertEquals(4, rods.size(), "Four uses place four rods");
        test.assertTrue(
                rods.stream().allMatch(SingingRodEntity::sealed),
                "Fourth rod completes and seals its group");
        test.assertEquals(4, Resonance.polygon(rods).size(), "Rods form a valid barrier");
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }

    private List<SingingRodEntity> rods(TestContext test) {
        String group = UUID.randomUUID().toString();
        var result = new ArrayList<SingingRodEntity>();
        for (int[] p : new int[][] {{2, 2}, {6, 2}, {6, 6}, {2, 6}}) {
            var rod = test.spawnEntity(ModEntities.SINGING_ROD, new Vec3d(p[0], 1, p[1]));
            rod.setNoGravity(true);
            rod.assign(UUID.randomUUID(), group);
            rod.seal();
            result.add(rod);
        }
        return result;
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 15)
    public void barrierStopsMovementButAllowsTeleportAndBreaksWithRod(TestContext test) {
        var rods = rods(test);
        var cow = test.spawnMob(EntityType.COW, new Vec3d(4, 1, 4));
        cow.setNoGravity(true);
        Vec3d start = cow.getEntityPos();
        test.assertEquals(10f, rods.getFirst().getMaxHealth(), "Rod has ten health");
        test.assertEquals(
                15.0,
                rods.getFirst().getAttributeValue(EntityAttributes.ARMOR),
                "Rod has full iron armor");
        cow.move(MovementType.SELF, new Vec3d(5, 0, 0));
        test.assertTrue(cow.getX() < start.x + 2.01, "Walking cannot cross four rods");
        cow.setPosition(start.add(5, 0, 0));
        test.assertEquals(start.x + 5, cow.getX(), "Teleportation can leave the barrier");
        cow.setPosition(start);
        rods.getFirst()
                .damage(test.getWorld(), test.getWorld().getDamageSources().genericKill(), 100);
        test.assertTrue(
                rods.stream().allMatch(SingingRodEntity::isRemoved),
                "Killing one rod removes the entire group");
        var lateRod = test.spawnEntity(ModEntities.SINGING_ROD, new Vec3d(3, 1, 3));
        lateRod.assign(UUID.randomUUID(), rods.getFirst().group());
        lateRod.tick();
        test.assertTrue(
                lateRod.isRemoved(), "An unloaded group member disappears when loaded later");
        cow.move(MovementType.SELF, new Vec3d(5, 0, 0));
        test.assertEquals(
                start.x + 5, cow.getX(), "Destroying a rod breaks the barrier immediately");
        test.complete();
    }

    @GameTest(structure = "spiritum-test:ritual_space", maxTicks = 15)
    public void resonanceLoadsAndRetrosonusKeepsSwordStats(TestContext test) {
        var blade = new ItemStack(ModContent.RETROSONUS);
        var enchantment =
                test.getWorld()
                        .getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(Resonance.ENCHANTMENT);
        blade.addEnchantment(enchantment, 1);
        test.assertFalse(
                enchantment.value().isAcceptableItem(new ItemStack(ModContent.TUNED_BLADE)),
                "Resonance cannot enchant a Tuned Blade");
        var oldTunedBlade = new ItemStack(ModContent.TUNED_BLADE);
        oldTunedBlade.addEnchantment(enchantment, 1);
        test.assertFalse(
                Resonance.enchanted(oldTunedBlade),
                "Legacy Resonance on a Tuned Blade no longer disables tracking");
        test.assertTrue(Resonance.enchanted(blade), "Resonance enchantment loads and applies");
        var attributes = blade.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        test.assertEquals(
                9.0,
                attributes.applyOperations(
                        EntityAttributes.ATTACK_DAMAGE, 1, EquipmentSlot.MAINHAND),
                "Retrosonus deals nine attack damage");
        test.assertTrue(
                Math.abs(
                                attributes.applyOperations(
                                                EntityAttributes.ATTACK_SPEED,
                                                4,
                                                EquipmentSlot.MAINHAND)
                                        - 1.4)
                        < .00001,
                "Retrosonus has 1.4 attack speed");
        test.assertEquals(
                3.5,
                attributes.applyOperations(
                        EntityAttributes.ENTITY_INTERACTION_RANGE, 3, EquipmentSlot.MAINHAND),
                "Retrosonus adds half a block of reach");
        var player = test.createMockCreativeServerPlayerInWorld();
        player.setStackInHand(Hand.MAIN_HAND, blade);
        var gem = new ItemStack(ModContent.SPIRIT_GEM);
        SpiritBinding.bind(gem, player);
        SpiritBinding.socket(blade, gem);
        test.assertEquals(
                ActionResult.PASS,
                blade.getItem().use(test.getWorld(), player, Hand.MAIN_HAND),
                "Resonance replaces tracking");
        test.assertFalse(player.isUsingItem(), "Resonance does not start guidance");
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }
}
