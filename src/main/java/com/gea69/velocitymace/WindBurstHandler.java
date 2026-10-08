package com.gea69.velocitymace;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class WindBurstHandler {

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
            return;
        }

        Vec3 attackerVelocity =
                VelocityMaceAttackContext.getAttackerVelocity();

        Vec3 targetVelocity =
                VelocityMaceAttackContext.getTargetVelocity();

        if (attackerVelocity == null) {
            return;
        }

        double relativeVelocity;

        if (targetVelocity != null) {
            relativeVelocity = attackerVelocity
                    .subtract(targetVelocity)
                    .length();
        } else {
            relativeVelocity = attackerVelocity.length();
        }

        if (relativeVelocity < VELOCITY_THRESHOLD) {
            return;
        }

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

        /*
         * FALL-FLYING / ELYTRA
         *
         * Requires Wind Burst III or higher.
         *
         * 50% straight upward
         * 50% in the direction the attacker is moving.
         */
        if (fallFlying) {

            if (enchantmentLevel < 3) {
                return;
            }

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
         * Do not modify the attacker.
         *
         * Add force to the target using:
         *
         * X = attacker's look X * force
         * Y = 0.1
         * Z = attacker's look Z * force
         */
        if (sprinting
                || swimming
                || crawling
                || riding) {

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
         * Requires Wind Burst II or higher.
         *
         * Only execute when every known special state is false.
         */
        if (!fallFlying
                && !flying
                && !sprinting
                && !swimming
                && !crawling
                && !riding) {

            if (enchantmentLevel < 2) {
                return;
            }

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