package io.github.flemmli97.improvedmobs.fabric;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.fabric.config.ConfigSpecs;
import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ImprovedMobsFabric implements ModInitializer {

    public static final ResourceLocation difficultyPacket = new ResourceLocation(ImprovedMobs.MODID, "difficulty");
    public static final ResourceLocation configPacket = new ResourceLocation(ImprovedMobs.MODID, "config");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(EventCalls::increaseDifficulty);
        ServerWorldEvents.LOAD.register(EventHandler::worldLoad);
        CommandRegistrationCallback.EVENT.register(EventHandler::registerCommand);
        ServerEntityEvents.ENTITY_LOAD.register(EventHandler::onEntityLoad);
        UseBlockCallback.EVENT.register(EventHandler::openTile);
        UseEntityCallback.EVENT.register(EventHandler::equipPet);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::worldJoin);
        ServerLifecycleEvents.SERVER_STARTING.register(EventHandler::serverStart);

        ConfigSpecs.initCommonConfig();
    }

    public static void sendDifficultyPacket(DifficultyData data, ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, difficultyPacket))
            return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL ? data.getDifficulty() : CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f));
        ServerPlayNetworking.send(player, difficultyPacket, buf);
    }

    public static void sendDifficultyPacketToAll(DifficultyData data, MinecraftServer server) {
        boolean global = Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL;
        FriendlyByteBuf buf = PacketByteBufs.create();
        if (global) {
            buf.writeFloat(data.getDifficulty());
        }
        PlayerLookup.all(server).forEach(player -> {
            if (!global)
                buf.writeFloat(CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f));
            if (ServerPlayNetworking.canSend(player, difficultyPacket))
                ServerPlayNetworking.send(player, difficultyPacket, buf);
        });
    }

    public static void sendConfigCync(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, configPacket))
            return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(Config.CommonConfig.useScalingHealthMod);
        ServerPlayNetworking.send(player, configPacket, buf);
    }
}
