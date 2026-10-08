package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.PrimedStrike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "startAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velocityMace$cancelNormalPrimedStrikeAttack(
            CallbackInfoReturnable<Boolean> cir
    ) {
        Minecraft minecraft =
                (Minecraft) (Object) this;

        LocalPlayer player =
                minecraft.player;

        if (player == null) {
            return;
        }

        ItemStack weapon =
                player.getMainHandItem();

        if (PrimedStrike.isPrimedStrikeMace(weapon)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "continueAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velocityMace$cancelPrimedStrikeBlockBreaking(
            boolean leftClick,
            CallbackInfo ci
    ) {
        if (!leftClick) {
            return;
        }

        Minecraft minecraft =
                (Minecraft) (Object) this;

        LocalPlayer player =
                minecraft.player;

        if (player == null) {
            return;
        }

        if (PrimedStrike.isPrimedStrikeMace(
                player.getMainHandItem()
        )) {
            ci.cancel();
        }
    }
}