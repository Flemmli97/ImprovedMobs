package io.github.flemmli97.improvedmobs.fabric.platform.integration;

import io.github.flemmli97.improvedmobs.platform.integration.DifficultyValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class DifficultyValuesImpl implements DifficultyValues {

    @Override
    public float getDifficulty(Level level, BlockPos pos, Supplier<Float> defaultVal) {
        return defaultVal.get();
    }
}
