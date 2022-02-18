package io.github.flemmli97.improvedmobs.forge.platform.integration;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.platform.integration.DifficultyValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public class DifficultyValuesImpl extends DifficultyValues {

    public static void init() {
        INSTANCE = new DifficultyValuesImpl();
    }

    @Override
    public float getDifficulty(Level level, BlockPos pos, float defaultVal) {
        if (Config.CommonConfig.useScalingHealthMod)
            return (float) SHDifficulty.areaDifficulty(level, pos);
        return defaultVal;
    }
}
