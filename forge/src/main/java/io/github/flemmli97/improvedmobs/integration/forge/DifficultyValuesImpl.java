package io.github.flemmli97.improvedmobs.integration.forge;

import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public class DifficultyValuesImpl {

    public static float getDifficulty(Level level, BlockPos pos, float defaultVal) {
        if (Config.CommonConfig.useScalingHealthMod)
            return (float) SHDifficulty.areaDifficulty(level, pos);
        return defaultVal;
    }
}
