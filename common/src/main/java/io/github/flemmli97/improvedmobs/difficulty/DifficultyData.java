package io.github.flemmli97.improvedmobs.difficulty;

import io.github.flemmli97.improvedmobs.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.integration.DifficultyValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class DifficultyData extends SavedData {

    private static final String identifier = "Difficulty";
    private float difficultyLevel;
    private long prevTime;

    public DifficultyData() {
    }

    private DifficultyData(CompoundTag tag) {
        this.load(tag);
    }

    public static DifficultyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(DifficultyData::new, DifficultyData::new, identifier);
    }

    public static float getDifficulty(Level world, LivingEntity e) {
        return DifficultyValues.getDifficulty(world, e.blockPosition(), DifficultyData.get(world.getServer()).getDifficulty());
    }

    public void increaseDifficultyBy(float amount, long time, MinecraftServer server) {
        this.difficultyLevel += amount;
        this.prevTime = time;
        this.setDirty();
        CrossPlatformStuff.sendDifficultyData(this, server);
    }

    public void setDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel = level;
        CrossPlatformStuff.sendDifficultyData(this, server);
        this.setDirty();
    }

    public void addDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel += level;
        CrossPlatformStuff.sendDifficultyData(this, server);
        this.setDirty();
    }

    public float getDifficulty() {
        return this.difficultyLevel;
    }

    public long getPrevTime() {
        return this.prevTime;
    }

    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        return compound;
    }
}
