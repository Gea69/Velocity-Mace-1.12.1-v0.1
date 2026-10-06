package com.gea69.velocitymace;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class WindBurstHandler {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    private static final double VELOCITY_THRESHOLD = 0.3D;

    private static final double FORCE_I = 1.2D;
    private static final double FORCE_II = 1.75D;
    private static final double FORCE_III = 2.2D;

    private WindBurstHandler() {
    }

    public static void apply(
            ServerLevel level,
            int enchantmentLevel
    ) {
        Entity attacker =
                VelocityMaceAttackContext.getAttacker();

        Entity target =
                VelocityMaceAttackContext.getTarget();

        if (attacker == null || target == null) {
            LOGGER.debug(
                    "[VelocityMace] Wind Burst: missing attack context. attacker={}, target={}",
                    attacker,
                    target
            );
            return;
        }

        Vec3 attackerVelocity =
                VelocityMaceAttackContext.getAttackerVelocity();

        Vec3 targetVelocity =
                VelocityMaceAttackContext.getTargetVelocity();

        if (attackerVelocity == null) {
            LOGGER.debug(
                    "[VelocityMace] Wind Burst: attacker velocity snapshot missing."
            );
            return;
        }

        /*
         * Use the velocity snapshots from Player.attack() HEAD.
         */
        double relativeVelocity;

        if (targetVelocity != null) {
            relativeVelocity = attackerVelocity
                    .subtract(targetVelocity)
                    .length();
        } else {
            relativeVelocity =
                    attackerVelocity.length();
        }

        /*
         * Read the movement-state snapshots captured at
         * Player.attack() HEAD.
         */
        boolean fallFlying =
                VelocityMaceAttackContext.isFallFlying();

        boolean flying =
                VelocityMaceAttackContext.isFlying();

        boolean sprinting =
                VelocityMaceAttackContext.isSprinting();

        boolean swimming =
                VelocityMaceAttackContext.isSwimming();

        boolean crawling =
                VelocityMaceAttackContext.isCrawling();

        boolean riding =
                VelocityMaceAttackContext.isRiding();

        double force =
                getForce(enchantmentLevel);

        LOGGER.debug(
                "[VelocityMace] Wind Burst smash: " +
                        "fallflying={}, flying={}, sprint={}, swim={}, crawl={}, ride={}, " +
                        "attackerVelocity={}, targetVelocity={}, relativeVelocity={}, force={}",
                fallFlying,
                flying,
                sprinting,
                swimming,
                crawling,
                riding,
                attackerVelocity,
                targetVelocity,
                relativeVelocity,
                force
        );

        if (relativeVelocity < VELOCITY_THRESHOLD) {
            LOGGER.debug(
                    "[VelocityMace] Wind Burst rejected: relative velocity {} < {}",
                    relativeVelocity,
                    VELOCITY_THRESHOLD
            );
            return;
        }

        /*
         * FALL-FLYING
         *
         * 50% upward + 50% current movement direction.
         */
        if (fallFlying) {

            LOGGER.debug(
                    "[VelocityMace] Wind Burst behavior: FALL-FLYING"
            );

            Vec3 movement = attackerVelocity;

            if (movement.lengthSqr() < 1.0E-7D) {
                movement =
                        attacker.getLookAngle();
            } else {
                movement =
                        movement.normalize();
            }

            Vec3 impulse =
                    new Vec3(
                            0.0D,
                            force * 0.5D,
                            0.0D
                    ).add(
                            movement.scale(force * 0.5D)
                    );

            attacker.setDeltaMovement(
                    attacker.getDeltaMovement().add(impulse)
            );

            attacker.hurtMarked = true;
            return;
        }

        /*
         * SPRINT / SWIM / CRAWL / RIDE
         *
         * Absolutely no force is applied to the attacker.
         *
         * Target receives:
         *
         * X = attacker's look X * force
         * Y = 0.1
         * Z = attacker's look Z * force
         */
        if (sprinting
                || swimming
                || crawling
                || riding) {

            LOGGER.debug(
                    "[VelocityMace] Wind Burst behavior: TARGET PUNT"
            );

            Vec3 look =
                    attacker.getLookAngle();

            target.push(
                    look.x * force,
                    0.1D,
                    look.z * force
            );

            target.hurtMarked = true;
            return;
        }

        /*
         * NORMAL WIND BURST
         *
         * Every special state must explicitly be false.
         */
        if (!fallFlying
                && !flying
                && !sprinting
                && !swimming
                && !crawling
                && !riding) {

            LOGGER.debug(
                    "[VelocityMace] Wind Burst behavior: NORMAL VERTICAL LAUNCH"
            );

            attacker.setDeltaMovement(
                    attacker.getDeltaMovement().add(
                            0.0D,
                            force,
                            0.0D
                    )
            );

            attacker.hurtMarked = true;
        }
    }

    private static double getForce(int level) {
        return switch (level) {
            case 1 -> FORCE_I;
            case 2 -> FORCE_II;
            case 3 -> FORCE_III;
            default -> FORCE_III
                    + 0.35D * (level - 3);
        };
    }
}