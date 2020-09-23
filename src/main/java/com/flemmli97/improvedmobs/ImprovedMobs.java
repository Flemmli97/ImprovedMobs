package com.flemmli97.improvedmobs;

import com.flemmli97.improvedmobs.capability.ITileOpened;
import com.flemmli97.improvedmobs.capability.TileCap;
import com.flemmli97.improvedmobs.capability.TileCapNetwork;
import com.flemmli97.improvedmobs.client.DifficultyDisplay;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.events.DifficultyHandler;
import com.flemmli97.improvedmobs.events.EventHandler;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = ImprovedMobs.MODID)
public class ImprovedMobs {

    public static final String MODID = "improvedmobs";
    public static final Logger logger = LogManager.getLogger(ImprovedMobs.MODID);

    public static final String thrownEntityID = MODID + ":thrown_entity";
    public static final String ridingGuardian = MODID + ":riding_guardian";

    public ImprovedMobs() {
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
    }

    @SubscribeEvent
    static void setup(FMLCommonSetupEvent event){
        PacketHandler.register();
        CapabilityManager.INSTANCE.register(ITileOpened.class, new TileCapNetwork(), TileCap::new);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        if(Config.commonConf.enableDifficultyScaling) {
            MinecraftForge.EVENT_BUS.register(DifficultyDisplay.class);
            MinecraftForge.EVENT_BUS.register(DifficultyHandler.class);
        }

        Config.commonConf.useScalingHealthMod = Config.commonConf.useScalingHealthMod && ModList.get().isLoaded("scalinghealth");
        Config.commonConf.useTGunsMod = Config.commonConf.useTGunsMod && ModList.get().isLoaded("techguns");
        Config.commonConf.useReforgedMod = Config.commonConf.useReforgedMod && ModList.get().isLoaded("reforged");
        Config.commonConf.useCoroUtil = Config.commonConf.useCoroUtil && ModList.get().isLoaded("coroutil");
    }
}
