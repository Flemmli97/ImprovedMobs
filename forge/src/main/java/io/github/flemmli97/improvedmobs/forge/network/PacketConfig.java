package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketConfig {

    private final boolean showDifficulty;

    private PacketConfig(FriendlyByteBuf buf) {
        this.showDifficulty = buf.readBoolean();
    }

    public PacketConfig() {
        this.showDifficulty = DifficultyFetcher.shouldClientShowDifficulty();
    }

    public static PacketConfig read(FriendlyByteBuf buf) {
        return new PacketConfig(buf);
    }

    public static void write(PacketConfig pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.showDifficulty);
    }

    public static void handle(PacketConfig pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Config.ClientConfig.showDifficultyServerSync = !pkt.showDifficulty);
        ctx.get().setPacketHandled(true);
    }
}
