package net.dark.spiritum.magic;

import net.dark.spiritum.block.entity.CandleBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/** Selects distinct ordinary and bound candles instead of substituting one for the other. */
public final class RitualCandles {
    public static boolean requiresOwner(OfferingRecipe recipe) {
        return recipe.id().endsWith("_binding") || recipe.id().equals("calling");
    }

    public static boolean validCount(OfferingRecipe recipe, int count) {
        return recipe.id().equals("withering")
                ? count >= 2 && count <= 6
                : count == recipe.candles();
    }

    public static List<BlockPos> select(
            World world, BlockPos pedestal, OfferingRecipe recipe, List<BlockPos> available) {
        List<BlockPos> ordinary = new ArrayList<>(), bound = new ArrayList<>();
        for (BlockPos pos : available)
            if (world.getBlockEntity(pos) instanceof CandleBlockEntity candle
                    && candle.availableFor(pedestal)) {
                (candle.getBoundPlayer() == null ? ordinary : bound).add(pos);
            }
        if (requiresOwner(recipe)) {
            int normal = recipe.candles() - 1;
            if (ordinary.size() < normal || bound.isEmpty()) return List.of();
            List<BlockPos> selected = new ArrayList<>(ordinary.subList(0, normal));
            selected.add(bound.getFirst()); // The owner candle is extinguished last.
            return selected;
        }
        if (recipe.id().equals("withering")) {
            if (ordinary.size() < 2) return List.of();
            List<BlockPos> selected = new ArrayList<>(ordinary.subList(0, 2));
            selected.addAll(bound.subList(0, Math.min(4, bound.size())));
            return selected;
        }
        List<BlockPos> selected = new ArrayList<>();
        UUID binding = null;
        for (BlockPos pos : available) {
            UUID player = ((CandleBlockEntity) world.getBlockEntity(pos)).getBoundPlayer();
            if (player != null && binding != null && !player.equals(binding)) continue;
            if (player != null) binding = player;
            selected.add(pos);
            if (selected.size() == recipe.candles()) return selected;
        }
        return List.of();
    }

    private RitualCandles() {}
}
