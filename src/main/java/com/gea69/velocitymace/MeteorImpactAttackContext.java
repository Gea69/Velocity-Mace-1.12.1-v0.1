
package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;

public final class MeteorImpactAttackContext {
    private static Entity attacker;
    private static Entity target;
    private static double speed;
    private static int level;

    private MeteorImpactAttackContext() {
    }

    public static void set(Entity newAttacker, Entity newTarget,
                           double speedBlocksPerSecond, int enchantmentLevel) {
        attacker = newAttacker;
        target = newTarget;
        speed = speedBlocksPerSecond;
        level = enchantmentLevel;
    }

    public static Hit consume(Entity hitTarget, Entity hitAttacker) {
        if (attacker != hitAttacker || target != hitTarget) {
            return null;
        }

        Hit hit = new Hit(attacker, target, speed, level);
        clear();
        return hit;
    }

    public static void clear() {
        attacker = null;
        target = null;
        speed = 0.0D;
        level = 0;
    }

    public record Hit(Entity attacker, Entity target, double speed, int level) {
    }
}