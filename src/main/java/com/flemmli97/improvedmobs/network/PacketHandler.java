package com.flemmli97.improvedmobs.network;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLConnectionData;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
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

    public static <T> void sendToClient(T message, ServerPlayerEntity player) {
        if(hasChannel(player))
            dispatcher.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToAll(T message, MinecraftServer server) {
        IPacket<?> pkt = dispatcher.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
        server.getPlayerList().getPlayers().forEach(player -> {
            if (hasChannel(player))
                player.connection.sendPacket(pkt);
        });
        dispatcher.send(PacketDistributor.ALL.noArg(), message);
    }

    private static boolean hasChannel(ServerPlayerEntity player) {
        FMLConnectionData data= NetworkHooks.getConnectionData(player.connection.getNetworkManager());
        return data != null && data.getChannels().containsKey(channelID);
    }
}
