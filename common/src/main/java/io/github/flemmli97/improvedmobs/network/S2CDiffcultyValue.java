package io.github.flemmli97.improvedmobs.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CDiffcultyValue(float difficulty) implements CustomPacketPayload {

    public static final Type<S2CDiffcultyValue> TYPE = new Type<>(new ResourceLocation(ImprovedMobs.MODID, "difficulty"));

    public static final StreamCodec<FriendlyByteBuf, S2CDiffcultyValue> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CDiffcultyValue decode(FriendlyByteBuf buf) {
            return new S2CDiffcultyValue(buf.readFloat());
        }

        @Override
        public void encode(FriendlyByteBuf buf, S2CDiffcultyValue pkt) {
            buf.writeFloat(pkt.difficulty);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
