package com.flemmli97.improvedmobs.client;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ImprovedMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventHandler {

    @SubscribeEvent
    static void setup(FMLClientSetupEvent event) {
        if (Config.CommonConfig.enableDifficultyScaling) {
            MinecraftForge.EVENT_BUS.register(new DifficultyDisplay());
        }
    }
}
