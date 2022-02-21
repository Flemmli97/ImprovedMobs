package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketDifficulty(float difficulty) {

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