package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {

    private static final double VELOCITY_SMASH_THRESHOLD = 0.3D;

    /*
     * canSmashAttack() has no other vanilla logic that needs preserving,
     * so this remains an overwrite.
     */
    @org.spongepowered.asm.mixin.Overwrite
    public static boolean canSmashAttack(LivingEntity attacker) {
        if (attacker.level().isClientSide()) {
            return false;
        }

        try {
            Vec3 attackerVelocity =
                    VelocityMaceAttackContext.getAttackerVelocity();

            Vec3 targetVelocity =
                    VelocityMaceAttackContext.getTargetVelocity();

            if (attackerVelocity != null && targetVelocity != null) {
                return attackerVelocity
                        .subtract(targetVelocity)
                        .length() >= VELOCITY_SMASH_THRESHOLD;
            }

            if (attackerVelocity != null) {
                return attackerVelocity.length()
                        >= VELOCITY_SMASH_THRESHOLD;
            }

            return attacker.getDeltaMovement().length()
                    >= VELOCITY_SMASH_THRESHOLD;

        } catch (Exception ignored) {
            Vec3 attackerVelocity =
                    VelocityMaceAttackContext.getAttackerVelocity();

            if (attackerVelocity != null) {
                return attackerVelocity.length()
                        >= VELOCITY_SMASH_THRESHOLD;
            }

            return attacker.getDeltaMovement().length()
                    >= VELOCITY_SMASH_THRESHOLD;
        }
    }

    /*
     * Replace vanilla's effective fall-distance value with the
     * attack-start relative velocity converted to blocks/second.
     *
     * This is what Density should see.
     */
    @ModifyVariable(
            method = "getAttackDamageBonus",
            at = @At("STORE"),
            ordinal = 3
    )
    private float velocityMace$replaceEffectiveFallDistance(
            float value
    ) {
        return (float) velocityMace$getVelocityPerSecond();
    }

    /*
     * Replace vanilla's calculated smash-damage contribution with the
     * exact formula used by the previous @Overwrite:
     *
     *     velocityPerSecond ^ 1.06
     *
     * This prevents vanilla's piecewise fall-distance formula from
     * changing the final velocity-derived damage.
     */
    @ModifyVariable(
            method = "getAttackDamageBonus",
            at = @At("STORE"),
            ordinal = 4
    )
    private float velocityMace$replaceSmashDamage(
            float value
    ) {
        return (float) Math.pow(
                velocityMace$getVelocityPerSecond(),
                1.06D
        );
    }

    private static double velocityMace$getVelocityPerSecond() {
        Vec3 attackerVelocity =
                VelocityMaceAttackContext.getAttackerVelocity();

        Vec3 targetVelocity =
                VelocityMaceAttackContext.getTargetVelocity();

        try {
            double velocityMagnitude;

            if (attackerVelocity != null && targetVelocity != null) {
                velocityMagnitude = attackerVelocity
                        .subtract(targetVelocity)
                        .length();
            } else if (attackerVelocity != null) {
                velocityMagnitude = attackerVelocity.length();
            } else {
                return 0.0D;
            }

            return velocityMagnitude * 20.0D;

        } catch (Exception ignored) {
            if (attackerVelocity != null) {
                return attackerVelocity.length() * 20.0D;
            }

            return 0.0D;
        }
    }
}