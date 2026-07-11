package com.flowingsun.warresource.tag;

import com.flowingsun.warresource.WarResource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class WarResourceTags {
    private WarResourceTags() {
    }

    public static final class Blocks {
        public static final TagKey<Block> ORE_SOURCE_STONES = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(WarResource.MODID, "ore_source_stones")
        );

        private Blocks() {
        }
    }
}
