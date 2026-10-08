package net.dark.spiritum.magic;

import static net.dark.spiritum.magic.OfferingRecipe.ingredients;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.item.*;

import java.util.List;

public final class MagicRecipes {
    private static OfferingRecipe rite(
            String id, int candles, boolean persistent, Object... items) {
        return new OfferingRecipe(id, candles, persistent, ingredients(items), ItemStack.EMPTY);
    }

    public static final List<OfferingRecipe> RITUALS =
            List.of(
                    rite("rain", 1, false, Items.ICE, 1, Items.PRISMARINE_SHARD, 1),
                    rite("clear_skies", 1, false, Items.SPONGE, 1, Items.FEATHER, 1),
                    rite("thunder", 1, false, Items.NAUTILUS_SHELL, 1, Items.DRAGON_BREATH, 1),
                    rite("daytime", 2, false, Items.BLAZE_POWDER, 1, Items.GOLD_INGOT, 1),
                    rite("nighttime", 2, false, Items.INK_SAC, 1, Items.SPIDER_EYE, 1),
                    rite("warding", 4, true, ModContent.ARGENT_NUGGET, 2, Items.GOLDEN_APPLE, 1),
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
                    rite("argentic_transmutation", 4, false, Items.IRON_BLOCK, 1),
                    rite(
                            "leech_binding",
                            4,
                            false,
                            ModContent.ARGENT_NUGGET,
                            1,
                            ModContent.CALX_OF_HADES,
                            2,
                            ModContent.HEX_ASH,
                            2),
                    rite(
                            "imp_binding",
                            4,
                            false,
                            ModContent.LIVING_FLESH,
                            1,
                            ModContent.CALX_OF_HADES,
                            2,
                            ModContent.HEX_ASH,
                            2),
                    rite(
                            "lemure_binding",
                            4,
                            false,
                            ModContent.SPIRIT_FRAGMENT,
                            1,
                            ModContent.CALX_OF_HADES,
                            3,
                            ModContent.HEX_ASH,
                            2),
                    rite("calling", 3, false, Items.ENDER_PEARL, 2, ModContent.HEX_ASH, 2),
                    rite("withering", 2, true, Items.WITHER_ROSE, 2, Items.SOUL_SAND, 4),
                    rite("vigilance", 4, true, ModContent.HEX_ASH, 6, Items.ECHO_SHARD, 2, ModContent.CALX_OF_HADES, 2),
                    rite("dominion", 4, true, Items.NETHER_STAR, 1, ModContent.CALX_OF_HADES, 6, ModContent.ARGENT_INGOT, 3));
    public static OfferingRecipe ritual(String id) {
        return RITUALS.stream().filter(r -> r.id().equals(id)).findFirst().orElse(null);
    }

    private MagicRecipes() {}
}
