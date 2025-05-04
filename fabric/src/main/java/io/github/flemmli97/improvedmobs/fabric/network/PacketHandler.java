package io.github.flemmli97.improvedmobs.fabric.network;

import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.network.S2CShowDifficulty;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class PacketHandler {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(S2CDiffcultyValue.TYPE, S2CDiffcultyValue.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CShowDifficulty.TYPE, S2CShowDifficulty.STREAM_CODEC);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(S2CDiffcultyValue.TYPE, PacketHandler::difficultyHandlerPacket);
            ClientPlayNetworking.registerGlobalReceiver(S2CShowDifficulty.TYPE, PacketHandler::handleConfig);
        }
    }

    public static void difficultyHandlerPacket(S2CDiffcultyValue pkt, ClientPlayNetworking.Context ctx) {
        ClientEvents.updateClientDifficulty(pkt.difficulty());
    }

    public static void handleConfig(S2CShowDifficulty pkt, ClientPlayNetworking.Context ctx) {
        Config.ClientConfig.showDifficultyServerSync = pkt.showDifficulty();
    }
}
