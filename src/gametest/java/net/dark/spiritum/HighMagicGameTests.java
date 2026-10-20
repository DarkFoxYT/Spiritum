package net.dark.spiritum;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.block.entity.*;
import net.dark.spiritum.entity.*;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

import java.util.*;

public class HighMagicGameTests {
    private static final BlockPos POS = new BlockPos(3, 2, 3);
    private static final List<BlockPos> CANDLES =
            List.of(
                    new BlockPos(1, 2, 3),
                    new BlockPos(5, 2, 3),
                    new BlockPos(3, 2, 1),
                    new BlockPos(3, 2, 5),
                    new BlockPos(2, 2, 2),
                    new BlockPos(4, 2, 4));

    private static PedestalBlockEntity setup(
            TestContext test, String id, int ordinary, List<UUID> bindings) {
        test.setBlockState(POS, ModContent.RITUAL_PEDESTAL);
        PedestalBlockEntity pedestal = test.getBlockEntity(POS, PedestalBlockEntity.class);
        PlayerEntity supplier = test.createMockPlayer(GameMode.SURVIVAL);
        supplier.setSneaking(true);
        MagicRecipes.ritual(id)
                .ingredients()
                .forEach((item, count) -> pedestal.insert(new ItemStack(item, count), supplier));
        for (int i = 0; i < ordinary + bindings.size(); i++) {
            test.setBlockState(CANDLES.get(i), ModContent.HEXED_CANDLE);
            var candle = test.getBlockEntity(CANDLES.get(i), CandleBlockEntity.class);
            candle.light(i < ordinary ? 4 : 5);
            if (i >= ordinary) candle.bind(bindings.get(i - ordinary));
        }
        return pedestal;
    }

    private static ServerPlayerEntity owner(TestContext test) {
        var owner = test.createMockCreativeServerPlayerInWorld();
        Vec3d location = Vec3d.ofCenter(test.getAbsolutePos(new BlockPos(6, 2, 6)));
        owner.refreshPositionAndAngles(location.x, location.y, location.z, 0, 0);
        owner.setNoGravity(true);
        owner.setVelocity(Vec3d.ZERO);
        return owner;
    }

    @GameTest(maxTicks = 180)
    public void impBindingTakesPriorityOverOtherSummons(TestContext test) {
        var owner = owner(test);
        var pedestal = setup(test, "imp_binding", 3, List.of(owner.getUuid()));
        var supplier = test.createMockPlayer(GameMode.SURVIVAL);
        pedestal.insert(new ItemStack(ModContent.SPIRIT_GEM), supplier);
        test.waitAndRun(20, () -> {
            test.assertTrue(pedestal.getActiveRitual().equals("imp_binding"), "Imp Binding must win when offers also match Lemure and Leech Binding");
            for (int i = 0; i < 4; i++) test.getBlockEntity(CANDLES.get(i), CandleBlockEntity.class).snuff();
            test.getWorld().getServer().getPlayerManager().remove(owner);
            test.complete();
        });
    }

    @GameTest(maxTicks = 140)
    public void persistentRitualExpiresAndSnuffsCandles(TestContext test) {
        var pedestal = setup(test, "abundance", 3, List.of());
        test.waitAndRun(90, () -> {
            try {
                var elapsed = PedestalBlockEntity.class.getDeclaredField("elapsed");
                elapsed.setAccessible(true);
                elapsed.setInt(pedestal, 11999);
            } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
            PedestalBlockEntity.tick(test.getWorld(), pedestal.getPos(), pedestal.getCachedState(), pedestal);
            test.assertTrue(pedestal.getActiveRitual().isEmpty(), "An aura must expire after ten minutes");
            for (int i = 0; i < 3; i++) test.assertTrue(test.getBlockState(CANDLES.get(i)).get(HexedCandleBlock.FLAME) == 0, "Expired auras must extinguish their fuel candles");
            test.complete();
        });
    }

    @GameTest(maxTicks = 180)
    public void leechBindingNeedsBoundCandle(TestContext test) {
        binding(test, "leech_binding", ModEntities.LEECH, 10, 2);
    }

    @GameTest(maxTicks = 180)
    public void impBindingNeedsBoundCandle(TestContext test) {
        binding(test, "imp_binding", ModEntities.IMP, 10, 4);
    }

    @GameTest(maxTicks = 180)
    public void lemureBindingNeedsBoundCandle(TestContext test) {
        binding(test, "lemure_binding", ModEntities.LEMURE, 15, 0);
    }

    private static <T extends OwnedDemonEntity> void binding(
            TestContext test, String id, EntityType<T> type, int health, int attack) {
        PedestalBlockEntity pedestal = setup(test, id, 4, List.of());
        test.waitAndRun(
                25,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().isEmpty(),
                            "High binding must reject four ordinary candles");
                    UUID summoner = UUID.randomUUID();
                    var candle = test.getBlockEntity(CANDLES.get(3), CandleBlockEntity.class);
                    candle.light(5);
                    candle.bind(summoner);
                    test.waitAndRun(
                            110,
                            () -> {
                                test.assertTrue(
                                        pedestal.getOfferings().isEmpty(),
                                        "Demon binding should consume ingredients");
                                List<T> demons =
                                        test.getWorld()
                                                .getEntitiesByType(
                                                        type,
                                                        new Box(pedestal.getPos()).expand(6),
                                                        e -> summoner.equals(e.getOwnerUuid()));
                                test.assertTrue(
                                        demons.size() == 1,
                                        "Binding should summon exactly one demon owned by the bound"
                                                + " soul");
                                test.assertTrue(
                                        demons.getFirst().getMaxHealth() == health
                                                && demons.getFirst()
                                                                .getAttributeValue(
                                                                        EntityAttributes
                                                                                .ATTACK_DAMAGE)
                                                        == attack,
                                        "Demon statistics must match the specification");
                                for (int i = 0; i < 4; i++)
                                    test.expectBlockProperty(
                                            CANDLES.get(i), HexedCandleBlock.FLAME, 0);
                                test.complete();
                            });
                });
    }

    @GameTest(maxTicks = 150)
    public void wardingRingPreventsCallingInvocation(TestContext test) {
        var owner = owner(test);
        ((RingBearer) owner).spiritum$setRing(new ItemStack(ModContent.WARDING_RING));
        var pedestal = setup(test, "calling", 2, List.of(owner.getUuid()));
        test.waitAndRun(
                5,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().isEmpty()
                                    && !pedestal.getOfferings().isEmpty(),
                            "Warding must reject Calling before consuming the offerings");
                    for (int i = 0; i < 3; i++)
                        test.getBlockEntity(CANDLES.get(i), CandleBlockEntity.class).snuff();
                    test.getWorld().getServer().getPlayerManager().remove(owner);
                    test.complete();
                });
    }

    @GameTest(maxTicks = 150)
    public void callingTeleportsStationaryPlayer(TestContext test) {
        var owner = owner(test);
        PedestalBlockEntity pedestal = setup(test, "calling", 2, List.of(owner.getUuid()));
        test.waitAndRun(
                115,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().isEmpty()
                                    && pedestal.getOfferings().isEmpty(),
                            "Calling should finish and consume offerings");
                    test.assertTrue(
                            owner.getEntityPos()
                                            .squaredDistanceTo(
                                                    Vec3d.ofBottomCenter(pedestal.getPos().up()))
                                    < .04,
                            "Calling must teleport the bound player atop the pedestal");
                    test.getWorld().getServer().getPlayerManager().remove(owner);
                    test.complete();
                });
    }

    @GameTest(maxTicks = 80)
    public void callingMovementConsumesOfferingsAndCandles(TestContext test) {
        var owner = owner(test);
        PedestalBlockEntity pedestal = setup(test, "calling", 2, List.of(owner.getUuid()));
        test.waitAndRun(
                20,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().equals("calling"),
                            "Calling must be activating before movement");
                    owner.refreshPositionAndAngles(
                            owner.getX() + 1, owner.getY(), owner.getZ(), 0, 0);
                    test.waitAndRun(
                            3,
                            () -> {
                                test.assertTrue(
                                        pedestal.getActiveRitual().isEmpty()
                                                && pedestal.getOfferings().isEmpty(),
                                        "Movement must cancel and consume the ingredients");
                                for (int i = 0; i < 3; i++)
                                    test.expectBlockProperty(
                                            CANDLES.get(i), HexedCandleBlock.FLAME, 0);
                                test.assertTrue(
                                        owner.squaredDistanceTo(Vec3d.ofCenter(pedestal.getPos()))
                                                > 4,
                                        "Cancelled Calling must not teleport");
                                test.getWorld().getServer().getPlayerManager().remove(owner);
                                test.complete();
                            });
                });
    }

    @GameTest(maxTicks = 120)
    public void witheringSupportsFourPlayerBindings(TestContext test) {
        var owner = owner(test);
        List<UUID> bindings =
                List.of(owner.getUuid(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        PedestalBlockEntity pedestal = setup(test, "withering", 2, bindings);
        var cow = test.spawnEntity(EntityType.COW, 2, 2, 3);
        cow.setAiDisabled(true);
        cow.setNoGravity(true);
        test.addInstantFinalTask(
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().equals("withering")
                                    && pedestal.getBoundPlayers().size() == 4,
                            "Withering must remain active with four distinct bound players");
                    test.assertTrue(
                            cow.hasStatusEffect(StatusEffects.WITHER)
                                    && cow.getStatusEffect(StatusEffects.WITHER).getAmplifier()
                                            == 0,
                            "Other living creatures should receive Wither I");
                    test.assertTrue(
                            !owner.hasStatusEffect(StatusEffects.WITHER),
                            "A bound player must be exempt");
                    var saved =
                            pedestal.createNbtWithIdentifyingData(
                                    test.getWorld().getRegistryManager());
                    var restored =
                            (PedestalBlockEntity)
                                    BlockEntity.createFromNbt(
                                            pedestal.getPos(),
                                            pedestal.getCachedState(),
                                            saved,
                                            test.getWorld().getRegistryManager());
                    test.assertTrue(
                            restored != null
                                    && restored.getBoundPlayers()
                                            .equals(new LinkedHashSet<>(bindings))
                                    && restored.getActiveRitual().equals("withering"),
                            "All optional bindings and six participating candles must survive"
                                    + " saving");
                    for (int i = 0; i < 6; i++)
                        test.getBlockEntity(CANDLES.get(i), CandleBlockEntity.class).snuff();
                    cow.removeStatusEffect(StatusEffects.WITHER);
                    test.getWorld().getServer().getPlayerManager().remove(owner);
                });
    }

    @GameTest
    public void impDamageIgnoresInvincibilityFrames(TestContext test) {
        var owner = owner(test);
        var pig = test.spawnEntity(EntityType.PIG, 3, 2, 3);
        pig.setAiDisabled(true);
        var imp = test.spawnEntity(ModEntities.IMP, 3, 2, 3);
        imp.setOwnerUuid(owner.getUuid());
        float initial = pig.getHealth();
        pig.damage(test.getWorld(), test.getWorld().getDamageSources().generic(), 1);
        imp.setTarget(pig);
        imp.tick();
        test.assertTrue(
                pig.getHealth() == initial - 5,
                "Imp attacks must deal four damage even immediately after another hit");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void leechDrainsThroughArmorAndUnlatches(TestContext test) {
        var owner = owner(test);
        var pig = test.spawnEntity(EntityType.PIG, 3, 2, 3);
        pig.setAiDisabled(true);
        pig.getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(20);
        var leech = test.spawnEntity(ModEntities.LEECH, 3, 2, 3);
        leech.setOwnerUuid(owner.getUuid());
        leech.setTarget(pig);
        leech.tick();
        test.assertTrue(leech.isLatched(), "Leech should latch upon reaching its target");
        float health = pig.getHealth();
        for (int i = 0; i < 100; i++) leech.tick();
        test.assertTrue(
                pig.getHealth() == health - 2,
                "Leech must drain one heart through armor after five seconds");
        test.assertTrue(
                !test.getWorld()
                        .getEntitiesByType(
                                ModEntities.SPIRIT_ENERGY,
                                new Box(leech.getBlockPos()).expand(3),
                                e -> owner.getUuid().equals(e.getOwnerUuid()))
                        .isEmpty(),
                "A successful drain should create an owner-bound healing orb");
        leech.damage(test.getWorld(), test.getWorld().getDamageSources().generic(), 1);
        test.assertTrue(!leech.isLatched(), "Taking damage must release the latch");
        leech.tick();
        test.assertTrue(!leech.isLatched(), "A short recovery must prevent immediate relatching");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void healingEnergyExpiresAfterTenSeconds(TestContext test) {
        var orb = test.spawnEntity(ModEntities.SPIRIT_ENERGY, 3, 2, 3);
        orb.setOwnerUuid(UUID.randomUUID());
        for (int i = 0; i < 199; i++) orb.tick();
        test.assertTrue(
                !orb.isRemoved() && orb.getLifeTicks() == 1, "Energy should survive for 199 ticks");
        orb.tick();
        test.assertTrue(orb.isRemoved(), "Energy must expire on tick 200");
        test.complete();
    }

    @GameTest
    public void impCollectsAndDeliversItems(TestContext test) {
        var owner = owner(test);
        var imp = test.spawnEntity(ModEntities.IMP, 3, 2, 3);
        imp.setOwnerUuid(owner.getUuid());
        var item =
                new ItemEntity(
                        test.getWorld(),
                        imp.getX(),
                        imp.getY(),
                        imp.getZ(),
                        new ItemStack(Items.DIAMOND, 3));
        item.resetPickupDelay();
        test.getWorld().spawnEntity(item);
        imp.tick();
        test.assertTrue(
                item.isRemoved() && imp.getCarriedStack().getCount() == 3,
                "Imp should collect a nearby stack");
        imp.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), 0, 0);
        imp.tick();
        test.assertTrue(
                imp.getCarriedStack().isEmpty()
                        && owner.getInventory().getMainStacks().stream()
                                .anyMatch(s -> s.isOf(Items.DIAMOND) && s.getCount() == 3),
                "Imp must deliver collected items into its owner's inventory");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void lemureRepairsFiveDurabilityEveryTwentySeconds(TestContext test) {
        var owner = owner(test);
        var lemure = test.spawnEntity(ModEntities.LEMURE, 6, 2, 6);
        lemure.setOwnerUuid(owner.getUuid());
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamage(20);
        owner.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, sword);
        for (int i = 0; i < 399; i++) lemure.tick();
        test.assertTrue(sword.getDamage() == 20, "Lemure should wait the full twenty seconds");
        lemure.tick();
        test.assertTrue(
                sword.getDamage() == 15 && lemure.getTarget() == null,
                "Lemure must repair five durability and never attack");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void impRespectsPickupOwnershipAndSavesCargo(TestContext test) {
        var owner = owner(test);
        var imp = test.spawnEntity(ModEntities.IMP, 3, 2, 3);
        imp.setOwnerUuid(owner.getUuid());
        var restricted =
                new ItemEntity(
                        test.getWorld(),
                        imp.getX(),
                        imp.getY(),
                        imp.getZ(),
                        new ItemStack(Items.EMERALD));
        restricted.setOwner(UUID.randomUUID());
        restricted.resetPickupDelay();
        test.getWorld().spawnEntity(restricted);
        imp.tick();
        test.assertTrue(
                !restricted.isRemoved() && imp.getCarriedStack().isEmpty(),
                "Imps must respect another player's pickup restriction");
        restricted.setOwner(null);
        restricted.setThrower(test.createMockPlayer(GameMode.SURVIVAL));
        imp.tick();
        test.assertTrue(
                restricted.isRemoved() && imp.getCarriedStack().isOf(Items.EMERALD),
                "Ordinary items thrown by other players may be collected");
        var restored = ModEntities.IMP.create(test.getWorld(), SpawnReason.LOAD);
        restored.copyFrom(imp);
        test.assertTrue(
                owner.getUuid().equals(restored.getOwnerUuid())
                        && restored.getCarriedStack().isOf(Items.EMERALD),
                "Owner and carried item must survive entity serialization");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void healingEnergyOnlyHealsItsOwner(TestContext test) {
        var owner = owner(test);
        owner.setHealth(16);
        var orb = test.spawnEntity(ModEntities.SPIRIT_ENERGY, 6, 2, 6);
        orb.setOwnerUuid(UUID.randomUUID());
        orb.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), 0, 0);
        orb.tick();
        test.assertTrue(
                !orb.isRemoved() && owner.getHealth() == 16,
                "A bystander cannot collect another owner's healing energy");
        orb.setOwnerUuid(owner.getUuid());
        orb.tick();
        test.assertTrue(
                orb.isRemoved() && owner.getHealth() == 18,
                "The owner should collect the energy and heal one heart");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest
    public void leechRespondsToOwnersAttack(TestContext test) {
        var owner = owner(test);
        var pig = test.spawnEntity(EntityType.PIG, 3, 2, 3);
        pig.setAiDisabled(true);
        var leech = test.spawnEntity(ModEntities.LEECH, 3, 2, 3);
        leech.setOwnerUuid(owner.getUuid());
        owner.onAttacking(pig);
        leech.tick();
        test.assertTrue(
                leech.getTarget() == pig && leech.isLatched(),
                "The owner's attack should direct the leech without manually assigning its target");
        test.getWorld().getServer().getPlayerManager().remove(owner);
        test.complete();
    }

    @GameTest(maxTicks = 320)
    public void witheringWorksWithoutBoundCandles(TestContext test) {
        // The 50-block aura reaches neighboring test structures. Wait for their player checks
        // to finish before enabling a second, deliberately unbound aura.
        test.waitAndRun(
                200,
                () -> {
                    var pedestal = setup(test, "withering", 2, List.of());
                    var cow = test.spawnEntity(EntityType.COW, 2, 2, 3);
                    cow.setAiDisabled(true);
                    cow.setNoGravity(true);
                    test.addInstantFinalTask(
                            () -> {
                                test.assertTrue(
                                        pedestal.getActiveRitual().equals("withering")
                                                && pedestal.getBoundPlayers().isEmpty(),
                                        "Withering should accept zero optional bound candles");
                                test.assertTrue(
                                        cow.hasStatusEffect(StatusEffects.WITHER),
                                        "Unbound Withering must still affect nearby creatures");
                                test.complete();
                            });
                });
    }
}
