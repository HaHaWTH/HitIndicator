package com.rosymaple.hitindication;

import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HitIndication.MODID)
public class HitIndication
{
    public static final String MODID = "hitindication";
    private static final Logger LOGGER = LogManager.getLogger();

    public HitIndication() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HitIndicatorClientConfigs.SPEC, "hitindication-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModPackets.register();

        LatestHitsProvider.register();
    }

}
