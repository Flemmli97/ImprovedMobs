package io.github.flemmli97.improvedmobs.integration.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DifficultyValuesImpl {

    public static float getDifficulty(Level level, BlockPos pos, float defaultVal) {
        //if (Config.CommonConfig.useScalingHealthMod)
        //    return (float) SHDifficulty.areaDifficulty(level, pos);
        return defaultVal;
    }
}
