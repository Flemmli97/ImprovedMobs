package io.github.flemmli97.improvedmobs.common.difficulty;

import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.values.DifficultyExpressionConfig;
import io.github.flemmli97.tenshilib.common.attachment.SerializableAttachment;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class PlayerDifficulty implements SerializableAttachment<CompoundTag, PlayerDifficulty> {

    private double difficultyLevel;
    private int difficultyIndex;

    private boolean paused;

    public PlayerDifficulty() {
    }

    public PlayerDifficulty(PlayerDifficulty other) {
        this.difficultyLevel = other.difficultyLevel;
        this.difficultyIndex = other.difficultyIndex;
        this.paused = other.paused;
    }

    public void setDifficultyLevel(double level) {
        this.difficultyLevel = level;
        this.difficultyIndex = 0;
    }

    public double getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public void increaseCurrent(VariableMap vars) {
        double current = this.difficultyLevel;
        DifficultyExpressionConfig.DifficultyExpression difficulty = Config.CommonConfig.difficultyIncrease.get(current, this.difficultyIndex);
        this.difficultyIndex = difficulty.getIndex();
        this.difficultyLevel = difficulty.get(vars.setVariable("difficulty", current));
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean paused() {
        return this.paused;
    }

    @Override
    public PlayerDifficulty read(CompoundTag tag, HolderLookup.Provider provider) {
        this.difficultyLevel = tag.contains("IMDifficulty") ? tag.getDouble("IMDifficulty") : tag.getDouble("Difficulty");
        this.difficultyIndex = tag.getInt("DifficultyIndex");
        this.paused = tag.getBoolean("Paused");
        return this;
    }

    @Override
    public CompoundTag write(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Difficulty", this.difficultyLevel);
        tag.putInt("DifficultyIndex", this.difficultyIndex);
        tag.putBoolean("Paused", this.paused);
        return tag;
    }
}
