package io.github.flemmli97.improvedmobs.fabric.network;

import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.fabric.ImprovedMobsFabric;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class PacketHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ImprovedMobsFabric.difficultyPacket, PacketHandler::difficultyHandlerPacket);
        ClientPlayNetworking.registerGlobalReceiver(ImprovedMobsFabric.configPacket, PacketHandler::handleConfig);
    }

    private static void difficultyHandlerPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ClientEvents.updateClientDifficulty(buf.readFloat());
    }

    private static void handleConfig(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Config.ClientConfig.showDifficultyServerSync = !buf.readBoolean();
    }
}
