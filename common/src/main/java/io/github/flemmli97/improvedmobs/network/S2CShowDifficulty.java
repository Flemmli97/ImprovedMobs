package io.github.flemmli97.improvedmobs.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CShowDifficulty(boolean showDifficulty) implements CustomPacketPayload {

    public static final Type<S2CShowDifficulty> TYPE = new Type<>(new ResourceLocation(ImprovedMobs.MODID, "config"));

    public static final StreamCodec<FriendlyByteBuf, S2CShowDifficulty> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CShowDifficulty decode(FriendlyByteBuf buf) {
            return new S2CShowDifficulty(buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, S2CShowDifficulty pkt) {
            buf.writeBoolean(pkt.showDifficulty);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
