package com.flowingsun.warresource.module.ore.drop;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.Optional;

final class OreDropTarget {
    private OreDropTarget() {
    }

    static Optional<Item> resolve(String oreName, RandomSource random) {
        Optional<Item> directItem = resolveDirectItem(oreName);
        if (directItem.isPresent()) {
            return directItem;
        }

        ResourceLocation tagLocation = parseTagLocation(oreName);
        if (tagLocation == null) {
            return Optional.empty();
        }

        TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, tagLocation);
        ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(itemTagKey);
        Optional<Item> item = itemTag.getRandomElement(random);
        if (item.isPresent()) {
            return item;
        }

        TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, tagLocation);
        ITag<Block> blockTag = ForgeRegistries.BLOCKS.tags().getTag(blockTagKey);
        return blockTag.getRandomElement(random)
                .map(Block::asItem)
                .filter(candidate -> candidate != null && candidate != net.minecraft.world.item.Items.AIR);
    }

    private static Optional<Item> resolveDirectItem(String oreName) {
        ResourceLocation location = parseDirectLocation(oreName);
        if (location == null) {
            return Optional.empty();
        }

        if (ForgeRegistries.ITEMS.containsKey(location)) {
            return Optional.of(ForgeRegistries.ITEMS.getValue(location));
        }
        if (ForgeRegistries.BLOCKS.containsKey(location)) {
            Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block != null && block.asItem() != net.minecraft.world.item.Items.AIR) {
                return Optional.of(block.asItem());
            }
        }
        return Optional.empty();
    }

    private static ResourceLocation parseDirectLocation(String oreName) {
        String value = oreName.trim();
        if (value.contains(":")) {
            return ResourceLocation.tryParse(value);
        }
        return ResourceLocation.tryParse("minecraft:" + value);
    }

    private static ResourceLocation parseTagLocation(String oreName) {
        String value = oreName.trim();
        if (value.contains(":")) {
            return ResourceLocation.tryParse(value);
        }
        return ResourceLocation.tryParse("forge:" + value);
    }
}
