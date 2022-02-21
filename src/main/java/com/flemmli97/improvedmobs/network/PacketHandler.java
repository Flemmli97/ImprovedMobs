package com.flemmli97.improvedmobs.network;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLConnectionData;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static final ResourceLocation channelID = new ResourceLocation(ImprovedMobs.MODID, "packets");
    private static final SimpleChannel dispatcher = NetworkRegistry.ChannelBuilder.named(channelID)
            .clientAcceptedVersions(a -> true)
            .serverAcceptedVersions(a -> true)//(a.equals(NetworkRegistry.ABSENT) || a.equals(NetworkRegistry.ACCEPTVANILLA)) ? false : true)
            .networkProtocolVersion(() -> "v1.0").simpleChannel();

    public static void register() {
        int id = 0;
        dispatcher.registerMessage(id++, PacketDifficulty.class, PacketDifficulty::write, PacketDifficulty::read, PacketDifficulty::handle);
    }

    public static <T> void sendDifficultyToClient(DifficultyData data, ServerPlayerEntity player) {
        if (hasChannel(player))
            dispatcher.sendTo(new PacketDifficulty(Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL ? data.getDifficulty() :
                    TileCapProvider.getPlayerDifficultyData(player).getDifficultyLevel()), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendDifficultyToAll(DifficultyData data, MinecraftServer server) {
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL) {
            IPacket<?> pkt = dispatcher.toVanillaPacket(new PacketDifficulty(data.getDifficulty()), NetworkDirection.PLAY_TO_CLIENT);
            server.getPlayerList().getPlayers().forEach(player -> {
                if (hasChannel(player))
                    player.connection.sendPacket(pkt);
            });
        } else {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (hasChannel(player))
                    player.connection.sendPacket(dispatcher.toVanillaPacket(
                            new PacketDifficulty(TileCapProvider.getPlayerDifficultyData(player).getDifficultyLevel()), NetworkDirection.PLAY_TO_CLIENT));
            });
        }
    }

    private static boolean hasChannel(ServerPlayerEntity player) {
        FMLConnectionData data = NetworkHooks.getConnectionData(player.connection.getNetworkManager());
        return data != null && data.getChannels().containsKey(channelID);
    }
}
