package io.github.flemmli97.improvedmobs.difficulty;

import net.minecraft.nbt.CompoundTag;

public interface IPlayerDifficulty {

    void setDifficultyLevel(float level);

    float getDifficultyLevel();

    void load(CompoundTag nbt);

    CompoundTag save(CompoundTag compound);
}
