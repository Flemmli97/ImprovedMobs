package io.github.flemmli97.improvedmobs.forge.client;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.client.ClientCalls;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandler {

    public static final ResourceLocation overlayID = new ResourceLocation(ImprovedMobs.MODID, "difficulty_overlay");

    public static void setup() {
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(ClientEventHandler::showDifficulty);
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::leave);
    }

    public static void showDifficulty(RegisterGuiOverlaysEvent e) {
        e.registerBelow(VanillaGuiOverlay.EXPERIENCE_BAR.id(), overlayID.getPath(),
                (forgeGui, poseStack, partialTicks, width, length) -> {
                    if (Config.CommonConfig.enableDifficultyScaling)
                        ClientEvents.showDifficulty(poseStack);
                });
    }

    public static void leave(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCalls.disconnect();
    }
}
