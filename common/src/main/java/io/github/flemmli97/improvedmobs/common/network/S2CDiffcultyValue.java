package io.github.flemmli97.improvedmobs.common.network;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record S2CDiffcultyValue(float difficulty) implements CustomPacketPayload {

    public static final Type<S2CDiffcultyValue> TYPE = new Type<>(ImprovedMobs.modRes("difficulty"));

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

    public static void handle(S2CDiffcultyValue pkt) {
        ClientEvents.updateClientDifficulty(pkt.difficulty());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
