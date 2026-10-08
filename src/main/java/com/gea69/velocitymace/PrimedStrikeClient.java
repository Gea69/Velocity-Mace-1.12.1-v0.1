package com.gea69.velocitymace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;

@EventBusSubscriber(
        modid = "velocitymace",
        value = Dist.CLIENT
)
public final class PrimedStrikeClient {

    private static final int COOLDOWN_TICKS = 100;

    private PrimedStrikeClient() {
    }

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        LocalPlayer player =
                minecraft.player;

        if (player == null) {
            return;
        }

        if (minecraft.level == null) {
            return;
        }

        if (minecraft.screen != null) {
            return;
        }

        ItemStack weapon =
                player.getMainHandItem();

        int level =
                PrimedStrike.getLevel(weapon);

        if (level <= 0) {
            return;
        }

        /*
         * LMB must remain physically held.
         */
        if (!minecraft.options.keyAttack.isDown()) {
            return;
        }

        /*
         * The local cooldown prevents sending a packet every
         * client tick after a successful trigger.
         */
        if (player.getCooldowns()
                .isOnCooldown(weapon.getItem())) {
            return;
        }

        double range =
                player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                        + PrimedStrike.getAdditionalRange(
                        level
                );

        Entity target =
                PrimedStrikeTargeting.findTarget(
                        player,
                        range
                );

        if (target == null) {
            /*
             * Still primed.
             *
             * Nothing happens until a target enters the
             * crosshair and valid range.
             */
            return;
        }

        PrimedStrikeNetworkClient.fire(
                target.getId()
        );

        /*
         * Start the client-side cooldown immediately.
         *
         * The server independently starts its authoritative
         * cooldown after accepting the attack.
         */
        player.getCooldowns()
                .addCooldown(
                        weapon.getItem(),
                        COOLDOWN_TICKS
                );
    }
}