package io.github.flemmli97.improvedmobs.forge.integration.difficulty;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public class ScalingHealthDifficulty implements DifficultyGetter {

    @Override
    public float getDifficulty(ServerLevel level, Vec3 pos) {
        return (float) SHDifficulty.areaDifficulty(level, BlockPos.containing(pos));
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.useScalingHealthMod;
    }

    @Override
    public boolean hasOwnDisplay() {
        return true;
    }
}
