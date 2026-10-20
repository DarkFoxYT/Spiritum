package net.dark.spiritum.magic;

import com.mojang.serialization.Codec;

import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public final class DissolvedRodGroups extends PersistentState {
    private static final Codec<DissolvedRodGroups> CODEC =
            Codec.STRING
                    .listOf()
                    .xmap(
                            saved -> new DissolvedRodGroups(new HashSet<>(saved)),
                            state -> new ArrayList<>(state.groups));
    public static final PersistentStateType<DissolvedRodGroups> TYPE =
            new PersistentStateType<>(
                    "spiritum_dissolved_rod_groups",
                    () -> new DissolvedRodGroups(new HashSet<>()),
                    CODEC,
                    null);
    private final Set<String> groups;

    private DissolvedRodGroups(Set<String> groups) {
        this.groups = groups;
    }

    public boolean contains(String group) {
        return groups.contains(group);
    }

    public void add(String group) {
        if (groups.add(group)) markDirty();
    }
}
