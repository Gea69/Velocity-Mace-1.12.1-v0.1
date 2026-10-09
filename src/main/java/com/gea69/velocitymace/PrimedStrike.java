package com.gea69.velocitymace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class PrimedStrike {

    public static final ResourceKey<Enchantment> ENCHANTMENT =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "primed_strike"
                    )
            );

    private PrimedStrike() {
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

    public static boolean isPrimedStrikeMace(ItemStack stack) {
        return getLevel(stack) > 0;
    }

    public static double getAdditionalRange(int level) {
        if (level <= 1) {
            return 0.0D;
        }

        return 0.5D * (level - 1);
    }

    public static boolean canAttackTarget(
            Player player,
            Entity target,
            int level
    ) {
        if (player == null || target == null) {
            return false;
        }

        if (level <= 0) {
            return false;
        }

        if (target == player) {
            return false;
        }

        if (target.isRemoved()) {
            return false;
        }

        if (!target.isPickable()) {
            return false;
        }

        if (!target.isAttackable()) {
            return false;
        }

        if (target.skipAttackInteraction(player)) {
            return false;
        }

        return player.canInteractWithEntity(
                target,
                getAdditionalRange(level)
        );
    }
}