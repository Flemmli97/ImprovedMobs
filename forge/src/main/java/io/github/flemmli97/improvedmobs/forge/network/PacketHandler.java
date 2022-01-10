package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
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
    }

    public static <T> void sendToClient(T message, ServerPlayer player) {
        if (hasChannel(player))
            dispatcher.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToAll(T message, MinecraftServer server) {
        Packet<?> pkt = dispatcher.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
        server.getPlayerList().getPlayers().forEach(player -> {
            if (hasChannel(player))
                player.connection.send(pkt);
        });
    }

    private static boolean hasChannel(ServerPlayer player) {
        ConnectionData data= NetworkHooks.getConnectionData(player.connection.connection);
        return data != null && data.getChannels().containsKey(channelID);
    }
}
