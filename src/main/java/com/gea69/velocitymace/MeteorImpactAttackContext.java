
package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;

public final class MeteorImpactAttackContext {

    private static Entity attacker;
    private static Entity target;
    private static double speed;
    private static float preDefenseDamage;
    private static int level;

    private MeteorImpactAttackContext() {
    }

    public static void set(
            Entity newAttacker,
            Entity newTarget,
            double speedBlocksPerSecond,
            float newPreDefenseDamage,
            int enchantmentLevel
    ) {
        attacker = newAttacker;
        target = newTarget;
        speed = speedBlocksPerSecond;
        preDefenseDamage = newPreDefenseDamage;
        level = enchantmentLevel;
    }

    public static Hit consume(Entity hitTarget, Entity hitAttacker) {
        if (attacker != hitAttacker || target != hitTarget) {
            return null;
        }

        Hit hit = new Hit(
                attacker,
                target,
                speed,
                preDefenseDamage,
                level
        );

        clear();
        return hit;
    }

    public static void clear() {
        attacker = null;
        target = null;
        speed = 0.0D;
        preDefenseDamage = 0.0F;
        level = 0;
    }

    public record Hit(
            Entity attacker,
            Entity target,
            double speed,
            float preDefenseDamage,
            int level
    ) {
    }
}