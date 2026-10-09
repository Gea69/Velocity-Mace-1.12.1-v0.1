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

    public static float getExplosionPower(double speedBlocksPerSecond) {
        return (float) (
                6.0D * Math.min(speedBlocksPerSecond, 100.0D) / 100.0D
        );
    }

    public static double getFireRadius(double speedBlocksPerSecond) {
        return 10.4D
                * Math.min(speedBlocksPerSecond, 100.0D)
                / 100.0D;
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
                        * Math.min(speedBlocksPerSecond, 100.0D)
                        / 100.0D
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