package com.flemmli97.improvedmobs;

import com.flemmli97.improvedmobs.capability.ITileOpened;
import com.flemmli97.improvedmobs.capability.TileCap;
import com.flemmli97.improvedmobs.capability.TileCapNetwork;
import com.flemmli97.improvedmobs.client.DifficultyDisplay;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.config.ConfigSpecs;
import com.flemmli97.improvedmobs.events.DifficultyHandler;
import com.flemmli97.improvedmobs.events.EventHandler;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = ImprovedMobs.MODID)
public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final Logger logger = LogManager.getLogger(ImprovedMobs.MODID);

    public static final String thrownEntityID = MODID + ":thrown_entity";
    public static final String ridingGuardian = MODID + ":riding_guardian";

    public ImprovedMobs() {
        File file = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").toFile();
        if(!file.exists())
            file.mkdir();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSpecs.clientSpec, "improvedmobs/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSpecs.commonSpec, "improvedmobs/common.toml");
    }

    @SubscribeEvent
    static void setup(FMLCommonSetupEvent event){
        PacketHandler.register();
        CapabilityManager.INSTANCE.register(ITileOpened.class, new TileCapNetwork(), TileCap::new);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        if(Config.CommonConfig.enableDifficultyScaling) {
            MinecraftForge.EVENT_BUS.register(DifficultyDisplay.class);
            MinecraftForge.EVENT_BUS.register(DifficultyHandler.class);
        }
    }

    @SubscribeEvent
    static void conf(ModConfig.ModConfigEvent event){
        if(event.getConfig().getSpec() == ConfigSpecs.clientSpec)
            Config.ClientConfig.load();
        else if(event.getConfig().getSpec() == ConfigSpecs.commonSpec)
            Config.CommonConfig.load();
    }
}
