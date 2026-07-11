package com.flowingsun.warresource.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

public class RemoveOreFeaturesBiomeModifier implements BiomeModifier {
    public static final Codec<RemoveOreFeaturesBiomeModifier> CODEC = Codec.unit(RemoveOreFeaturesBiomeModifier::new);

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE) {
            return;
        }

        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            builder.getGenerationSettings().getFeatures(step).removeIf(this::isOreFeature);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WarResourceBiomeModifiers.REMOVE_ORE_FEATURES.get();
    }

    private boolean isOreFeature(Holder<PlacedFeature> placedFeature) {
        return placedFeature.value().getFeatures()
                .anyMatch(this::isOreConfiguredFeature);
    }

    private boolean isOreConfiguredFeature(ConfiguredFeature<?, ?> configuredFeature) {
        if (configuredFeature.feature() != Feature.ORE && configuredFeature.feature() != Feature.SCATTERED_ORE) {
            return false;
        }
        if (!(configuredFeature.config() instanceof OreConfiguration oreConfiguration)) {
            return false;
        }

        return oreConfiguration.targetStates.stream()
                .map(target -> target.state.getBlock())
                .anyMatch(this::isOreBlock);
    }

    private boolean isOreBlock(Block block) {
        if (block.defaultBlockState().is(Tags.Blocks.ORES)) {
            return true;
        }

        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key != null && key.getPath().endsWith("_ore");
    }
}
