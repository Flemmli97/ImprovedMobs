package com.flemmli97.improvedmobs.difficulty;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.network.PacketDifficulty;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public class DifficultyData extends WorldSavedData {

    private static final String identifier = "Difficulty";
    private float difficultyLevel;
    private long prevTime;

    public DifficultyData() {
        this(identifier);
    }

    private DifficultyData(String id) {
        super(id);
    }

    public static DifficultyData get(World world) {
        return world.getServer().func_241755_D_().getSavedData().getOrCreate(DifficultyData::new, identifier);
    }

    public static float getDifficulty(World world, LivingEntity e) {
        if (Config.CommonConfig.useScalingHealthMod)
            return (float) SHDifficulty.areaDifficulty(world, e.getPosition());
        return DifficultyData.get(world).getDifficulty();
    }

    public void increaseDifficultyBy(float amount, long time, MinecraftServer server) {
        this.difficultyLevel += amount;
        this.prevTime = time;
        this.markDirty();
        PacketHandler.sendToAll(new PacketDifficulty(this), server);
    }

    public void setDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel = level;
        PacketHandler.sendToAll(new PacketDifficulty(this), server);
        this.markDirty();
    }

    public void addDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel += level;
        PacketHandler.sendToAll(new PacketDifficulty(this), server);
        this.markDirty();
    }

    public float getDifficulty() {
        return this.difficultyLevel;
    }

    public long getPrevTime() {
        return this.prevTime;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        return compound;
    }
}
