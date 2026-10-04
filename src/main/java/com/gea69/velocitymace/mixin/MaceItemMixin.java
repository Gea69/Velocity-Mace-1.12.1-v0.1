package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MaceItemMixin");

    /*
     * 0.3 blocks/tick = 6 blocks/second.
     */
    private static final double VELOCITY_SMASH_THRESHOLD = 0.3D;

    /**
     * Determines whether the attack qualifies as a velocity-based smash.
     *
     * The server is authoritative. The client does not make a smash
     * determination.
     *
     * @param attacker the attacking entity
     * @return true if the attack-start relative velocity reaches the threshold
     */
    @Overwrite
    public static boolean canSmashAttack(LivingEntity attacker) {
        LOGGER.info("==================================================");
        LOGGER.info("canSmashAttack() ENTERED");

        /*
         * Do not allow the client to independently decide whether an
         * attack is a smash. The server makes the authoritative decision.
         */
        if (attacker.level().isClientSide()) {
            LOGGER.info("Side: CLIENT");
            LOGGER.info("Client-side smash check skipped.");
            LOGGER.info("canSmashAttack() OUTPUT = false");
            LOGGER.info("==================================================");
            return false;
        }

        LOGGER.info("Side: SERVER");
        LOGGER.info("Attacker: {}", attacker);
        LOGGER.info(
                "Current attacker velocity: {}",
                attacker.getDeltaMovement()
        );

        try {
            Vec3 attackerVelocity =
                    VelocityMaceAttackContext.getAttackerVelocity();

            Vec3 targetVelocity =
                    VelocityMaceAttackContext.getTargetVelocity();

            Entity target =
                    VelocityMaceAttackContext.getTarget();

            LOGGER.info(
                    "Attack-start attacker velocity snapshot: {}",
                    attackerVelocity
            );

            LOGGER.info(
                    "Attack-start target velocity snapshot: {}",
                    targetVelocity
            );

            LOGGER.info(
                    "Current target velocity: {}",
                    target != null
                            ? target.getDeltaMovement()
                            : "<NULL>"
            );

            /*
             * Preferred path:
             *
             * relative velocity = attacker snapshot - target snapshot
             *
             * Both snapshots were captured before attack processing could
             * apply knockback or otherwise modify the target's velocity.
             */
            if (attackerVelocity != null && targetVelocity != null) {
                Vec3 relativeVelocity =
                        attackerVelocity.subtract(targetVelocity);

                double relativeSpeed =
                        relativeVelocity.length();

                double relativeSpeedPerSecond =
                        relativeSpeed * 20.0D;

                LOGGER.info(
                        "Using attack-start RELATIVE velocity."
                );
                LOGGER.info(
                        "Relative velocity: {}",
                        relativeVelocity
                );
                LOGGER.info(
                        "Relative speed: {} blocks/tick",
                        relativeSpeed
                );
                LOGGER.info(
                        "Relative speed: {} blocks/second",
                        relativeSpeedPerSecond
                );
                LOGGER.info(
                        "Smash threshold: {} blocks/tick",
                        VELOCITY_SMASH_THRESHOLD
                );

                boolean result =
                        relativeSpeed >= VELOCITY_SMASH_THRESHOLD;

                LOGGER.info(
                        "Relative velocity comparison: {} >= {} = {}",
                        relativeSpeed,
                        VELOCITY_SMASH_THRESHOLD,
                        result
                );
                LOGGER.info(
                        "canSmashAttack() OUTPUT = {}",
                        result
                );
                LOGGER.info("==================================================");

                return result;
            }

            /*
             * Fallback:
             *
             * If the target snapshot is unavailable, use the attacker's
             * attack-start velocity rather than reading a potentially
             * modified current velocity.
             */
            if (attackerVelocity != null) {
                double attackerSpeed =
                        attackerVelocity.length();

                LOGGER.info(
                        "Target velocity snapshot unavailable."
                );
                LOGGER.info(
                        "Falling back to attack-start attacker-only velocity."
                );
                LOGGER.info(
                        "Attacker snapshot speed: {} blocks/tick",
                        attackerSpeed
                );
                LOGGER.info(
                        "Attacker snapshot speed: {} blocks/second",
                        attackerSpeed * 20.0D
                );

                boolean result =
                        attackerSpeed >= VELOCITY_SMASH_THRESHOLD;

                LOGGER.info(
                        "Attacker velocity comparison: {} >= {} = {}",
                        attackerSpeed,
                        VELOCITY_SMASH_THRESHOLD,
                        result
                );
                LOGGER.info(
                        "canSmashAttack() OUTPUT = {}",
                        result
                );
                LOGGER.info("==================================================");

                return result;
            }

            /*
             * Last-resort fallback. This should only happen if the attack
             * context was unavailable entirely.
             */
            double currentAttackerSpeed =
                    attacker.getDeltaMovement().length();

            LOGGER.warn(
                    "Attack velocity snapshot unavailable entirely."
            );
            LOGGER.warn(
                    "Falling back to CURRENT attacker velocity."
            );
            LOGGER.warn(
                    "Current attacker speed: {} blocks/tick",
                    currentAttackerSpeed
            );

            boolean result =
                    currentAttackerSpeed >= VELOCITY_SMASH_THRESHOLD;

            LOGGER.info(
                    "canSmashAttack() OUTPUT = {}",
                    result
            );
            LOGGER.info("==================================================");

            return result;

        } catch (Exception exception) {
            /*
             * If anything goes wrong while accessing the context, preserve
             * the requested fallback behavior.
             */
            LOGGER.error(
                    "Exception while calculating relative velocity.",
                    exception
            );

            Vec3 attackerVelocity =
                    VelocityMaceAttackContext.getAttackerVelocity();

            if (attackerVelocity != null) {
                double attackerSpeed =
                        attackerVelocity.length();

                boolean result =
                        attackerSpeed >= VELOCITY_SMASH_THRESHOLD;

                LOGGER.warn(
                        "Exception fallback using attacker snapshot: {} blocks/tick -> {}",
                        attackerSpeed,
                        result
                );

                LOGGER.info("==================================================");
                return result;
            }

            double currentAttackerSpeed =
                    attacker.getDeltaMovement().length();

            boolean result =
                    currentAttackerSpeed >= VELOCITY_SMASH_THRESHOLD;

            LOGGER.warn(
                    "Exception fallback using current attacker velocity: {} blocks/tick -> {}",
                    currentAttackerSpeed,
                    result
            );

            LOGGER.info("==================================================");
            return result;
        }
    }

    /**
     * Adds the custom velocity-based damage bonus.
     *
     * Server-side only:
     *
     *     velocityDamage = (velocity * 20)^1.06
     *
     * where velocity is the attack-start relative velocity in blocks/tick.
     */
    @Overwrite
    public float getAttackDamageBonus(
            Entity target,
            float damage,
            DamageSource damageSource
    ) {
        LOGGER.info("==================================================");
        LOGGER.info("getAttackDamageBonus() ENTERED");

        /*
         * The actual damage calculation is authoritative on the server.
         * Client-side calls simply preserve the incoming damage value.
         */
        if (target.level().isClientSide()) {
            LOGGER.info("Side: CLIENT");
            LOGGER.info(
                    "Client-side damage calculation skipped. Returning base damage: {}",
                    damage
            );
            LOGGER.info("==================================================");

            return damage;
        }

        LOGGER.info("Side: SERVER");
        LOGGER.info("Input target: {}", target);
        LOGGER.info("Input damage: {}", damage);

        LivingEntity attacker = null;

        Entity damageSourceEntity =
                damageSource.getEntity();

        if (damageSourceEntity instanceof LivingEntity livingEntity) {
            attacker = livingEntity;
        }

        LOGGER.info(
                "Attacker from DamageSource: {}",
                attacker
        );

        try {
            Vec3 attackerVelocity =
                    VelocityMaceAttackContext.getAttackerVelocity();

            Vec3 targetVelocity =
                    VelocityMaceAttackContext.getTargetVelocity();

            LOGGER.info(
                    "Attack-start attacker velocity snapshot: {}",
                    attackerVelocity
            );

            LOGGER.info(
                    "Attack-start target velocity snapshot: {}",
                    targetVelocity
            );

            /*
             * Preferred path: relative attack-start velocity.
             */
            if (attackerVelocity != null && targetVelocity != null) {
                Vec3 relativeVelocity =
                        attackerVelocity.subtract(targetVelocity);

                double velocityMagnitude =
                        relativeVelocity.length();

                double velocityPerSecond =
                        velocityMagnitude * 20.0D;

                double velocityDamage =
                        Math.pow(velocityPerSecond, 1.06D);

                float finalDamage =
                        (float) (damage + velocityDamage);

                LOGGER.info(
                        "Using attack-start RELATIVE velocity."
                );
                LOGGER.info(
                        "Relative velocity: {}",
                        relativeVelocity
                );
                LOGGER.info(
                        "Relative velocity magnitude: {} blocks/tick",
                        velocityMagnitude
                );
                LOGGER.info(
                        "Relative velocity magnitude: {} blocks/second",
                        velocityPerSecond
                );
                LOGGER.info(
                        "Velocity damage bonus: {}",
                        velocityDamage
                );
                LOGGER.info(
                        "Final returned damage: {}",
                        finalDamage
                );
                LOGGER.info("==================================================");

                return finalDamage;
            }

            /*
             * Fallback: attacker-only attack-start velocity.
             */
            if (attackerVelocity != null) {
                double velocityMagnitude =
                        attackerVelocity.length();

                double velocityPerSecond =
                        velocityMagnitude * 20.0D;

                double velocityDamage =
                        Math.pow(velocityPerSecond, 1.06D);

                float finalDamage =
                        (float) (damage + velocityDamage);

                LOGGER.warn(
                        "Target velocity snapshot unavailable."
                );
                LOGGER.warn(
                        "Falling back to attack-start attacker-only velocity."
                );
                LOGGER.info(
                        "Attacker speed: {} blocks/tick",
                        velocityMagnitude
                );
                LOGGER.info(
                        "Attacker speed: {} blocks/second",
                        velocityPerSecond
                );
                LOGGER.info(
                        "Velocity damage bonus: {}",
                        velocityDamage
                );
                LOGGER.info(
                        "Final returned damage: {}",
                        finalDamage
                );
                LOGGER.info("==================================================");

                return finalDamage;
            }

            /*
             * Last-resort fallback if there is no attack context at all.
             */
            if (attacker != null) {
                double velocityMagnitude =
                        attacker.getDeltaMovement().length();

                double velocityPerSecond =
                        velocityMagnitude * 20.0D;

                double velocityDamage =
                        Math.pow(velocityPerSecond, 1.06D);

                float finalDamage =
                        (float) (damage + velocityDamage);

                LOGGER.warn(
                        "Attack velocity snapshot unavailable entirely."
                );
                LOGGER.warn(
                        "Falling back to current attacker velocity."
                );
                LOGGER.info(
                        "Current attacker speed: {} blocks/tick",
                        velocityMagnitude
                );
                LOGGER.info(
                        "Velocity damage bonus: {}",
                        velocityDamage
                );
                LOGGER.info(
                        "Final returned damage: {}",
                        finalDamage
                );
                LOGGER.info("==================================================");

                return finalDamage;
            }

            LOGGER.warn(
                    "No attacker or velocity snapshot available."
            );
            LOGGER.info(
                    "Returning base damage: {}",
                    damage
            );
            LOGGER.info("==================================================");

            return damage;

        } catch (Exception exception) {
            LOGGER.error(
                    "Exception while calculating velocity damage.",
                    exception
            );

            /*
             * Requested fallback: attacker-only velocity.
             */
            if (attacker != null) {
                try {
                    Vec3 attackerVelocity =
                            VelocityMaceAttackContext.getAttackerVelocity();

                    if (attackerVelocity == null) {
                        attackerVelocity =
                                attacker.getDeltaMovement();
                    }

                    double velocityPerSecond =
                            attackerVelocity.length() * 20.0D;

                    double velocityDamage =
                            Math.pow(velocityPerSecond, 1.06D);

                    float finalDamage =
                            (float) (damage + velocityDamage);

                    LOGGER.warn(
                            "Exception fallback velocity damage: {}",
                            velocityDamage
                    );
                    LOGGER.info(
                            "Final returned damage: {}",
                            finalDamage
                    );
                    LOGGER.info("==================================================");

                    return finalDamage;

                } catch (Exception fallbackException) {
                    LOGGER.error(
                            "Exception during attacker-only fallback.",
                            fallbackException
                    );
                }
            }

            LOGGER.info(
                    "Returning base damage after fallback failure: {}",
                    damage
            );
            LOGGER.info("==================================================");

            return damage;
        }
    }
}