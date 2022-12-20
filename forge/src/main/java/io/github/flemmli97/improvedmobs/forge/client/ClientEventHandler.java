package io.github.flemmli97.improvedmobs.forge.client;

import io.github.flemmli97.improvedmobs.client.ClientCalls;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEventHandler {

    public static void setup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::showDifficulty);
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::leave);
    }

    public static void showDifficulty(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.TEXT)
            return;
        ClientEvents.showDifficulty(e.getMatrixStack());
    }

    public static void leave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientCalls.disconnect();
    }
}
