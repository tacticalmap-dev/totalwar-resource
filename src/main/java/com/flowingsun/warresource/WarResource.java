package com.flowingsun.warresource;

import com.flowingsun.warresource.module.WarResourceModule;
import com.flowingsun.warresource.module.ore.OreDropModule;
import com.flowingsun.warresource.worldgen.WarResourceBiomeModifiers;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

@Mod(WarResource.MODID)
public class WarResource {
    public static final String MODID = "warresource";

    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<WarResourceModule> modules;

    public WarResource(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        this.modules = List.of(new OreDropModule());

        WarResourceBiomeModifiers.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modules.forEach(module -> module.registerForgeEventBus(MinecraftForge.EVENT_BUS));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("War Resource loaded");
        modules.forEach(WarResourceModule::onCommonSetup);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        modules.forEach(module -> module.onServerStarting(event.getServer()));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        modules.forEach(module -> module.onServerStopping(event.getServer()));
    }
}
