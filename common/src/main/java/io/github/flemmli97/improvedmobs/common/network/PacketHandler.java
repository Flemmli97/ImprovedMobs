package io.github.flemmli97.improvedmobs.common.network;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import net.minecraft.server.level.ServerPlayer;

public class PacketHandler {

    public static S2CShowDifficulty createConfigPacket() {
        return new S2CShowDifficulty(DifficultyFetcher.shouldClientShowDifficulty());
    }

    public static S2CDiffcultyValue createDifficultyPacket(DifficultyData data, ServerPlayer player) {
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.GLOBAL) {
            return new S2CDiffcultyValue(data.getDifficulty());
        } else {
            return new S2CDiffcultyValue(Config.CommonConfig.difficultyType.increaseDifficulty ? ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).getDifficultyLevel()
                    : DifficultyData.getDifficultyFromDist(player.serverLevel(), player.position()));
        }
    }
}
