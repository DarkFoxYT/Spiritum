package net.dark.spiritum.magic;

import net.minecraft.item.*;

import java.util.*;

public record OfferingRecipe(
        String id, int candles, boolean persistent, Map<Item, Integer> ingredients, Item output) {
    public static Map<Item, Integer> ingredients(Object... pairs) {
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            result.put((Item) pairs[i], (Integer) pairs[i + 1]);
        return Collections.unmodifiableMap(result);
    }

    public boolean matches(List<ItemStack> stacks) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        return ingredients.entrySet().stream()
                .allMatch(e -> counts.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    public void consume(List<ItemStack> stacks) {
        if (!matches(stacks)) throw new IllegalStateException("Missing ingredients for " + id);
        for (var entry : ingredients.entrySet()) {
            int needed = entry.getValue();
            for (ItemStack stack : stacks)
                if (stack.isOf(entry.getKey())) {
                    int take = Math.min(needed, stack.getCount());
                    stack.decrement(take);
                    needed -= take;
                    if (needed == 0) break;
                }
        }
        stacks.removeIf(ItemStack::isEmpty);
    }
}
