package com.gea69.velocitymace.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "getDamageAfterArmorAbsorb",
            at = @At("RETURN"),
            cancellable = true
    )
    private void velocityMace$applyBreachLevelFourBonus(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Float> cir
    ) {
        ItemStack weapon =
                source.getWeaponItem();

        if (weapon == null || weapon.isEmpty()) {
            return;
        }

        int breachLevel =
                velocityMace$getBreachLevel(weapon);

        if (breachLevel < 4) {
            return;
        }

        LivingEntity target =
                (LivingEntity) (Object) this;

        double armor =
                target.getArmorValue();

        double toughness =
                target.getAttributeValue(
                        Attributes.ARMOR_TOUGHNESS
                );

        /*
         * Bonus percentage:
         *
         * armor * 2 - toughness
         *
         * Deliberately not clamped.
         */
        double bonusPercent =
                armor * 2.0D - toughness;

        double damageMultiplier =
                1.0D
                        + bonusPercent / 100.0D;

        float modifiedDamage =
                (float) (
                        cir.getReturnValue()
                                * damageMultiplier
                );

        cir.setReturnValue(
                modifiedDamage
        );
    }

    private static int velocityMace$getBreachLevel(
            ItemStack weapon
    ) {
        ItemEnchantments enchantments =
                weapon.getEnchantments();

        for (
                var entry :
                enchantments.entrySet()
        ) {
            Holder<Enchantment> enchantment =
                    entry.getKey();

            if (enchantment.is(
                    Enchantments.BREACH
            )) {
                return entry.getIntValue();
            }
        }

        return 0;
    }
}