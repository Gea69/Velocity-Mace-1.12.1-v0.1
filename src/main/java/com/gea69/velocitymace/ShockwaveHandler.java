package com.gea69.velocitymace;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ShockwaveHandler {

    private static final double KNOCKBACK_ZONE_RADIUS = 3.5D;

    private ShockwaveHandler() {
    }

    public static void apply(
            ShockwaveAttackContext.Hit hit
    ) {
        Entity attacker =
                hit.attacker();

        Entity primaryTarget =
                hit.target();

        if (attacker == null
                || primaryTarget == null
                || attacker.isRemoved()
                || primaryTarget.isRemoved()) {
            return;
        }

        if (!(primaryTarget.level() instanceof ServerLevel level)) {
            return;
        }

        float multiplier =
                Shockwave.getDamageMultiplier(
                        hit.enchantmentLevel()
                );

        if (multiplier <= 0.0F) {
            return;
        }

        float shockwaveDamage =
                hit.preDefenseDamage() * multiplier;

        if (shockwaveDamage <= 0.0F
                || !Float.isFinite(shockwaveDamage)) {
            return;
        }

        Vec3 center =
                primaryTarget.position();

        playEffect(
                level,
                primaryTarget,
                center
        );

        AABB zone =
                primaryTarget.getBoundingBox()
                        .inflate(KNOCKBACK_ZONE_RADIUS);

        List<Entity> nearbyEntities =
                level.getEntities(
                        primaryTarget,
                        zone,
                        entity ->
                                entity != primaryTarget
                                        && entity.isAlive()
                                        && entity.isAttackable()
                );

        DamageSource damageSource =
                hit.damageSource();

        for (Entity entity : nearbyEntities) {
            if (entity == primaryTarget) {
                continue;
            }

            if (entity.isRemoved() || !entity.isAlive()) {
                continue;
            }

            /*
             * Each nearby entity receives the same pre-defense
             * splash damage amount. Its own armor, resistance,
             * and other defenses are applied by Minecraft.
             */
            entity.hurt(
                    damageSource,
                    shockwaveDamage
            );
        }
    }

    private static void playEffect(
            ServerLevel level,
            Entity target,
            Vec3 center
    ) {
        double y =
                center.y + target.getBbHeight() * 0.5D;

        /*
         * Reuse the gust-emitter particles and wind-burst
         * sound associated with vanilla Wind Burst.
         */
        level.sendParticles(
                ParticleTypes.GUST_EMITTER_LARGE,
                center.x,
                y,
                center.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.GUST_EMITTER_SMALL,
                center.x,
                y,
                center.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.WIND_CHARGE_BURST,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}