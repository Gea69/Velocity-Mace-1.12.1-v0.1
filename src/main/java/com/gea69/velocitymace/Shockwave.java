package com.gea69.velocitymace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class Shockwave {

    public static final ResourceKey<Enchantment> ENCHANTMENT =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "shockwave"
                    )
            );

    private Shockwave() {
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

    public static float getDamageMultiplier(int level) {
        return switch (level) {
            case 1 -> 0.25F;
            case 2 -> 0.50F;
            case 3 -> 0.75F;
            case 4 -> 1.00F;
            default -> 0.0F;
        };
    }
}