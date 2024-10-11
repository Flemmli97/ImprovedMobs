package io.github.flemmli97.improvedmobs.fabric.integration.difficulty;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.config.Config;
import net.levelz.access.PlayerStatsManagerAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class LevelZDifficulty implements DifficultyGetter {

    @Override
    public float getDifficulty(ServerLevel level, Vec3 pos) {
        return DifficultyGetter.getDifficulty(level, pos, p -> (float) ((PlayerStatsManagerAccess) p).getPlayerStatsManager().overallLevel * Config.CommonConfig.levelZScale);
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.useLevelZMod;
    }
}
