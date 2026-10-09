
package com.gea69.velocitymace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class MeteorImpact {

    public static final ResourceKey<Enchantment> ENCHANTMENT =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "meteor_impact"
                    )
            );

    private MeteorImpact() {
    }

    public static int getLevel(ItemStack stack) {
        if (stack == null
                || stack.isEmpty()
                || !stack.is(MaceTags.MACE_ENCHANTABLE)) {
            return 0;
        }

        return stack.getEnchantments()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey()
                        .unwrapKey()
                        .map(ENCHANTMENT::equals)
                        .orElse(false))
                .mapToInt(entry -> entry.getIntValue())
                .findFirst()
                .orElse(0);
    }

    /**
     * Controls explosion power, including block destruction.
     * This remains independent of entity damage.
     */
    public static float getExplosionPower(double speedBlocksPerSecond) {
        return (float) (
                6.0D * Math.min(
                        Math.max(speedBlocksPerSecond, 0.0D),
                        100.0D
                ) / 100.0D
        );
    }

    /**
     * Entity damage multiplier:
     * 0 b/s = 0%, 50 b/s = 150%, 100+ b/s = 300%.
     */
    public static float getExplosionDamageMultiplier(
            double speedBlocksPerSecond
    ) {
        double cappedSpeed = Math.min(
                Math.max(speedBlocksPerSecond, 0.0D),
                100.0D
        );

        return (float) (cappedSpeed * 0.03D);
    }

    /**
     * Entity damage is based on the primary smash hit before defenses.
     * The resulting damage is then processed through normal defenses.
     */
    public static float getExplosionEntityDamage(
            float preDefenseDamage,
            double speedBlocksPerSecond
    ) {
        if (!Float.isFinite(preDefenseDamage)
                || preDefenseDamage <= 0.0F) {
            return 0.0F;
        }

        return preDefenseDamage
                * getExplosionDamageMultiplier(speedBlocksPerSecond);
    }

    public static double getFireRadius(double speedBlocksPerSecond) {
        return 10.4D
                * Math.min(
                Math.max(speedBlocksPerSecond, 0.0D),
                100.0D
        ) / 100.0D;
    }

    public static int getRawRecoilDamage(
            double maxHealth,
            double speedBlocksPerSecond
    ) {
        if (maxHealth <= 1.0D) {
            return 0;
        }

        int damage = (int) Math.floor(
                maxHealth
                        * Math.min(
                        Math.max(speedBlocksPerSecond, 0.0D),
                        100.0D
                ) / 100.0D
        );

        return Math.max(
                1,
                Math.min(damage, (int) Math.ceil(maxHealth) - 1)
        );
    }

    public static double getMaximumRecoilReduction(int level) {
        return switch (level) {
            case 2 -> 0.25D;
            case 3 -> 0.50D;
            case 4 -> 0.75D;
            case 5 -> 1.00D;
            default -> 0.0D;
        };
    }
}