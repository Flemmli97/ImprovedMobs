package com.flemmli97.improvedmobs.difficulty;

import com.flemmli97.improvedmobs.network.PacketDifficulty;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

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
        return world.getServer().getOverworld().getSavedData().getOrCreate(DifficultyData::new, identifier);
    }

    public static float getDifficulty(World world, LivingEntity e) {
        //if(ConfigHandler.useScalingHealthMod)
        //    return (float) Config.Difficulty.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, e.getPosition());
        return DifficultyData.get(world).getDifficulty();
    }


    public void increaseDifficultyBy(ServerWorld world, float amount, long time) {
        this.difficultyLevel += amount;
        this.prevTime = time;
        this.markDirty();
        PacketHandler.sendToAll(new PacketDifficulty(this), world.getServer());
    }

    public void setDifficulty(float level, ServerWorld world) {
        this.difficultyLevel = level;
        PacketHandler.sendToAll(new PacketDifficulty(this), world.getServer());
        this.markDirty();
    }

    public void addDifficulty(float level, ServerWorld world) {
        this.difficultyLevel += level;
        PacketHandler.sendToAll(new PacketDifficulty(this), world.getServer());
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
