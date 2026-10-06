package com.gea69.velocitymace.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.effects.ExplodeEffect;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gea69.velocitymace.WindBurstHandler;

import java.util.List;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

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

        WindBurstHandler.apply(level, enchantmentLevel);

        ci.cancel();
    }

    private boolean velocityMace$isWindBurst() {
        Enchantment enchantment =
                (Enchantment) (Object) this;

        List<TargetedConditionalEffect<EnchantmentEntityEffect>> effects =
                enchantment.getEffects(
                        EnchantmentEffectComponents.POST_ATTACK
                );

        if (effects == null) {
            return false;
        }

        for (TargetedConditionalEffect<EnchantmentEntityEffect> effect
                : effects) {

            if (effect.enchanted()
                    != EnchantmentTarget.ATTACKER) {
                continue;
            }

            if (effect.affected()
                    != EnchantmentTarget.ATTACKER) {
                continue;
            }

            if (!(effect.effect() instanceof ExplodeEffect)) {
                continue;
            }

            return true;
        }

        return false;
    }
}