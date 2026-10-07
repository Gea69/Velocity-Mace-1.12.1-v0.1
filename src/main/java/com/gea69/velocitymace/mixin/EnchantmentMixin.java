package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import com.gea69.velocitymace.WindBurstHandler;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    private static final double BREACH_MIN_VELOCITY =
            6.0D;

    private static final double BREACH_MAX_VELOCITY =
            100.0D;

    @Inject(
            method = "doPostAttack(Lnet/minecraft/server/level/ServerLevel;ILnet/minecraft/world/item/enchantment/EnchantedItemInUse;Lnet/minecraft/world/item/enchantment/EnchantmentTarget;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velocityMace$modifyWindBurst(
            ServerLevel level,
            int enchantmentLevel,
            EnchantedItemInUse item,
            EnchantmentTarget target,
            Entity entity,
            DamageSource damageSource,
            CallbackInfo ci
    ) {
        if (target != EnchantmentTarget.ATTACKER) {
            return;
        }

        if (!velocityMace$isWindBurst()) {
            return;
        }

        WindBurstHandler.apply(
                level,
                enchantmentLevel
        );

        ci.cancel();
    }

    @Inject(
            method = "modifyArmorEffectiveness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velocityMace$modifyBreach(
            ServerLevel level,
            int enchantmentLevel,
            ItemStack stack,
            Entity user,
            DamageSource damageSource,
            MutableFloat armorEffectiveness,
            CallbackInfo ci
    ) {
        if (!velocityMace$isBreach()) {
            return;
        }

        double penetration =
                velocityMace$getBreachPenetration(
                        enchantmentLevel
                );

        float originalEffectiveness =
                armorEffectiveness.floatValue();

        float modifiedEffectiveness =
                (float) (
                        originalEffectiveness
                                * (1.0D - penetration)
                );

        armorEffectiveness.setValue(
                modifiedEffectiveness
        );

        ci.cancel();
    }

    private boolean velocityMace$isWindBurst() {
        Holder<Enchantment> enchantment =
                VelocityMaceAttackContext
                        .getCurrentEnchantment();

        if (enchantment == null) {
            return false;
        }

        return enchantment.is(
                Enchantments.WIND_BURST
        );
    }

    private boolean velocityMace$isBreach() {
        Holder<Enchantment> enchantment =
                VelocityMaceAttackContext
                        .getCurrentEnchantment();

        if (enchantment == null) {
            return false;
        }

        return enchantment.is(
                Enchantments.BREACH
        );
    }

    private static double velocityMace$getBreachPenetration(
            int enchantmentLevel
    ) {
        if (enchantmentLevel <= 0) {
            return 0.0D;
        }

        double velocity =
                velocityMace$getVelocityPerSecond();

        if (velocity < BREACH_MIN_VELOCITY) {
            return 0.0D;
        }

        /*
         * 100 blocks/sec = 100% penetration.
         */
        double velocityPenetration =
                velocity / BREACH_MAX_VELOCITY;

        /*
         * Level I   = 33.33%
         * Level II  = 66.67%
         * Level III = 100%
         * Level IV+ = 100%
         */
        double levelCap =
                Math.min(
                        enchantmentLevel / 3.0D,
                        1.0D
                );

        return Math.min(
                velocityPenetration,
                levelCap
        );
    }

    private static double velocityMace$getVelocityPerSecond() {
        Vec3 attackerVelocity =
                VelocityMaceAttackContext
                        .getAttackerVelocity();

        Vec3 targetVelocity =
                VelocityMaceAttackContext
                        .getTargetVelocity();

        if (attackerVelocity == null) {
            return 0.0D;
        }

        try {
            if (targetVelocity != null) {
                return attackerVelocity
                        .subtract(targetVelocity)
                        .length()
                        * 20.0D;
            }

            return attackerVelocity.length()
                    * 20.0D;

        } catch (Exception ignored) {
            return 0.0D;
        }
    }
}