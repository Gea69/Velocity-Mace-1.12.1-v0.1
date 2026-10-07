package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import com.gea69.velocitymace.WindBurstHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        WindBurstHandler.apply(
                level,
                enchantmentLevel
        );

        ci.cancel();
    }

    private boolean velocityMace$isWindBurst() {
        var enchantment =
                VelocityMaceAttackContext.getCurrentEnchantment();

        if (enchantment == null) {
            return false;
        }

        return enchantment.is(Enchantments.WIND_BURST);
    }
}