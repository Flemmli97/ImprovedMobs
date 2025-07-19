package io.github.flemmli97.improvedmobs.fabric;

import io.github.flemmli97.improvedmobs.client.ClientCalls;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ImprovedMobsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> ClientEvents.showDifficulty(matrixStack));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> ClientCalls.disconnect()));
    }
}
