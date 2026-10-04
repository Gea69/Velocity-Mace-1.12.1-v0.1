package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("PlayerMixin");

    @Inject(method = "attack", at = @At("HEAD"))
    private void velocityMace$storeAttackTarget(
            Entity target,
            CallbackInfo ci
    ) {
        Player player = (Player) (Object) this;

        /*
         * The server is authoritative for the actual attack.
         *
         * We don't need to maintain a second, client-side attack context.
         */
        if (player.level().isClientSide()) {
            return;
        }

        Vec3 attackerVelocity = player.getDeltaMovement();
        Vec3 targetVelocity =
                target != null ? target.getDeltaMovement() : null;

        LOGGER.info("==================================================");
        LOGGER.info("Player.attack() HEAD - SERVER");
        LOGGER.info("Attacker: {}", player);
        LOGGER.info("Target: {}", target);
        LOGGER.info(
                "Attacker velocity snapshot: {}",
                attackerVelocity
        );
        LOGGER.info(
                "Attacker speed snapshot: {} blocks/tick",
                attackerVelocity.length()
        );
        LOGGER.info(
                "Target velocity snapshot: {}",
                targetVelocity
        );

        VelocityMaceAttackContext.setAttack(
                player,
                target,
                attackerVelocity,
                targetVelocity
        );

        LOGGER.info("Player.attack() HEAD processing complete.");
        LOGGER.info("==================================================");
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void velocityMace$clearAttackTarget(
            Entity target,
            CallbackInfo ci
    ) {
        Player player = (Player) (Object) this;

        /*
         * Only the server created the context, so only the server
         * needs to clear it.
         */
        if (player.level().isClientSide()) {
            return;
        }

        LOGGER.info("==================================================");
        LOGGER.info("Player.attack() RETURNED");
        LOGGER.info("Attack target: {}", target);
        LOGGER.info(
                "Player velocity at attack RETURN: {}",
                player.getDeltaMovement()
        );
        LOGGER.info("Clearing VelocityMaceAttackContext...");

        VelocityMaceAttackContext.clearTarget();

        LOGGER.info("Context cleared.");
        LOGGER.info("==================================================");
    }
}