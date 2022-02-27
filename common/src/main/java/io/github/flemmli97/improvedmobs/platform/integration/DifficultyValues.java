package io.github.flemmli97.improvedmobs.platform.integration;

import io.github.flemmli97.tenshilib.platform.InitUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public interface DifficultyValues {

    DifficultyValues INSTANCE = InitUtil.getPlatformInstance(DifficultyValues.class,
            "io.github.flemmli97.improvedmobs.fabric.platform.integration.DifficultyValuesImpl",
            "io.github.flemmli97.improvedmobs.forge.platform.integration.DifficultyValuesImpl");

    float getDifficulty(Level level, BlockPos pos, Supplier<Float> defaultVal);
}
