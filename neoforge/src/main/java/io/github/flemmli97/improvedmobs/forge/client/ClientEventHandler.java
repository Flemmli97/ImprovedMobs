package io.github.flemmli97.improvedmobs.forge.client;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.client.ClientCalls;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

public class ClientEventHandler {

    public static final ResourceLocation OVERLAY_ID = ImprovedMobs.modRes("difficulty_overlay");

    public static void setup(IEventBus modBus) {
        modBus.addListener(ClientEventHandler::showDifficulty);
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::leave);
    }

    public static void showDifficulty(RegisterGuiLayersEvent e) {
        e.registerBelow(VanillaGuiLayers.EXPERIENCE_BAR, OVERLAY_ID,
                (graphics, partialTicks) -> ClientEvents.showDifficulty(graphics));
    }

    public static void leave(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCalls.disconnect();
    }
}
