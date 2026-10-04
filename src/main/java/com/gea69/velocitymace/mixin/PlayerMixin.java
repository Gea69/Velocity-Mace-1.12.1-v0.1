package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void velocityMace$storeAttackTarget(Entity target, CallbackInfo ci) {
        VelocityMaceAttackContext.setTarget(target);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void velocityMace$clearAttackTarget(Entity target, CallbackInfo ci) {
        VelocityMaceAttackContext.clearTarget();
    }
}