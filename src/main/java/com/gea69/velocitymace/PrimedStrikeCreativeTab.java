package com.gea69.velocitymace;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(
        modid = "velocitymace",
        bus = EventBusSubscriber.Bus.MOD
)
public final class PrimedStrikeCreativeTab {

    private PrimedStrikeCreativeTab() {
    }

    @SubscribeEvent
    public static void buildContents(
            BuildCreativeModeTabContentsEvent event
    ) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) {
            return;
        }

        Holder<Enchantment> primedStrike =
                event.getParameters()
                        .holders()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(PrimedStrike.ENCHANTMENT);

        for (int level = 1; level <= 5; level++) {
            event.accept(
                    createBook(
                            primedStrike,
                            level
                    )
            );
        }
    }

    private static ItemStack createBook(
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
                net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS,
                enchantments.toImmutable()
        );

        return book;
    }
}