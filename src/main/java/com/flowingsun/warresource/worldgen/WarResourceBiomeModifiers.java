package com.flowingsun.warresource.worldgen;

import com.flowingsun.warresource.WarResource;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WarResourceBiomeModifiers {
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, WarResource.MODID);

    public static final RegistryObject<Codec<RemoveOreFeaturesBiomeModifier>> REMOVE_ORE_FEATURES =
            BIOME_MODIFIER_SERIALIZERS.register("remove_ore_features", () -> RemoveOreFeaturesBiomeModifier.CODEC);

    private WarResourceBiomeModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}
