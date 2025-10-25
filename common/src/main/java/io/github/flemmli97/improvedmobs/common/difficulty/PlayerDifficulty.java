package io.github.flemmli97.improvedmobs.common.difficulty;

import io.github.flemmli97.tenshilib.common.attachment.SerializableAttachment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class PlayerDifficulty implements SerializableAttachment<CompoundTag, PlayerDifficulty> {

    private float difficultyLevel;

    private boolean paused;

    public PlayerDifficulty() {
    }

    public PlayerDifficulty(PlayerDifficulty other) {
        this.difficultyLevel = other.difficultyLevel;
        this.paused = other.paused;
    }

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

    @Override
    public PlayerDifficulty read(CompoundTag tag, HolderLookup.Provider provider) {
        this.difficultyLevel = tag.contains("IMDifficulty") ? tag.getFloat("IMDifficulty") : tag.getFloat("Difficulty");
        this.paused = tag.getBoolean("Paused");
        return this;
    }

    @Override
    public CompoundTag write(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Difficulty", this.difficultyLevel);
        tag.putBoolean("Paused", this.paused);
        return tag;
    }
}
