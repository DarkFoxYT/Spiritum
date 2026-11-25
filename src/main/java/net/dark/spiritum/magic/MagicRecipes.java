package net.dark.spiritum.magic;

import net.dark.spiritum.Spiritum;
import net.dark.spiritum.registry.SpiritumContent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Range;

import java.util.List;

import static net.dark.spiritum.magic.OfferingRecipe.ingredients;

@SuppressWarnings("SameParameterValue")
public final class MagicRecipes {
    private static OfferingRecipe rite(String id,
            int candles, boolean persistent, Object... items) {
        return new OfferingRecipe(Spiritum.id(id), candles, new Range<>(0), persistent, ingredients(items), ItemStack.EMPTY);
    }

    private static OfferingRecipe rite(String id,
            int candles, int minGemCount, int maxGemCount, boolean persistent, Object... items) {
        return new OfferingRecipe(Spiritum.id(id), candles+minGemCount, new Range<>(minGemCount,maxGemCount), persistent, ingredients(items), ItemStack.EMPTY);
    }

    private static OfferingRecipe rite(String id,
            int candles, int gemCount, boolean persistent, Object... items) {
        return new OfferingRecipe(Spiritum.id(id), candles+gemCount, new Range<>(gemCount), persistent, ingredients(items), ItemStack.EMPTY);
    }

    private static OfferingRecipe rite(String id,
            ItemStack result, int candles, boolean persistent, Object... items) {
        return new OfferingRecipe(Spiritum.id(id), candles, new Range<>(0), persistent, ingredients(items), result);
    }

    public static final List<OfferingRecipe> RITUALS =
            List.of(
                    rite("rain", 1, false, Items.ICE, 1, Items.PRISMARINE_SHARD, 1),
                    rite("clear_skies", 1, false, Items.SPONGE, 1, Items.FEATHER, 1),
                    rite("thunder", 1, false, Items.NAUTILUS_SHELL, 1, Items.DRAGON_BREATH, 1),
                    rite("daytime", 2, false, Items.BLAZE_POWDER, 1, Items.GOLD_INGOT, 1),
                    rite("nighttime", 2, false, Items.INK_SAC, 1, Items.SPIDER_EYE, 1),
                    rite("warding", 4, true, SpiritumContent.ARGENT_NUGGET, 2, Items.GOLDEN_APPLE, 1),
                    rite(
                            "libido",
                            2,
                            true,
                            Items.HAY_BLOCK,
                            1,
                            Items.COCOA_BEANS,
                            1,
                            Items.POPPY,
                            1),
                    rite("zombie_summoning", 2, false, Items.ROTTEN_FLESH, 3),
                    rite("skeleton_summoning", 2, false, Items.BONE, 3),
                    rite("boat_summoning", 2, false, Items.OAK_PLANKS, 3),
                    rite(
                            "abundance",
                            3,
                            true,
                            Items.BONE_BLOCK,
                            1,
                            Items.REDSTONE,
                            1,
                            Items.GLOWSTONE_DUST,
                            1),
                    rite("argentic_transmutation", SpiritumContent.ARGENT_NUGGET.getDefaultStack(), 3, false, Items.IRON_INGOT, 1),
                    rite(
                            "leech_binding",
                            3,1,
                            false,
                            SpiritumContent.ARGENT_NUGGET,
                            1,
                            SpiritumContent.CALX_OF_HADES,
                            2,
                            SpiritumContent.HEX_ASH,
                            2),
                    rite(
                            "imp_binding",
                            3,1,
                            false,
                            SpiritumContent.LIVING_FLESH,
                            1,
                            SpiritumContent.CALX_OF_HADES,
                            2,
                            SpiritumContent.HEX_ASH,
                            2),
                    rite(
                            "lemure_binding",
                            3,1,
                            false,
                            SpiritumContent.SPIRIT_FRAGMENT,
                            1,
                            SpiritumContent.CALX_OF_HADES,
                            2,
                            SpiritumContent.HEX_ASH,
                            2),
                    rite("calling", 2, 1, false, Items.ENDER_PEARL, 2, SpiritumContent.HEX_ASH, 2),
                    rite("withering", 2, 1, 4, true, Items.WITHER_ROSE, 2, Items.SOUL_SAND, 4),
                    rite(
                            "vigilance",
                            4,1,4,
                            true,
                            SpiritumContent.HEX_ASH,
                            6,
                            SpiritumContent.CALX_OF_HADES,
                            2));

    public static OfferingRecipe ritual(Identifier id) {
        return RITUALS.stream().filter(r -> r.id().equals(id)).findFirst().orElse(null);
    }

    private MagicRecipes() {}
}
