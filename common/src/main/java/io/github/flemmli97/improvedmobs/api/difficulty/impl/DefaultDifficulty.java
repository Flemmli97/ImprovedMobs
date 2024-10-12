package io.github.flemmli97.improvedmobs.api.difficulty.impl;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DefaultDifficulty implements DifficultyGetter {

    @Override
    public float getDifficulty(ServerLevel level, Vec3 pos) {
        return switch (Config.CommonConfig.difficultyType) {
            case GLOBAL -> DifficultyData.get(level.getServer()).getDifficulty();
            case PLAYERMAX -> {
                float diff = 0;
                for (Player player : DifficultyGetter.playersIn(level, pos, 256)) {
                    float pD = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).getDifficultyLevel();
                    if (pD > diff)
                        diff = pD;
                }
                yield diff;
            }
            case PLAYERMEAN -> {
                float diff = 0;
                List<Player> list = DifficultyGetter.playersIn(level, pos, 256);
                if (list.isEmpty())
                    yield 0f;
                for (Player player : list) {
                    diff += CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).getDifficultyLevel();
                }
                yield diff / list.size();
            }
            case DISTANCE, DISTANCESPAWN -> DifficultyData.getDifficultyFromDist(level, pos);
        };
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.IntegrationType.ON;
    }
}
