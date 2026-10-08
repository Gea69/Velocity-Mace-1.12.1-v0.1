package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.PrimedStrike;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.item.CreativeModeTabs")
public abstract class CreativeModeTabsMixin {

    @Inject(
            method = "generateEnchantmentBookTypesOnlyMaxLevel",
            at = @At("TAIL")
    )
    private static void velocityMace$addPrimedStrikeMaxLevel(
            CreativeModeTab.Output output,
            HolderLookup<Enchantment> enchantments,
            CreativeModeTab.TabVisibility tabVisibility,
            CallbackInfo ci
    ) {
        Holder<Enchantment> primedStrike =
                enchantments.getOrThrow(
                        PrimedStrike.ENCHANTMENT
                );

        int maxLevel =
                primedStrike.value().getMaxLevel();

        output.accept(
                velocityMace$createBook(
                        primedStrike,
                        maxLevel
                ),
                tabVisibility
        );
    }

    @Inject(
            method = "generateEnchantmentBookTypesAllLevels",
            at = @At("TAIL")
    )
    private static void velocityMace$addPrimedStrikeAllLevels(
            CreativeModeTab.Output output,
            HolderLookup<Enchantment> enchantments,
            CreativeModeTab.TabVisibility tabVisibility,
            CallbackInfo ci
    ) {
        Holder<Enchantment> primedStrike =
                enchantments.getOrThrow(
                        PrimedStrike.ENCHANTMENT
                );

        int maxLevel =
                primedStrike.value().getMaxLevel();

        for (int level = 1; level <= maxLevel; level++) {
            output.accept(
                    velocityMace$createBook(
                            primedStrike,
                            level
                    ),
                    tabVisibility
            );
        }
    }

    private static ItemStack velocityMace$createBook(
            Holder<Enchantment> enchantment,
            int level
    ) {
        ItemStack book =
                new ItemStack(Items.ENCHANTED_BOOK);

        ItemEnchantments.Mutable enchantments =
                new ItemEnchantments.Mutable(
                        ItemEnchantments.EMPTY
                );

        enchantments.set(
                enchantment,
                level
        );

        book.set(
                DataComponents.STORED_ENCHANTMENTS,
                enchantments.toImmutable()
        );

        return book;
    }
}