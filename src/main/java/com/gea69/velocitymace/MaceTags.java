package com.gea69.velocitymace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class MaceTags {

    public static final TagKey<Item> MACE_ENCHANTABLE =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            "minecraft",
                            "enchantable/mace"
                    )
            );

    private MaceTags() {
    }
}