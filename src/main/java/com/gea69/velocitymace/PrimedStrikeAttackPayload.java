package com.gea69.velocitymace;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PrimedStrikeAttackPayload(
        int entityId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PrimedStrikeAttackPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "primed_strike_attack"
                    )
            );

    public static final StreamCodec<ByteBuf, PrimedStrikeAttackPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    PrimedStrikeAttackPayload::entityId,
                    PrimedStrikeAttackPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}