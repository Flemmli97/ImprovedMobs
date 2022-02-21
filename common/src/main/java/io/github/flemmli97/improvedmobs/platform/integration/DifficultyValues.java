package io.github.flemmli97.improvedmobs.platform.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public abstract class DifficultyValues {

    protected static DifficultyValues INSTANCE;

    public static DifficultyValues instance() {
        return INSTANCE;
    }

    public abstract float getDifficulty(Level level, BlockPos pos, Supplier<Float> defaultVal);
}
