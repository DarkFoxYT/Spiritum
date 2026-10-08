package net.dark.spiritum.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TunedBladeTargetPayload(float yaw, float pitch) implements CustomPayload {
    public static final Id<TunedBladeTargetPayload> ID =
            new Id<>(Identifier.of("spiritum", "tuned_blade_target"));
    public static final PacketCodec<RegistryByteBuf, TunedBladeTargetPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.FLOAT,
                    TunedBladeTargetPayload::yaw,
                    PacketCodecs.FLOAT,
                    TunedBladeTargetPayload::pitch,
                    TunedBladeTargetPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
