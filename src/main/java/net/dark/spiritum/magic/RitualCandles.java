package net.dark.spiritum.magic;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.block.entity.CandleBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/** Selects distinct ordinary and bound candles instead of substituting one for the other. */
public final class RitualCandles {
    public static boolean requiresOwner(OfferingRecipe recipe) {
        return recipe.ownerCandles().minInclusive()>0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validCount(OfferingRecipe recipe, List<Integer> candles) {
        /*if (recipe.id().equals("vigilance") || recipe.id().equals("dominion")) return count >= 4 && count <= 8;
        return recipe.id().equals("withering")
                ? count >= 2 && count <= 6
                : count == recipe.candles();*/
        return candles.stream().filter(i -> i == 4).count() >= recipe.candles()-recipe.ownerCandles().minInclusive() &&
            recipe.ownerCandles().contains(candles.stream().filter(i -> i == 5).toList().size());
    }

    public static List<BlockPos> select(
            World world, BlockPos pedestal, OfferingRecipe recipe, List<BlockPos> available) {
        List<Integer> candlesType = available.stream().map(pos -> world.getBlockState(pos).get(HexedCandleBlock.FLAME,0)).toList();
        if (!validCount(recipe,candlesType))
            return List.of();

        List<BlockPos> ordinary = new ArrayList<>(), bound = new ArrayList<>();
        for (BlockPos pos : available)
            if (world.getBlockEntity(pos) instanceof CandleBlockEntity candle
                    && candle.availableFor(pedestal)) {
                (candle.getBoundPlayer() == null ? ordinary : bound).add(pos);
            }

        if (recipe.ownerCandles().maxInclusive()>0) {
            int normal = recipe.candles() - recipe.ownerCandles().minInclusive();
            if (ordinary.size() < normal || bound.size() < recipe.ownerCandles().minInclusive()) return List.of();
            List<BlockPos> selected = new ArrayList<>(ordinary.subList(0, normal));
            selected.addAll(bound.subList(0, Math.min(recipe.ownerCandles().maxInclusive(), bound.size())));
            return selected;
        }
        List<BlockPos> selected = new ArrayList<>();
        UUID binding = null;
        for (BlockPos pos : available) {
            //noinspection DataFlowIssue
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
