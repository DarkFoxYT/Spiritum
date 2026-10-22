package net.dark.spiritum.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public final class RodOwnership extends PersistentState {
    public record Group(String group, int count, long readyAt) {
        private static final Codec<Group> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                Codec.STRING
                                                        .fieldOf("group")
                                                        .forGetter(Group::group),
                                                Codec.INT.fieldOf("count").forGetter(Group::count),
                                                Codec.LONG
                                                        .fieldOf("ready_at")
                                                        .forGetter(Group::readyAt))
                                        .apply(instance, Group::new));

        public boolean complete() {
            return count >= 4;
        }
    }

    public static final PersistentStateType<RodOwnership> TYPE =
            new PersistentStateType<>(
                    "spiritum_rod_ownership",
                    RodOwnership::new,
                    Codec.unboundedMap(Codec.STRING, Group.CODEC)
                            .xmap(RodOwnership::new, state -> state.groups),
                    null);
    private final Map<String, Group> groups;

    private RodOwnership() {
        this(Map.of());
    }

    private RodOwnership(Map<String, Group> groups) {
        this.groups = new HashMap<>(groups);
    }

    public Group get(UUID owner) {
        return groups.get(owner.toString());
    }

    public void put(UUID owner, Group group) {
        groups.put(owner.toString(), group);
        markDirty();
    }
}
