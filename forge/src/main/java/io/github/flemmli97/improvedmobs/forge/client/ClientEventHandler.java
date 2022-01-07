package io.github.flemmli97.improvedmobs.forge.client;

import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEventHandler {

    public static void setup(FMLClientSetupEvent event) {
        if (Config.CommonConfig.enableDifficultyScaling) {
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::showDifficulty);
        }
    }

    public static void showDifficulty(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.TEXT)
            return;
        ClientEvents.showDifficulty(e.getMatrixStack());
    }
}
