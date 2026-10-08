package com.gea69.velocitymace;

import net.neoforged.neoforge.network.PacketDistributor;

public final class PrimedStrikeNetworkClient {

    private PrimedStrikeNetworkClient() {
    }

    public static void fire(int entityId) {
        PacketDistributor.sendToServer(
                new PrimedStrikeAttackPayload(entityId)
        );
    }
}