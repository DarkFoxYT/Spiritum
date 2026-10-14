package net.dark.spiritum;

import net.dark.spiritum.block.entity.*;
import net.dark.spiritum.entity.*;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.registry.ModEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/** Real client startup, resource loading, world rendering and pedestal item synchronization. */
public class SpiritumClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var singleplayer = context.worldBuilder().create()) {
            singleplayer.getServer().runCommand("fill -8 -60 -8 8 -60 8 minecraft:stone");
            singleplayer.getServer().runCommand("tp @a 0.5 -59 6.5 180 15");
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                var world = server.getOverworld();
                                var player = server.getPlayerManager().getPlayerList().getFirst();
                                BlockPos pedestalPos = new BlockPos(0, -59, 0);
                                world.setBlockState(
                                        pedestalPos, ModContent.RITUAL_PEDESTAL.getDefaultState());
                                var pedestal =
                                        (PedestalBlockEntity) world.getBlockEntity(pedestalPos);
                                pedestal.insert(new ItemStack(Items.IRON_NUGGET), player);
                                pedestal.insert(new ItemStack(ModContent.SPIRIT_GEM), player);
                                for (int x : new int[] {-2, 2}) {
                                    BlockPos candlePos = new BlockPos(x, -59, 0);
                                    world.setBlockState(
                                            candlePos, ModContent.HEXED_CANDLE.getDefaultState());
                                    ((CandleBlockEntity) world.getBlockEntity(candlePos)).light(5);
                                }
                                world.setBlockState(
                                        new BlockPos(-3, -59, -2),
                                        ModContent.ALCHEMY_VAT.getDefaultState());
                                ((VatBlockEntity) world.getBlockEntity(new BlockPos(-3, -59, -2)))
                                        .fill();
                                world.setBlockState(
                                        new BlockPos(3, -59, -2),
                                        ModContent.HEXSTONE_BRICKS.getDefaultState());
                                for (var item : ModContent.ITEMS)
                                    player.getInventory().offerOrDrop(new ItemStack(item));
                                var leech = ModEntities.LEECH.create(world, SpawnReason.TRIGGERED);
                                leech.refreshPositionAndAngles(-2, -58, -2, 0, 0);
                                world.spawnEntity(leech);
                                var imp = ModEntities.IMP.create(world, SpawnReason.TRIGGERED);
                                imp.refreshPositionAndAngles(0, -59, -2, 0, 0);
                                world.spawnEntity(imp);
                                var lemure =
                                        ModEntities.LEMURE.create(world, SpawnReason.TRIGGERED);
                                lemure.refreshPositionAndAngles(2, -58, -2, 0, 0);
                                world.spawnEntity(lemure);
                                var energy =
                                        ModEntities.SPIRIT_ENERGY.create(
                                                world, SpawnReason.TRIGGERED);
                                energy.refreshPositionAndAngles(1, -58.5, -1, 0, 0);
                                world.spawnEntity(energy);
                            });
            context.waitTicks(20);
            singleplayer.getClientWorld().waitForChunksRender();
            context.takeScreenshot("spiritum-supplied-assets");
            context.runOnClient(
                    client -> {
                        if (!(client.world.getBlockEntity(new BlockPos(0, -59, 0))
                                        instanceof PedestalBlockEntity pedestal)
                                || pedestal.getOfferings().size() != 2)
                            throw new AssertionError(
                                    "Pedestal items must synchronize to the client");
                    });
            singleplayer.getServer().runCommand("time set midnight");
            singleplayer.getServer().runCommand("gamemode spectator @a");
            singleplayer.getServer().runCommand("tp @a 0.5 -56 5.5 180 35");
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                var world = server.getOverworld();
                                var player = server.getPlayerManager().getPlayerList().getFirst();
                                var pedestal =
                                        (PedestalBlockEntity)
                                                world.getBlockEntity(new BlockPos(0, -59, 0));
                                pedestal.insert(new ItemStack(Items.BONE_BLOCK), player);
                                pedestal.insert(new ItemStack(Items.REDSTONE), player);
                                pedestal.insert(new ItemStack(Items.GLOWSTONE_DUST), player);
                                BlockPos third = new BlockPos(0, -59, 2);
                                world.setBlockState(
                                        third, ModContent.HEXED_CANDLE.getDefaultState());
                                ((CandleBlockEntity) world.getBlockEntity(third)).light(4);
                            });
            context.waitFor(
                    client ->
                            client.world.getBlockEntity(new BlockPos(0, -59, 0))
                                            instanceof PedestalBlockEntity pedestal
                                    && pedestal.getActiveRitual().equals("abundance"));
            context.waitTicks(20);
            Object steadySigil = currentSigil(context);
            for (int tick = 0; tick < 60; tick++) {
                context.waitTicks(1);
                if (currentSigil(context) != steadySigil)
                    throw new AssertionError(
                            "Ritual glow must remain continuous across particle updates");
            }
            context.takeScreenshot("spiritum-emissive-ritual-plane");
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                ((CandleBlockEntity)
                                                server.getOverworld()
                                                        .getBlockEntity(new BlockPos(0, -59, 2)))
                                        .snuff();
                            });
            context.waitTicks(10);
            context.runOnClient(
                    client -> {
                        var pedestal =
                                (PedestalBlockEntity)
                                        client.world.getBlockEntity(new BlockPos(0, -59, 0));
                        if (!pedestal.getActiveRitual().isEmpty())
                            throw new AssertionError(
                                    "Snuffing a participating candle must stop the rendered"
                                            + " ritual");
                    });
            singleplayer.getServer().runCommand("gamemode survival @a");
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                var player = server.getPlayerManager().getPlayerList().getFirst();
                                ((net.dark.spiritum.magic.RingBearer) player)
                                        .spiritum$setRing(new ItemStack(ModContent.SUMMONERS_RING));
                                player.playerScreenHandler.sendContentUpdates();
                            });
            context.waitTicks(10);
            context.setScreen(
                    () ->
                            new net.minecraft.client.gui.screen.ingame.InventoryScreen(
                                    net.minecraft.client.MinecraftClient.getInstance().player));
            context.waitTicks(5);
            context.runOnClient(
                    client -> {
                        if (client.player.playerScreenHandler.slots.size() != 47
                                || !net.dark.spiritum.magic.RingMagic.equipped(client.player)
                                        .isOf(ModContent.SUMMONERS_RING))
                            throw new AssertionError(
                                    "The equipped ring must synchronize to the extra inventory"
                                            + " slot");
                    });
            context.takeScreenshot("spiritum-ring-inventory");
            context.runOnClient(
                    client ->
                            client.interactionManager.clickSlot(
                                    0,
                                    46,
                                    0,
                                    net.minecraft.screen.slot.SlotActionType.QUICK_MOVE,
                                    client.player));
            context.waitTicks(10);
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                if (!net.dark.spiritum.magic.RingMagic.equipped(
                                                server.getPlayerManager()
                                                        .getPlayerList()
                                                        .getFirst())
                                        .isEmpty())
                                    throw new AssertionError(
                                            "Client shift-click must unequip the ring on the"
                                                    + " server");
                            });
            context.setScreen(() -> null);
            singleplayer.getServer().runCommand("gamemode creative @a");
            context.waitTicks(5);
            context.runOnClient(
                    client ->
                            client.interactionManager.clickCreativeStack(
                                    new ItemStack(ModContent.WARDING_RING), 46));
            context.waitTicks(5);
            singleplayer
                    .getServer()
                    .runOnServer(
                            server -> {
                                if (!net.dark.spiritum.magic.RingMagic.warded(
                                        server.getPlayerManager().getPlayerList().getFirst()))
                                    throw new AssertionError(
                                            "Creative inventory ring packets must equip on the"
                                                    + " server");
                            });
            context.setScreen(
                    () ->
                            new net.minecraft.client.gui.screen.ingame.InventoryScreen(
                                    net.minecraft.client.MinecraftClient.getInstance().player));
            context.waitTicks(5);
            try {
                context.runOnClient(
                        client -> {
                            var tab =
                                    net.minecraft.item.ItemGroups.getGroups().stream()
                                            .filter(
                                                    group ->
                                                            group.getType()
                                                                    == net.minecraft.item.ItemGroup
                                                                            .Type.INVENTORY)
                                            .findFirst()
                                            .orElseThrow();
                            var select =
                                    net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
                                            .class
                                            .getDeclaredMethod(
                                                    "setSelectedTab",
                                                    net.minecraft.item.ItemGroup.class);
                            select.setAccessible(true);
                            select.invoke(client.currentScreen, tab);
                        });
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
            context.waitTicks(5);
            context.takeScreenshot("spiritum-creative-ring-inventory");
            testViewers(context);
        }
    }

    private static Object currentSigil(ClientGameTestContext context) {
        try {
            return context.computeOnClient(
                    client -> {
                        var field =
                                Class.forName("net.dark.spiritum.client.SpiritParticles")
                                        .getDeclaredField("sigils");
                        field.setAccessible(true);
                        var particles = (java.util.Map<?, ?>) field.get(null);
                        if (particles.size() != 1)
                            throw new AssertionError(
                                    "An active pedestal must have exactly one continuous sigil");
                        return particles.values().iterator().next();
                    });
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private static void testViewers(ClientGameTestContext context) {
        context.runOnClient(client -> {
            var recipes = net.dark.spiritum.client.compat.MagicRecipeView.alchemyRecipes();
            if (recipes.size() < 6)
                throw new AssertionError("Alchemy recipes must synchronize to the client");
        });
        try {
            var loader = net.fabricmc.loader.api.FabricLoader.getInstance();
            if (loader.isModLoaded("jei")) {
                context.waitFor(
                        client -> {
                            try {
                                var field =
                                        Class.forName(
                                                        "net.dark.spiritum.client.compat.SpiritumJeiPlugin")
                                                .getDeclaredField("runtime");
                                field.setAccessible(true);
                                return field.get(null) != null;
                            } catch (ReflectiveOperationException e) {
                                throw new AssertionError(e);
                            }
                        });
                for (String category : new String[] {"RITUALS", "ALCHEMY"}) {
                    context.runOnClient(
                            client -> {
                                var plugin =
                                        Class.forName(
                                                "net.dark.spiritum.client.compat.SpiritumJeiPlugin");
                                var field = plugin.getDeclaredField("runtime");
                                field.setAccessible(true);
                                Object runtime = field.get(null);
                                Object gui =
                                        Class.forName("mezz.jei.api.runtime.IJeiRuntime")
                                                .getMethod("getRecipesGui")
                                                .invoke(runtime);
                                Class.forName("mezz.jei.api.runtime.IRecipesGui")
                                        .getMethod("showTypes", java.util.List.class)
                                        .invoke(
                                                gui,
                                                java.util.List.of(
                                                        plugin.getField(category).get(null)));
                            });
                    context.waitTicks(5);
                    context.takeScreenshot("spiritum-jei-" + category.toLowerCase());
                }
            }
            if (loader.isModLoaded("roughlyenoughitems")) {
                for (String category : new String[] {"rituals", "alchemy"}) {
                    context.runOnClient(
                            client -> {
                                var identifierClass =
                                        Class.forName(
                                                "me.shedaniel.rei.api.common.category.CategoryIdentifier");
                                Object id =
                                        identifierClass
                                                .getMethod("of", String.class)
                                                .invoke(null, "spiritum:" + category);
                                var builderClass =
                                        Class.forName(
                                                "me.shedaniel.rei.api.client.view.ViewSearchBuilder");
                                Object builder = builderClass.getMethod("builder").invoke(null);
                                builderClass
                                        .getMethod("addCategory", identifierClass)
                                        .invoke(builder, id);
                                if (!(boolean) builderClass.getMethod("open").invoke(builder))
                                    throw new AssertionError(
                                            "REI must find Spiritum " + category + " displays");
                            });
                    context.waitTicks(5);
                    context.takeScreenshot("spiritum-rei-" + category);
                }
            }
            context.setScreen(() -> null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Recipe viewer integration failed", e);
        }
    }
}
