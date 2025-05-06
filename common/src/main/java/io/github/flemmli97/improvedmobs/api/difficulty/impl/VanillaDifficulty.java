package io.github.flemmli97.improvedmobs.api.difficulty.impl;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class VanillaDifficulty implements DifficultyGetter {

    @Override
    public float getDifficulty(ServerLevel level, Vec3 pos) {
        return level.getCurrentDifficultyAt(BlockPos.containing(pos)).getSpecialMultiplier() * Config.CommonConfig.vanillaClampedMax;
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.vanillaClamped;
    }
}
