package net.dark.spiritum;

import net.dark.spiritum.block.*;
import net.dark.spiritum.block.entity.*;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.recipe.NeedleCoatingRecipe;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import java.util.*;

public class SpiritumGameTests {
    private static final BlockPos POS = new BlockPos(3, 2, 3);

    @GameTest
    public void selfBindingSplitsOneGem(TestContext test) {
        var player = test.createMockPlayer(GameMode.SURVIVAL);
        var stack = new ItemStack(ModContent.SPIRIT_GEM, 8);
        player.setStackInHand(Hand.MAIN_HAND, stack);
        player.setSneaking(true);
        stack.getItem().use(test.getWorld(), player, Hand.MAIN_HAND);
        test.assertTrue(stack.getCount() == 7 && SpiritBinding.player(stack).isEmpty(),
                "The remaining gems must stay unbound");
        test.assertTrue(player.getInventory().contains(gem -> gem.getCount() == 1
                        && SpiritBinding.player(gem).filter(player.getUuid()::equals).isPresent()),
                "Exactly one bound gem must be returned to inventory");
        var single = new ItemStack(ModContent.SPIRIT_GEM);
        player.setStackInHand(Hand.OFF_HAND, single);
        single.getItem().use(test.getWorld(), player, Hand.OFF_HAND);
        test.assertTrue(single.getCount() == 1
                        && SpiritBinding.player(single).filter(player.getUuid()::equals).isPresent(),
                "A single gem must bind in place");
        test.complete();
    }

    @GameTest
    public void abundanceRespectsRandomTickSpeed(TestContext test) {
        var world = test.getWorld();
        BlockPos origin = test.getAbsolutePos(POS);
        BlockPos cropPos = origin.east();
        world.setBlockState(cropPos.down(), Blocks.FARMLAND.getDefaultState());
        world.setBlockState(cropPos, Blocks.WHEAT.getDefaultState());
        world.setBlockState(cropPos.up(), Blocks.GLOWSTONE.getDefaultState());
        var rule = net.minecraft.world.rule.GameRules.RANDOM_TICK_SPEED;
        int original = world.getGameRules().getValue(rule);
        try {
            world.getGameRules().setValue(rule, 0, world.getServer());
            for (int i = 0; i < 100; i++)
                RitualEffects.sustain(world, origin, MagicRecipes.ritual("abundance"), i);
            test.assertTrue(
                    world.getBlockState(cropPos).get(CropBlock.AGE) == 0,
                    "Abundance must honor disabled random ticks");
            world.getGameRules().setValue(rule, 4096, world.getServer());
            for (int i = 0; i < 100; i++)
                RitualEffects.sustain(world, origin, MagicRecipes.ritual("abundance"), i);
            test.assertTrue(
                    world.getBlockState(cropPos).get(CropBlock.AGE) == 7,
                    "Abundance should accelerate crop random ticks");
        } finally {
            world.getGameRules().setValue(rule, original, world.getServer());
        }
        test.complete();
    }

    @GameTest
    public void gemSocketsAndHexbladeBinding(TestContext test) {
        PlayerEntity killer = test.createMockPlayer(GameMode.SURVIVAL);
        PlayerEntity victim = test.createMockPlayer(GameMode.SURVIVAL);
        ItemStack blade = new ItemStack(ModContent.HEXBLADE),
                gem = new ItemStack(ModContent.SPIRIT_GEM);
        killer.setStackInHand(Hand.MAIN_HAND, blade);
        killer.setStackInHand(Hand.OFF_HAND, gem);
        ModContent.HEXBLADE.use(test.getWorld(), killer, Hand.MAIN_HAND);
        test.assertTrue(
                gem.isEmpty() && SpiritBinding.socket(blade).isOf(ModContent.SPIRIT_GEM),
                "Blade should consume one gem into its socket");
        ServerLivingEntityEvents.AFTER_DEATH
                .invoker()
                .afterDeath(victim, test.getWorld().getDamageSources().playerAttack(killer));
        test.assertTrue(
                SpiritBinding.player(SpiritBinding.socket(blade))
                        .orElseThrow()
                        .equals(victim.getUuid()),
                "Hexblade player kills must bind the socketed gem");
        killer.setSneaking(true);
        ModContent.HEXBLADE.use(test.getWorld(), killer, Hand.MAIN_HAND);
        test.assertTrue(SpiritBinding.socket(blade).isEmpty(), "Sneak use should empty the socket");
        test.assertTrue(
                killer.getInventory().getMainStacks().stream()
                        .anyMatch(
                                s ->
                                        s.isOf(ModContent.SPIRIT_GEM)
                                                && SpiritBinding.player(s)
                                                        .orElse(new UUID(0, 0))
                                                        .equals(victim.getUuid())),
                "Ejected gem must retain its player binding");
        test.complete();
    }

    @GameTest
    public void potionNeedleAndRemoteVoodoo(TestContext test) {
        var world = test.getWorld();
        var target = test.createMockCreativeServerPlayerInWorld();
        PlayerEntity user = test.createMockPlayer(GameMode.SURVIVAL);
        ItemStack needle = new ItemStack(ModContent.ARGENT_NEEDLE),
                poppet = new ItemStack(ModContent.VOODOO_POPPET),
                gem = new ItemStack(ModContent.SPIRIT_GEM);
        ItemStack potion = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_POISON);
        var input = CraftingRecipeInput.create(2, 1, List.of(needle, potion));
        var recipe = new NeedleCoatingRecipe(CraftingRecipeCategory.MISC);
        test.assertTrue(
                recipe.matches(input, world),
                "Needles and potions should combine in a crafting inventory");
        ItemStack coated = recipe.craft(input, world.getRegistryManager());
        test.assertTrue(coated.contains(DataComponentTypes.POTION_CONTENTS), "Coating must preserve potion contents");
        test.assertTrue(
                recipe.getRecipeRemainders(input).stream()
                        .anyMatch(s -> s.isOf(Items.GLASS_BOTTLE)),
                "Coating should return an empty bottle");
        SpiritBinding.bind(gem, target);
        SpiritBinding.socket(poppet, gem);
        user.setStackInHand(Hand.MAIN_HAND, coated);
        user.setStackInHand(Hand.OFF_HAND, poppet);
        ModContent.ARGENT_NEEDLE.use(world, user, Hand.MAIN_HAND);
        int duration =
                potion.get(DataComponentTypes.POTION_CONTENTS)
                                .getEffects()
                                .iterator()
                                .next()
                                .getDuration()
                        / 4;
        test.assertTrue(
                target.hasStatusEffect(StatusEffects.POISON)
                        && target.getStatusEffect(StatusEffects.POISON).getDuration() == duration,
                "Remote potion duration must be exactly quartered");
        test.assertTrue(
                coated.isEmpty() && poppet.getDamage() == 1,
                "One application must consume the needle and wear the poppet");
        world.getServer().getPlayerManager().remove(target);
        test.complete();
    }

    @GameTest
    public void emptyHandBlockInteractions(TestContext test) {
        test.setBlockState(POS, ModContent.RITUAL_PEDESTAL);
        var player = test.createMockCreativeServerPlayerInWorld();
        BlockPos absolute = test.getAbsolutePos(POS);
        var hit = new BlockHitResult(Vec3d.ofCenter(absolute), Direction.UP, absolute, false);
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));
        player.interactionManager.interactBlock(
                player, test.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, hit);
        test.assertTrue(
                test.getBlockEntity(POS, PedestalBlockEntity.class).getOfferings().size() == 1,
                "Right click must insert held items");
        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        player.interactionManager.interactBlock(
                player, test.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, hit);
        test.assertTrue(
                test.getBlockEntity(POS, PedestalBlockEntity.class).getOfferings().isEmpty(),
                "Empty hand right click must extract offerings");
        test.setBlockState(POS, ModContent.HEXED_CANDLE);
        test.getBlockEntity(POS, CandleBlockEntity.class).light(4);
        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        player.interactionManager.interactBlock(
                player, test.getWorld(), player.getMainHandStack(), Hand.MAIN_HAND, hit);
        test.expectBlockProperty(POS, HexedCandleBlock.FLAME, 0);
        test.getWorld().getServer().getPlayerManager().remove(player);
        test.complete();
    }

    @GameTest(maxTicks = 130)
    public void wardingAndBreeding(TestContext test) {
        setupRite(test, "warding");
        var zombie = test.spawnEntity(EntityType.ZOMBIE, 5, 2, 3);
        test.waitAndRun(
                90,
                () -> {
                    var goals =
                            ((net.dark.spiritum.mixin.MobGoalsAccessor) zombie).spiritum$getGoals();
                    test.assertTrue(
                            goals.getGoals().stream()
                                    .anyMatch(g -> g.isRunning() && g.getPriority() == 0),
                            "Hostile mobs should run the priority-zero ward flee goal");
                    test.spawnEntity(EntityType.COW, 2, 2, 2).setAiDisabled(true);
                    test.spawnEntity(EntityType.COW, 4, 2, 2).setAiDisabled(true);
                    RitualEffects.sustain(
                            test.getWorld(),
                            test.getAbsolutePos(POS),
                            MagicRecipes.ritual("libido"),
                            1200);
                    test.assertTrue(
                            test.getEntitiesAround(EntityType.COW, 3, 2, 2, 5).size() == 3,
                            "Libido should breed two eligible same-type animals once per minute");
                    test.complete();
                });
    }

    @GameTest
    public void countedIngredientsAndLeftovers(TestContext test) {
        for (OfferingRecipe recipe : allRecipes(test)) {
            List<ItemStack> stacks = new ArrayList<>();
            recipe.ingredients().forEach((item, count) -> stacks.add(new ItemStack(item, count)));
            test.assertTrue(recipe.matches(stacks), recipe.id() + " should match");
            ItemStack first = stacks.getFirst();
            first.decrement(1);
            test.assertTrue(
                    !recipe.matches(stacks), recipe.id() + " must reject insufficient counts");
            first.increment(2);
            recipe.consume(stacks);
            test.assertTrue(
                    stacks.size() == 1 && stacks.getFirst().getCount() == 1,
                    recipe.id() + " must consume only its ingredients");
        }
        test.complete();
    }

    private static List<OfferingRecipe> allRecipes(TestContext test) {
        List<OfferingRecipe> recipes = new ArrayList<>(MagicRecipes.RITUALS);
        test.getWorld().getRecipeManager().getAllOfType(net.dark.spiritum.recipe.AlchemyRecipe.TYPE)
                .forEach(entry -> recipes.add(entry.value().offering(entry.id().getValue().toString())));
        return recipes;
    }

    @GameTest(maxTicks = 110)
    public void candleModesAndExpiry(TestContext test) {
        test.setBlockState(POS, ModContent.HEXED_CANDLE);
        CandleBlockEntity candle = test.getBlockEntity(POS, CandleBlockEntity.class);
        var pig = test.spawnEntity(EntityType.PIG, new Vec3d(4, 2, 3));
        pig.setAiDisabled(true);
        var world = test.getWorld();
        candle.light(1);
        // Flame application is exercised on an actual scheduled world tick below.
        test.waitAndRun(
                45,
                () -> {
                    test.assertTrue(pig.isOnFire(), "Blaze candle should ignite nearby entities");
                    candle.light(2);
                    test.waitAndRun(
                            45,
                            () -> {
                                test.assertTrue(
                                        pig.hasStatusEffect(StatusEffects.WEAKNESS),
                                        "Soul candle should apply weakness");
                                candle.light(3);
                                for (int i = 0; i < 1200; i++)
                                    CandleBlockEntity.tick(
                                            world,
                                            candle.getPos(),
                                            candle.getCachedState(),
                                            candle);
                                test.expectBlock(Blocks.AIR, POS);
                                test.complete();
                            });
                });
    }

    @GameTest(maxTicks = 150)
    public void instantRitualConsumesCandlesAndSummons(TestContext test) {
        PedestalBlockEntity pedestal = setupRite(test, "zombie_summoning");
        test.waitAndRun(
                110,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().isEmpty(), "Instant ritual must finish");
                    test.assertTrue(
                            pedestal.getOfferings().isEmpty(),
                            "Instant ritual must consume ingredients");
                    test.expectBlockProperty(new BlockPos(2, 2, 3), HexedCandleBlock.FLAME, 0);
                    test.expectBlockProperty(new BlockPos(4, 2, 3), HexedCandleBlock.FLAME, 0);
                    test.assertTrue(
                            !test.getWorld()
                                    .getEntitiesByClass(
                                            net.minecraft.entity.mob.ZombieEntity.class,
                                            new Box(pedestal.getPos()).expand(80),
                                            e -> e.age < 80)
                                    .isEmpty(),
                            "Rite must summon a zombie (wards may make it flee)");
                    test.complete();
                });
    }

    @GameTest(maxTicks = 150)
    public void persistentRitualEndsWhenSnuffed(TestContext test) {
        PedestalBlockEntity pedestal = setupRite(test, "abundance");
        test.waitAndRun(
                85,
                () -> {
                    test.assertTrue(
                            pedestal.getActiveRitual().equals("abundance"),
                            "Persistent ritual should stay active");
                    test.assertTrue(
                            pedestal.getOfferings().isEmpty(),
                            "Persistent ingredients should be consumed once");
                    CandleBlockEntity candle =
                            test.getBlockEntity(new BlockPos(2, 2, 3), CandleBlockEntity.class);
                    candle.snuff();
                    test.waitAndRun(
                            3,
                            () -> {
                                test.assertTrue(
                                        pedestal.getActiveRitual().isEmpty(),
                                        "Snuffing a candle should cancel the rite");
                                test.complete();
                            });
                });
    }

    private static PedestalBlockEntity setupRite(TestContext test, String id) {
        test.setBlockState(POS, ModContent.RITUAL_PEDESTAL);
        PedestalBlockEntity pedestal = test.getBlockEntity(POS, PedestalBlockEntity.class);
        PlayerEntity player = test.createMockPlayer(GameMode.SURVIVAL);
        player.setSneaking(true);
        OfferingRecipe recipe = MagicRecipes.ritual(id);
        recipe.ingredients()
                .forEach((item, count) -> pedestal.insert(new ItemStack(item, count), player));
        List<BlockPos> candles =
                List.of(
                        new BlockPos(2, 2, 3),
                        new BlockPos(4, 2, 3),
                        new BlockPos(3, 2, 2),
                        new BlockPos(3, 2, 4));
        for (int i = 0; i < recipe.candles(); i++) {
            test.setBlockState(candles.get(i), ModContent.HEXED_CANDLE);
            test.getBlockEntity(candles.get(i), CandleBlockEntity.class).light(4);
        }
        return pedestal;
    }

    @GameTest
    public void alchemyRecipesAndTimeout(TestContext test) {
        test.setBlockState(POS, ModContent.ALCHEMY_VAT);
        VatBlockEntity vat = test.getBlockEntity(POS, VatBlockEntity.class);
        var world = test.getWorld();
        var recipes = world.getRecipeManager().getAllOfType(net.dark.spiritum.recipe.AlchemyRecipe.TYPE);
        test.assertTrue(recipes.size() >= 6, "Built-in alchemy recipes must load from data");
        test.assertTrue(recipes.stream().anyMatch(entry -> entry.id().getValue().toString()
                        .equals("spiritum_test:compat_alchemy")),
                "Recipes from another namespace must load without Java registration");
        for (var entry : recipes) {
            OfferingRecipe recipe = entry.value().offering(entry.id().getValue().toString());
            test.killAllEntities(ItemEntity.class);
            vat.fill();
            recipe.ingredients()
                    .forEach(
                            (item, count) -> {
                                ItemEntity entity =
                                        new ItemEntity(
                                                world,
                                                vat.getPos().getX() + .5,
                                                vat.getPos().getY() + .7,
                                                vat.getPos().getZ() + .5,
                                                new ItemStack(item, count));
                                world.spawnEntity(entity);
                            });
            VatBlockEntity.tick(world, vat.getPos(), vat.getCachedState(), vat);
            test.assertTrue(
                    !vat.getCachedState().get(AlchemyVatBlock.FILLED),
                    "Completed alchemy should drain the vat");
            test.assertTrue(
                    world.getEntitiesByClass(
                                            ItemEntity.class,
                                            new Box(vat.getPos()).expand(2),
                                            e -> ItemStack.areItemsAndComponentsEqual(e.getStack(), recipe.outputStack()))
                                    .size()
                            == 1,
                    "Alchemy must output " + recipe.id());
            test.assertTrue(
                    world.getEntitiesByClass(ItemEntity.class, new Box(vat.getPos()).expand(2),
                            e -> e.getStack().isOf(recipe.output())).getFirst().getStack().getCount()
                            == recipe.outputStack().getCount(),
                    "Alchemy must use the correct output count for " + recipe.id());
            for (ItemStack byproduct : entry.value().byproducts())
                test.assertTrue(world.getEntitiesByClass(ItemEntity.class,
                                new Box(vat.getPos()).expand(2),
                                e -> ItemStack.areItemsAndComponentsEqual(e.getStack(), byproduct)
                                        && e.getStack().getCount() == byproduct.getCount()).size() == 1,
                        "Alchemy must return its data-defined byproducts");
        }
        test.killAllEntities(ItemEntity.class);
        vat.fill();
        world.spawnEntity(
                new ItemEntity(
                        world,
                        vat.getPos().getX() + .5,
                        vat.getPos().getY() + .7,
                        vat.getPos().getZ() + .5,
                        new ItemStack(Items.REDSTONE)));
        for (int i = 0; i < 600; i++)
            VatBlockEntity.tick(world, vat.getPos(), vat.getCachedState(), vat);
        test.assertTrue(
                !vat.getCachedState().get(AlchemyVatBlock.FILLED) && vat.getOfferings().isEmpty(),
                "Boiling should time out at 600 ticks and refund ingredients");
        test.assertTrue(
                !world.getEntitiesByClass(
                                ItemEntity.class,
                                new Box(vat.getPos()).expand(2),
                                e -> e.getStack().isOf(Items.REDSTONE))
                        .isEmpty(),
                "Timeout should return unused redstone");
        test.complete();
    }

    @GameTest
    public void offeringsSurviveSavingAndBreaking(TestContext test) {
        test.setBlockState(POS, ModContent.RITUAL_PEDESTAL);
        PedestalBlockEntity pedestal = test.getBlockEntity(POS, PedestalBlockEntity.class);
        PlayerEntity player = test.createMockPlayer(GameMode.SURVIVAL);
        player.setSneaking(true);
        for (int i = 0; i < 70; i++) pedestal.insert(new ItemStack(Items.COBBLESTONE, 64), player);
        var saved = pedestal.createNbtWithIdentifyingData(test.getWorld().getRegistryManager());
        var restored =
                (PedestalBlockEntity)
                        BlockEntity.createFromNbt(
                                pedestal.getPos(),
                                pedestal.getCachedState(),
                                saved,
                                test.getWorld().getRegistryManager());
        test.assertTrue(
                restored != null
                        && restored.getOfferings().stream().mapToInt(ItemStack::getCount).sum()
                                == 4480,
                "Unlimited pedestal offerings must survive serialization");
        test.removeBlock(POS);
        test.assertTrue(
                test
                                .getWorld()
                                .getEntitiesByClass(
                                        ItemEntity.class,
                                        new Box(pedestal.getPos()).expand(2),
                                        e -> e.getStack().isOf(Items.COBBLESTONE))
                                .stream()
                                .mapToInt(e -> e.getStack().getCount())
                                .sum()
                        == 4480,
                "Breaking the pedestal must drop its inventory");
        test.complete();
    }

    @GameTest
    public void instantWeatherAndTimeEffects(TestContext test) {
        var world = test.getWorld();
        BlockPos pos = test.getAbsolutePos(POS);
        RitualEffects.trigger(world, pos, MagicRecipes.ritual("rain"));
        test.assertTrue(world.getLevelProperties().isRaining(), "Rain ritual should set weather");
        RitualEffects.trigger(world, pos, MagicRecipes.ritual("thunder"));
        test.assertTrue(
                world.getLevelProperties().isThundering(), "Thunder ritual should set weather");
        RitualEffects.trigger(world, pos, MagicRecipes.ritual("clear_skies"));
        test.assertTrue(!world.getLevelProperties().isRaining(), "Clear ritual should set weather");
        var overworld = world.getServer().getWorld(net.minecraft.world.World.OVERWORLD);
        long originalTime = overworld.getTimeOfDay();
        try {
            for (long day : new long[] {0, 42, 100000}) {
                overworld.setTimeOfDay(day * 24000 + 23000);
                for (int cast = 0; cast < 3; cast++) {
                    RitualEffects.trigger(world, pos, MagicRecipes.ritual("daytime"));
                    test.assertTrue(overworld.getTimeOfDay() == day * 24000 + 6000,
                            "Day spell must preserve the day counter on repeated casts");
                    RitualEffects.trigger(world, pos, MagicRecipes.ritual("nighttime"));
                    test.assertTrue(overworld.getTimeOfDay() == day * 24000 + 18000,
                            "Night spell must preserve the day counter on repeated casts");
                }
            }
        } finally {
            overworld.setTimeOfDay(originalTime);
        }
        RitualEffects.trigger(world, pos, MagicRecipes.ritual("skeleton_summoning"));
        test.assertTrue(
                !test.getEntitiesAround(EntityType.SKELETON, 3, 3, 3, 3).isEmpty(),
                "Skeleton rite should summon a skeleton");
        RitualEffects.trigger(world, pos, MagicRecipes.ritual("argentic_transmutation"));
        test.assertTrue(
                !world.getEntitiesByClass(
                                ItemEntity.class,
                                new Box(pos).expand(2),
                                e -> e.getStack().isOf(ModContent.ARGENT_NUGGET))
                        .isEmpty(),
                "Transmutation must produce an argent nugget");
        test.complete();
    }
}
