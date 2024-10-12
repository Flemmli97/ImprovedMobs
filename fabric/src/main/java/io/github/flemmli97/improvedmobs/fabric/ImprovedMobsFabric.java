package io.github.flemmli97.improvedmobs.fabric;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.fabric.config.ConfigSpecs;
import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.LevelZDifficulty;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.PlayerEXDifficulty;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ImprovedMobsFabric implements ModInitializer {

    public static final ResourceLocation difficultyPacket = new ResourceLocation(ImprovedMobs.MODID, "difficulty");
    public static final ResourceLocation configPacket = new ResourceLocation(ImprovedMobs.MODID, "config");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(EventCalls::tick);
        ServerWorldEvents.LOAD.register(EventHandler::worldLoad);
        CommandRegistrationCallback.EVENT.register(EventHandler::registerCommand);
        ServerEntityEvents.ENTITY_LOAD.register(EventHandler::onEntityLoad);
        UseBlockCallback.EVENT.register(EventHandler::openTile);
        UseEntityCallback.EVENT.register(EventHandler::equipPet);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::worldJoin);
        ServerLifecycleEvents.SERVER_STARTING.register(EventHandler::serverStart);

        ConfigSpecs.initCommonConfig();
        DifficultyFetcher.register(FabricLoader.getInstance()::isModLoaded);
        if (FabricLoader.getInstance().isModLoaded("playerex"))
            DifficultyFetcher.add(new ResourceLocation(ImprovedMobs.MODID, "player_ex_integration"), new PlayerEXDifficulty());
        if (FabricLoader.getInstance().isModLoaded("levelz"))
            DifficultyFetcher.add(new ResourceLocation(ImprovedMobs.MODID, "level_z_integration"), new LevelZDifficulty());
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
            if (!global) {
                if (Config.CommonConfig.difficultyType.increaseDifficulty)
                    buf.writeFloat(CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f));
                else
                    buf.writeFloat(DifficultyData.getDifficultyFromDist(player.getLevel(), player.position()));
            }
            if (ServerPlayNetworking.canSend(player, difficultyPacket))
                ServerPlayNetworking.send(player, difficultyPacket, buf);
        });
    }

    public static void sendConfigCync(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, configPacket))
            return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(DifficultyFetcher.shouldClientShowDifficulty());
        ServerPlayNetworking.send(player, configPacket, buf);
    }
}
