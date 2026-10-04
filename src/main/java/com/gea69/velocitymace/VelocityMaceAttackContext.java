package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VelocityMaceAttackContext {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("VelocityMaceAttackContext");

    private static final ThreadLocal<LivingEntity> ATTACKER = new ThreadLocal<>();
    private static final ThreadLocal<Entity> ATTACK_TARGET = new ThreadLocal<>();

    /*
     * These are snapshots taken at Player.attack() HEAD.
     *
     * They must not be replaced with getDeltaMovement() later in the
     * attack because the target's velocity can change as a result of
     * the attack itself.
     */
    private static final ThreadLocal<Vec3> ATTACKER_VELOCITY = new ThreadLocal<>();
    private static final ThreadLocal<Vec3> TARGET_VELOCITY = new ThreadLocal<>();

    private VelocityMaceAttackContext() {
    }

    public static void setAttack(
            LivingEntity attacker,
            Entity target,
            Vec3 attackerVelocity,
            Vec3 targetVelocity
    ) {
        ATTACKER.set(attacker);
        ATTACK_TARGET.set(target);
        ATTACKER_VELOCITY.set(attackerVelocity);

        if (targetVelocity != null) {
            TARGET_VELOCITY.set(targetVelocity);
        } else {
            TARGET_VELOCITY.remove();
        }

        LOGGER.info("===== SET ATTACK CONTEXT =====");
        LOGGER.info("Attacker: {}", describeEntity(attacker));
        LOGGER.info("Target: {}", describeEntity(target));
        LOGGER.info("Attacker velocity snapshot: {}", describeVelocity(attackerVelocity));
        LOGGER.info("Target velocity snapshot: {}", describeVelocity(targetVelocity));
        LOGGER.info("===== SET ATTACK CONTEXT COMPLETE =====");
    }

    public static LivingEntity getAttacker() {
        return ATTACKER.get();
    }

    public static Entity getTarget() {
        return ATTACK_TARGET.get();
    }

    public static Vec3 getAttackerVelocity() {
        return ATTACKER_VELOCITY.get();
    }

    public static Vec3 getTargetVelocity() {
        return TARGET_VELOCITY.get();
    }

    public static void clearTarget() {
        LOGGER.info("===== CLEAR ATTACK CONTEXT =====");
        LOGGER.info("Attacker being cleared: {}", describeEntity(ATTACKER.get()));
        LOGGER.info("Target being cleared: {}", describeEntity(ATTACK_TARGET.get()));
        LOGGER.info(
                "Attacker velocity snapshot being cleared: {}",
                describeVelocity(ATTACKER_VELOCITY.get())
        );
        LOGGER.info(
                "Target velocity snapshot being cleared: {}",
                describeVelocity(TARGET_VELOCITY.get())
        );

        ATTACKER.remove();
        ATTACK_TARGET.remove();
        ATTACKER_VELOCITY.remove();
        TARGET_VELOCITY.remove();

        LOGGER.info("===== CLEAR ATTACK CONTEXT COMPLETE =====");
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "<NULL>";
        }

        return entity
                + ", velocity="
                + describeVelocity(entity.getDeltaMovement());
    }

    private static String describeVelocity(Vec3 velocity) {
        if (velocity == null) {
            return "<NULL>";
        }

        return String.format(
                "(%.6f, %.6f, %.6f), speed=%.6f blocks/tick",
                velocity.x,
                velocity.y,
                velocity.z,
                velocity.length()
        );
    }
}