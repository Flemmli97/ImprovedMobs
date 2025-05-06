package io.github.flemmli97.improvedmobs.difficulty;

import net.minecraft.nbt.CompoundTag;

public class PlayerDifficulty {

    private float difficultyLevel;

    private boolean paused;

    public void setDifficultyLevel(float level) {
        this.difficultyLevel = level;
    }

    public float getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean paused() {
        return this.paused;
    }

    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.contains("IMDifficulty") ? nbt.getFloat("IMDifficulty") : nbt.getFloat("Difficulty");
        this.paused = nbt.getBoolean("Paused");
    }

    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putBoolean("IMPaused", this.paused);
        return compound;
    }

    public void copyFrom(PlayerDifficulty other) {
        this.difficultyLevel = other.difficultyLevel;
        this.paused = other.paused;
    }
}
