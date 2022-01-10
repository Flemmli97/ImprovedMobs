package io.github.flemmli97.improvedmobs.fabric.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler {

    public static final ResourceLocation difficultyPacket = new ResourceLocation(ImprovedMobs.MODID, "difficulty");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.difficultyPacket, PacketHandler::difficultyHandlerPacket);
    }

    private static void difficultyHandlerPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ClientEvents.updateClientDifficulty(buf.readFloat());
    }
}
