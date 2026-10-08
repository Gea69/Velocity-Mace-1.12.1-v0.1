package com.gea69.velocitymace;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class PrimedStrikeNetwork {

    private static final int COOLDOWN_TICKS = 100;

    private PrimedStrikeNetwork() {
    }

    public static void register(
            RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar =
                event.registrar("1");

        registrar.playToServer(
                PrimedStrikeAttackPayload.TYPE,
                PrimedStrikeAttackPayload.STREAM_CODEC,
                PrimedStrikeNetwork::handleAttack
        );
    }

    private static void handleAttack(
            PrimedStrikeAttackPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack weapon =
                player.getMainHandItem();

        int level =
                PrimedStrike.getLevel(weapon);

        if (level <= 0) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(
                weapon.getItem()
        )) {
            return;
        }

        Entity target =
                player.level().getEntity(
                        payload.entityId()
                );

        if (target == null) {
            return;
        }

        double range =
                player.getAttributeValue(
                        Attributes.ENTITY_INTERACTION_RANGE
                )
                        + PrimedStrike.getAdditionalRange(level);

        /*
         * Never trust the target ID supplied by the client.
         *
         * Recalculate what the server says the player is
         * currently looking at.
         */
        Entity raycastTarget =
                PrimedStrikeTargeting.findTarget(
                        player,
                        range
                );

        if (raycastTarget != target) {
            return;
        }

        if (!PrimedStrike.canAttackTarget(
                player,
                target,
                level
        )) {
            return;
        }

        /*
         * Play the server-side swing so other players see
         * the attack animation as well.
         */
        player.swing(
                InteractionHand.MAIN_HAND
        );

        player.attack(target);

        player.getCooldowns().addCooldown(
                weapon.getItem(),
                COOLDOWN_TICKS
        );
    }
}