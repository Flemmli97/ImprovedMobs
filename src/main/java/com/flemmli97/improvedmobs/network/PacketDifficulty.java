package com.flemmli97.improvedmobs.network;

import com.flemmli97.improvedmobs.client.DifficultyDisplay;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDifficulty {

    public float difficulty;

    private PacketDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public PacketDifficulty(DifficultyData data) {
        this.difficulty = data.getDifficulty();
    }

    public static PacketDifficulty read(PacketBuffer buf) {
        return new PacketDifficulty(buf.readFloat());
    }

    public static void write(PacketDifficulty pkt, PacketBuffer buf) {
        buf.writeFloat(pkt.difficulty);
    }

    public static void handle(PacketDifficulty pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DifficultyDisplay.updateClientDifficulty(pkt.difficulty));
        ctx.get().setPacketHandled(true);
    }
}