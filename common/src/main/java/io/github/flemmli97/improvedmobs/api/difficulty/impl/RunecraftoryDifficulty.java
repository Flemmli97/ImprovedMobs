package io.github.flemmli97.improvedmobs.api.difficulty.impl;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.runecraftory.api.attachment.PlayerAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class RunecraftoryDifficulty implements DifficultyGetter {

    @Override
    public double getDifficulty(ServerLevel level, Vec3 pos) {
        return DifficultyGetter.getDifficulty(level, pos, p -> PlayerAPI.getLevel(p) * Config.CommonConfig.runecraftoryScale);
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.useRunecraftoryMod;
    }
}
