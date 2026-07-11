package com.flowingsun.warresource.module;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.IEventBus;

public interface WarResourceModule {
    String id();

    default void registerForgeEventBus(IEventBus forgeEventBus) {
    }

    default void onCommonSetup() {
    }

    default void onServerStarting(MinecraftServer server) {
    }

    default void onServerStopping(MinecraftServer server) {
    }
}
