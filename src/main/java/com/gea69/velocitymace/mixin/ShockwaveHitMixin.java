package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.ShockwaveAttackContext;
import com.gea69.velocitymace.ShockwaveHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShockwaveHitMixin {

    @Inject(
            method = "hurt",
            at = @At("RETURN")
    )
    private void velocityMace$applyShockwaveAfterHit(
            DamageSource damageSource,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity target =
                (LivingEntity) (Object) this;

        if (target.level().isClientSide()) {
            return;
        }

        Entity attacker =
                damageSource.getEntity();

        if (attacker == null) {
            return;
        }

        ShockwaveAttackContext.Hit hit =
                ShockwaveAttackContext.consume(
                        target,
                        attacker
                );

        if (hit == null) {
            return;
        }

        if (!cir.getReturnValue()) {
            return;
        }

        ShockwaveHandler.apply(hit);
    }
}