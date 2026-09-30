package net.dark.spiritum.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Vanilla data components provide save and multiplayer synchronization for bound gems and sockets.
 */
public final class SpiritBinding {
    public static Optional<UUID> player(ItemStack gem) {
        String id =
                gem.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                        .copyNbt()
                        .getString("SpiritumPlayer", "");
        try {
            return id.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public static String playerName(ItemStack gem) {
        return gem.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                .copyNbt()
                .getString("SpiritumPlayerName", "");
    }

    public static void bind(ItemStack gem, PlayerEntity player) {
        NbtComponent.set(
                DataComponentTypes.CUSTOM_DATA,
                gem,
                nbt -> {
                    nbt.putString("SpiritumPlayer", player.getUuidAsString());
                    nbt.putString("SpiritumPlayerName", player.getName().getString());
                });
        gem.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static ItemStack socket(ItemStack holder) {
        return holder.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)
                .copyFirstStack();
    }

    public static void socket(ItemStack holder, ItemStack gem) {
        if (gem.isEmpty()) holder.remove(DataComponentTypes.CONTAINER);
        else
            holder.set(
                    DataComponentTypes.CONTAINER,
                    ContainerComponent.fromStacks(List.of(gem.copyWithCount(1))));
    }

    private SpiritBinding() {}
}
