
package com.gea69.velocitymace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public final class MeteorImpactHandler {
    private static final ResourceKey<DamageType> RECOIL_DAMAGE_TYPE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace", "meteor_impact_recoil"
                    )
            );

    private MeteorImpactHandler() {
    }

    public static void apply(MeteorImpactAttackContext.Hit hit) {
        if (!(hit.attacker() instanceof LivingEntity attacker)
                || !(hit.target() instanceof LivingEntity target)
                || !(target.level() instanceof ServerLevel level)
                || !attacker.isAlive()
                ) {
            return;
        }

        double speed = hit.speed();
        int enchantmentLevel = hit.level();

        Vec3 center = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ()
        );

        float power = MeteorImpact.getExplosionPower(speed);
        double fireRadius = MeteorImpact.getFireRadius(speed);
        boolean mobGriefing = level.getGameRules()
                .getBoolean(GameRules.RULE_MOBGRIEFING);

        /*
         * TNT interaction uses the TNT explosion drop-decay gamerule.
         * NONE still permits the explosion's entity damage and effects,
         * but prevents block destruction when mobGriefing is disabled.
         */
        Level.ExplosionInteraction interaction = mobGriefing
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;

        DamageSource explosionDamage = Explosion.getDefaultDamageSource(level, attacker);

        Explosion explosion = level.explode(
                attacker,
                explosionDamage,
                null,
                center.x,
                center.y,
                center.z,
                power,
                false,
                interaction,
                ParticleTypes.EXPLOSION,
                ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.GENERIC_EXPLODE
        );

        /*
         * Vanilla explosion damage is already applied by the explosion.
         * Add only the extra component above 100 blocks/second, keeping
         * the explosion's actual power and block destruction capped at 6.
         */
        if (speed > 100.0D && power > 0.0F) {
            applyOverCapExplosionDamage(
                    level, attacker, center, power, speed / 100.0D - 1.0D,
                    explosionDamage
            );
        }

        if (mobGriefing && fireRadius > 0.0D) {
            placeImpactFire(level, attacker, center, fireRadius);
        }

        /*
         * Ignite living entities in the explosion's damage range.
         * The attacker is excluded; the explosion source also excludes
         * the attacker from the normal explosion damage pass.
         */
        double blastRadius = power * 2.0D;
        AABB entityArea = new AABB(
                center.x - blastRadius, center.y - blastRadius, center.z - blastRadius,
                center.x + blastRadius, center.y + blastRadius, center.z + blastRadius
        );

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, entityArea,
                entity -> entity != attacker && entity.isAlive()
        )) {
            if (entity.distanceToSqr(center) <= blastRadius * blastRadius) {
                entity.igniteForSeconds(8.0F);
            }
        }

        applyRecoil(attacker, enchantmentLevel, speed);
    }

    private static void applyOverCapExplosionDamage(
            ServerLevel level,
            LivingEntity attacker,
            Vec3 center,
            float power,
            double extraMultiplier,
            DamageSource damageSource
    ) {
        double damageRadius = power * 2.0D;
        if (damageRadius <= 0.0D || extraMultiplier <= 0.0D) {
            return;
        }

        AABB area = new AABB(
                center.x - damageRadius, center.y - damageRadius, center.z - damageRadius,
                center.x + damageRadius, center.y + damageRadius, center.z + damageRadius
        );

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, area,
                candidate -> candidate != attacker && candidate.isAlive()
        )) {
            double distance = Math.sqrt(entity.distanceToSqr(center));
            double normalizedDistance = distance / damageRadius;

            if (normalizedDistance >= 1.0D) {
                continue;
            }

            double exposure = Explosion.getSeenPercent(center, entity);
            double impact = (1.0D - normalizedDistance) * exposure;
            if (impact <= 0.0D) {
                continue;
            }

            /*
             * Vanilla's explosion damage formula for the capped power.
             * Scale only the additional damage beyond the capped explosion.
             */
            double cappedExplosionDamage =
                    (impact * impact + impact) * 7.0D * power + 1.0D;

            float extraDamage = (float) (Math.floor(cappedExplosionDamage)
                    * extraMultiplier);

            if (extraDamage > 0.0F) {
                entity.hurt(damageSource, extraDamage);
            }
        }
    }

    private static void placeImpactFire(
            ServerLevel level,
            LivingEntity attacker,
            Vec3 center,
            double radius
    ) {
        BlockPos attackerPos = attacker.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = (int) Math.floor(center.x - radius);
        int maxX = (int) Math.ceil(center.x + radius);
        int minY = Math.max(level.getMinBuildHeight(),
                (int) Math.floor(center.y - radius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1,
                (int) Math.ceil(center.y + radius));
        int minZ = (int) Math.floor(center.z - radius);
        int maxZ = (int) Math.ceil(center.z + radius);

        double radiusSquared = radius * radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = (x + 0.5D) - center.x;
                    double dy = (y + 0.5D) - center.y;
                    double dz = (z + 0.5D) - center.z;

                    if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                        continue;
                    }

                    // Never place fire in the 3x3 X/Z area around the attacker.
                    if (Math.abs(x - attackerPos.getX()) <= 1
                            && Math.abs(z - attackerPos.getZ()) <= 1) {
                        continue;
                    }

                    pos.set(x, y, z);

                    if (!level.isEmptyBlock(pos)
                            || level.random.nextInt(3) != 0) {
                        continue;
                    }

                    if (!Blocks.FIRE.defaultBlockState().canSurvive(level, pos)) {
                        continue;
                    }

                    level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void applyRecoil(
            LivingEntity attacker,
            int enchantmentLevel,
            double speed
    ) {
        double maxHealth = attacker.getMaxHealth();
        int rawDamage = MeteorImpact.getRawRecoilDamage(maxHealth, speed);

        if (rawDamage <= 0) {
            return;
        }

        double armorPoints = Math.max(0.0D, attacker.getArmorValue());
        double armorCap = Math.max(0, enchantmentLevel - 1) * 5.0D;

        // Each armor point contributes 1.25%, up to the level-specific cap.
        double armorReduction = Math.min(armorPoints, armorCap) * 0.0125D;

        int protectionPoints = getProtectionPoints(attacker);
        double protectionCap = Math.max(0, enchantmentLevel - 1) * 10.0D;

        // Each protection point contributes 1.875%, up to the level cap.
        double protectionReduction =
                Math.min(protectionPoints, protectionCap) * 0.01875D;

        double totalReduction = Math.min(
                MeteorImpact.getMaximumRecoilReduction(enchantmentLevel),
                armorReduction + protectionReduction
        );

        double remainingDamage = rawDamage * (1.0D - totalReduction);

        // Keep at least 1 damage unless the recoil is fully mitigated.
        int finalDamage = totalReduction >= 1.0D
                ? 0
                : Math.max(1, (int) Math.floor(remainingDamage));

        if (finalDamage > 0) {
            DamageSource recoilSource =
                    attacker.damageSources().source(RECOIL_DAMAGE_TYPE);

            attacker.hurt(recoilSource, finalDamage);
        }
    }

    private static int getProtectionPoints(LivingEntity attacker) {
        int points = 0;

        for (ItemStack armorPiece : attacker.getArmorSlots()) {
            for (var entry : armorPiece.getEnchantments().entrySet()) {
                String id = entry.getKey().unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("");

                int weight = switch (id) {
                    case "minecraft:blast_protection" -> 4;
                    case "minecraft:fire_protection" -> 3;
                    case "minecraft:projectile_protection" -> 2;
                    case "minecraft:protection" -> 1;
                    default -> 0;
                };

                points += weight * entry.getIntValue();
            }
        }

        return points;
    }
}