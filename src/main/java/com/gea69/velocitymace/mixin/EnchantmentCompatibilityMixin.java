package com.gea69.velocitymace.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentCompatibilityMixin {

    @Inject(
            method = "areCompatible",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void velocityMace$modifyCompatibility(
            Holder<Enchantment> first,
            Holder<Enchantment> second,
            CallbackInfoReturnable<Boolean> cir
    ) {
        /*
         * Density + Breach:
         * explicitly allow.
         */
        if (isPair(
                first,
                second,
                Enchantments.DENSITY,
                Enchantments.BREACH
        )) {
            cir.setReturnValue(true);
            return;
        }

        /*
         * Breach + Wind Burst:
         * explicitly forbid.
         */
        if (isPair(
                first,
                second,
                Enchantments.BREACH,
                Enchantments.WIND_BURST
        )) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isPair(
            Holder<Enchantment> first,
            Holder<Enchantment> second,
            ResourceKey<Enchantment> firstKey,
            ResourceKey<Enchantment> secondKey
    ) {
        return (
                first.is(firstKey)
                        && second.is(secondKey)
        )
                || (
                first.is(secondKey)
                        && second.is(firstKey)
        );
    }
}