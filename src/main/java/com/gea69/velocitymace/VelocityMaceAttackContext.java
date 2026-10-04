package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class VelocityMaceAttackContext {

    private static final ThreadLocal<LivingEntity> ATTACKER = new ThreadLocal<>();
    private static final ThreadLocal<Entity> ATTACK_TARGET = new ThreadLocal<>();

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
        ATTACKER.remove();
        ATTACK_TARGET.remove();
        ATTACKER_VELOCITY.remove();
        TARGET_VELOCITY.remove();
    }
}