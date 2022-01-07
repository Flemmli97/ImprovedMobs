package io.github.flemmli97.improvedmobs.fabric.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketHandler {

    public static final ResourceLocation difficultyPacket = new ResourceLocation(ImprovedMobs.MODID, "difficulty");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.difficultyPacket, PacketHandler::difficultyHandlerPacket);
    }

    private static void difficultyHandlerPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ClientEvents.updateClientDifficulty(buf.readFloat());
    }

    public static void sendDifficultyPacket(DifficultyData data, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(data.getDifficulty());
        ServerPlayNetworking.send(player, PacketHandler.difficultyPacket, buf);
    }

    public static void sendDifficultyPacketToAll(DifficultyData data, MinecraftServer server) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(data.getDifficulty());
        PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.send(player, PacketHandler.difficultyPacket, buf));
    }
}
