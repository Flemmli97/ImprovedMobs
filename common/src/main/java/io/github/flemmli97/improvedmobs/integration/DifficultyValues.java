package io.github.flemmli97.improvedmobs.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DifficultyValues {

    @ExpectPlatform
    public static float getDifficulty(Level level, BlockPos pos, float defaultVal) {
        throw new AssertionError();
    }
}
