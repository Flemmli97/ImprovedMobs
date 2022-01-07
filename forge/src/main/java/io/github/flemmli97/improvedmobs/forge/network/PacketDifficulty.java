package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDifficulty {

    private final float difficulty;

    private PacketDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public PacketDifficulty(DifficultyData data) {
        this.difficulty = data.getDifficulty();
    }

    public static PacketDifficulty read(FriendlyByteBuf buf) {
        return new PacketDifficulty(buf.readFloat());
    }

    public static void write(PacketDifficulty pkt, FriendlyByteBuf buf) {
        buf.writeFloat(pkt.difficulty);
    }

    public static void handle(PacketDifficulty pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientEvents.updateClientDifficulty(pkt.difficulty));
        ctx.get().setPacketHandled(true);
    }
}