package com.gea69.velocitymace;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public final class ShockwaveAttackContext {

    private static Hit pendingHit;

    private ShockwaveAttackContext() {
    }

    public static void set(
            Entity attacker,
            Entity target,
            DamageSource damageSource,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
        pendingHit = new Hit(
                attacker,
                target,
                damageSource,
                preDefenseDamage,
                enchantmentLevel
        );
    }

    public static Hit consume(
            Entity target,
            Entity attacker
    ) {
        Hit hit = pendingHit;

        if (hit == null) {
            return null;
        }

        if (hit.target() != target || hit.attacker() != attacker) {
            return null;
        }

        pendingHit = null;
        return hit;
    }

    public static void clear() {
        pendingHit = null;
    }

    public record Hit(
            Entity attacker,
            Entity target,
            DamageSource damageSource,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
    }
}