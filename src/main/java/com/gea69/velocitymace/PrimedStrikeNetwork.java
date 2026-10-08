package com.gea69.velocitymace;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.ai.attributes.Attributes;

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

        if (player.getCooldowns()
                .isOnCooldown(weapon.getItem())) {
            return;
        }

        Entity target =
                player.level()
                        .getEntity(
                                payload.entityId()
                        );

        if (target == null) {
            return;
        }

        /*
         * Re-run the raycast on the server.
         *
         * We do not trust the target supplied by the client.
         */
        double range =
                player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                        + PrimedStrike.getAdditionalRange(level);

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
         * Use the normal Player.attack() pipeline.
         *
         * This is important because it allows your existing:
         *
         * PlayerMixin
         * MaceItemMixin
         * Breach
         * Wind Burst
         * velocity smash
         *
         * logic to operate exactly as it does for a normal attack.
         */
        player.attack(target);

        /*
         * Five seconds = 100 ticks.
         */
        player.getCooldowns()
                .addCooldown(
                        weapon.getItem(),
                        COOLDOWN_TICKS
                );
    }
}