package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final ResourceLocation channelID = new ResourceLocation(ImprovedMobs.MODID, "packets");
    private static final SimpleChannel dispatcher = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ImprovedMobs.MODID, "packets"))
            .clientAcceptedVersions(a -> true)
            .serverAcceptedVersions(a -> true)//(a.equals(NetworkRegistry.ABSENT) || a.equals(NetworkRegistry.ACCEPTVANILLA)) ? false : true)
            .networkProtocolVersion(() -> "v1.0").simpleChannel();

    public static void register() {
        int id = 0;
        dispatcher.registerMessage(id++, PacketDifficulty.class, PacketDifficulty::write, PacketDifficulty::read, PacketDifficulty::handle);
        dispatcher.registerMessage(id++, PacketConfig.class, PacketConfig::write, PacketConfig::read, PacketConfig::handle);
    }

    public static <T> void sendDifficultyToClient(DifficultyData data, ServerPlayer player) {
        if (hasChannel(player))
            dispatcher.sendTo(new PacketDifficulty(Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL ? data.getDifficulty() :
                    CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f)), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendDifficultyToAll(DifficultyData data, MinecraftServer server) {
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL) {
            Packet<?> pkt = dispatcher.toVanillaPacket(new PacketDifficulty(data.getDifficulty()), NetworkDirection.PLAY_TO_CLIENT);
            server.getPlayerList().getPlayers().forEach(player -> {
                if (hasChannel(player))
                    player.connection.send(pkt);
            });
        } else {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (hasChannel(player)) {
                    float diff = Config.CommonConfig.difficultyType.increaseDifficulty ? CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f)
                            : DifficultyData.getDifficultyFromDist(player.getLevel(), player.position());
                    player.connection.send(dispatcher.toVanillaPacket(
                            new PacketDifficulty(diff), NetworkDirection.PLAY_TO_CLIENT));
                }
            });
        }
    }

    public static <T> void sendConfigSync(ServerPlayer player) {
        if (hasChannel(player))
            dispatcher.sendTo(new PacketConfig(), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    private static boolean hasChannel(ServerPlayer player) {
        ConnectionData data = NetworkHooks.getConnectionData(player.connection.connection);
        return data != null && data.getChannels().containsKey(channelID);
    }
}
