package io.github.flemmli97.improvedmobs.forge.network;

import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketConfig {

    private final boolean scalingHealth;

    private PacketConfig(FriendlyByteBuf buf) {
        this.scalingHealth = buf.readBoolean();
    }

    public PacketConfig() {
        this.scalingHealth = Config.CommonConfig.useScalingHealthMod;
    }

    public static PacketConfig read(FriendlyByteBuf buf) {
        return new PacketConfig(buf);
    }

    public static void write(PacketConfig pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.scalingHealth);
    }

    public static void handle(PacketConfig pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Config.ClientConfig.showDifficultyServerSync = !pkt.scalingHealth);
        ctx.get().setPacketHandled(true);
    }
}
