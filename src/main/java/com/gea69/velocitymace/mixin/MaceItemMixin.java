package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;


@Mixin(MaceItem.class)
public abstract class MaceItemMixin {

    private static final double VELOCITY_SMASH_THRESHOLD = 0.3D;

    @Overwrite
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
                        .length()
                        >= VELOCITY_SMASH_THRESHOLD;
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

    @Inject(
            method = "getAttackDamageBonus",
            at = @At("HEAD")
    )
    private void velocityMace$captureWeapon(
            Entity target,
            float damage,
            DamageSource damageSource,
            CallbackInfoReturnable<Float> cir
    ) {
        if (target.level().isClientSide()) {
            return;
        }

        try {
            VelocityMaceAttackContext.setWeapon(
                    damageSource.getWeaponItem()
            );
        } catch (Exception ignored) {
            VelocityMaceAttackContext.setWeapon(
                    ItemStack.EMPTY
            );
        }
    }

    @Redirect(
            method = "hurtEnemy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void velocityMace$preserveUpwardVelocity(
            ServerPlayer attacker,
            Vec3 movement
    ) {
        Vec3 originalVelocity =
                VelocityMaceAttackContext.getAttackerVelocity();

        if (originalVelocity == null) {
            attacker.setDeltaMovement(movement);
            return;
        }

        double restoredY =
                Math.max(originalVelocity.y, 0.01D);

        attacker.setDeltaMovement(
                movement.with(
                        Direction.Axis.Y,
                        restoredY
                )
        );
    }

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

    @ModifyVariable(
            method = "getAttackDamageBonus",
            at = @At("STORE"),
            ordinal = 4
    )
    private float velocityMace$replaceSmashDamage(
            float value
    ) {
        double x =
                velocityMace$getVelocityPerSecond();

        double z =
                velocityMace$getWeaponDamage();

        return (float) velocityMace$calculateY(x, z);
    }

    private static double velocityMace$calculateY(
            double x,
            double z
    ) {
        if (x < 100.0D) {
            return z * (
                    -0.000586378907D * x * x
                            + 0.194071058D * x
                            - 0.143316706D
            );
        }

        return z * (
                13.4D
                        + 0.1667D * (x - 100.0D)
        );
    }

    private static double velocityMace$getVelocityPerSecond() {
        Vec3 attackerVelocity =
                VelocityMaceAttackContext
                        .getAttackerVelocity();

        Vec3 targetVelocity =
                VelocityMaceAttackContext
                        .getTargetVelocity();

        try {
            if (attackerVelocity != null
                    && targetVelocity != null) {

                return attackerVelocity
                        .subtract(targetVelocity)
                        .length()
                        * 20.0D;
            }

            if (attackerVelocity != null) {
                return attackerVelocity.length()
                        * 20.0D;
            }

            return 0.0D;

        } catch (Exception ignored) {
            if (attackerVelocity != null) {
                return attackerVelocity.length()
                        * 20.0D;
            }

            return 0.0D;
        }
    }

    private static double velocityMace$getWeaponDamage() {
        ItemStack weapon =
                VelocityMaceAttackContext.getWeapon();

        if (weapon == null || weapon.isEmpty()) {
            return 1.0D;
        }

        ItemAttributeModifiers modifiers =
                weapon.getAttributeModifiers();

        double attackDamage = 0.0D;

        for (ItemAttributeModifiers.Entry entry :
                modifiers.modifiers()) {

            if (!entry.attribute().equals(
                    Attributes.ATTACK_DAMAGE
            )) {
                continue;
            }

            AttributeModifier modifier =
                    entry.modifier();

            if (modifier.operation()
                    == AttributeModifier.Operation.ADD_VALUE) {

                attackDamage += modifier.amount();
            }
        }

        return attackDamage + 1.0D;
    }
}