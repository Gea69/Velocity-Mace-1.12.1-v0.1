package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {

    /**
     * Minimum velocity, in blocks per tick, required for a smash attack.
     *
     * 0.3 blocks/tick = 6 blocks/second.
     */
    private static final double VELOCITY_SMASH_THRESHOLD = 0.3D;

    /**
     * Determines whether the attacker is moving fast enough relative to
     * the attack target to perform a mace smash attack.
     * @param attacker the entity attacking with the mace
     * @return true if the attack qualifies as a smash
     */
    @Overwrite
    public static boolean canSmashAttack(LivingEntity attacker) {
        try {
            Entity target = VelocityMaceAttackContext.getTarget();

            if (target != null) {
                double relativeVelocity = attacker
                        .getDeltaMovement()
                        .subtract(target.getDeltaMovement())
                        .length();

                return relativeVelocity >= VELOCITY_SMASH_THRESHOLD;
            }
        } catch (Exception ignored) {
            // Fall through to attacker-only velocity.
        }

        return attacker.getDeltaMovement().length()
                >= VELOCITY_SMASH_THRESHOLD;
    }

    /**
     * Replaces the vanilla fall-distance-based mace bonus with a
     * velocity-based bonus.
     * Bonus damage:
     *     (velocityMagnitude * 20)^1.06
     * The velocity is measured in blocks per tick, so multiplying by
     * 20 converts it to blocks per second.
     */
    @Overwrite
    public float getAttackDamageBonus(
            Entity target,
            float damage,
            DamageSource damageSource
    ) {
        if (!(damageSource.getEntity() instanceof LivingEntity attacker)) {
            return damage;
        }

        double velocityMagnitude;

        try {
            if (target != null) {
                velocityMagnitude = attacker
                        .getDeltaMovement()
                        .subtract(target.getDeltaMovement())
                        .length();
            } else {
                throw new IllegalStateException("Mace target was null");
            }
        } catch (Exception ignored) {
            velocityMagnitude = attacker.getDeltaMovement().length();
        }

        return damage
                + (float) Math.pow(
                velocityMagnitude * 20.0D,
                1.06D
        );
    }
}