package com.flowingsun.warresource.module.ore;

import com.flowingsun.warresource.module.WarResourceModule;
import com.flowingsun.warresource.module.ore.config.OreDropConfig;
import com.flowingsun.warresource.module.ore.config.OreDropConfigLoader;
import com.flowingsun.warresource.module.ore.drop.OreDropHandler;
import com.flowingsun.warresource.module.ore.town.WarTownLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.IEventBus;

public class OreDropModule implements WarResourceModule {
    private final OreDropHandler dropHandler = new OreDropHandler(new WarTownLookup());

    @Override
    public String id() {
        return "ore";
    }

    @Override
    public void registerForgeEventBus(IEventBus forgeEventBus) {
        forgeEventBus.register(dropHandler);
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        dropHandler.updateConfig(OreDropConfigLoader.load());
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        dropHandler.updateConfig(OreDropConfig.empty());
    }
}
