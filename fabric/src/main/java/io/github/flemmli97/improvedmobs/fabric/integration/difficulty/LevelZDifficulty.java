package io.github.flemmli97.improvedmobs.fabric.integration.difficulty;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.levelz.access.LevelManagerAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class LevelZDifficulty implements DifficultyGetter {

    @Override
    public double getDifficulty(ServerLevel level, Vec3 pos) {
        return DifficultyGetter.getDifficulty(level, pos, p -> ((LevelManagerAccess) p).getLevelManager().getOverallLevel() * Config.CommonConfig.levelZScale);
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.useLevelZMod;
    }
}
