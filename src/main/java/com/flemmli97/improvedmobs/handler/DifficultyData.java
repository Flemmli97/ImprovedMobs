package com.flemmli97.improvedmobs.handler;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.packet.PacketDifficulty;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.silentchaos512.scalinghealth.config.Config;

public class DifficultyData extends WorldSavedData {

	private static String identifier = "Difficulty";
	private float difficultyLevel;
	private long prevTime;

	public DifficultyData() {
		this(identifier);
	}

	public DifficultyData(String name) {
		super(name);
	}

	public static DifficultyData get(World world) {
		MapStorage storage = world.getMapStorage();
		DifficultyData data = (DifficultyData) storage.getOrLoadData(DifficultyData.class, identifier);
		if(data == null){
			data = new DifficultyData();
			storage.setData(identifier, data);
		}
		return data;
	}

	public static float getDifficulty(World world, EntityLiving e) {
		if(ConfigHandler.useScalingHealthMod)
			return (float) Config.Difficulty.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, e.getPosition());
		return DifficultyData.get(e.world).getDifficulty();
	}

	public void increaseDifficultyBy(float amount, long time) {
		this.difficultyLevel += amount;
		this.prevTime = time;
		this.markDirty();
		PacketHandler.sendToAll(new PacketDifficulty(this));
	}

	public void setDifficulty(float level) {
		this.difficultyLevel = level;
		this.markDirty();
	}

	public void addDifficulty(float level) {
		this.difficultyLevel += level;
		this.markDirty();
	}

	public float getDifficulty() {
		return this.difficultyLevel;
	}

	public long getPrevTime() {
		return this.prevTime;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.difficultyLevel = nbt.getFloat("Difficulty");
		this.prevTime = nbt.getLong("Time");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setFloat("Difficulty", this.difficultyLevel);
		compound.setLong("Time", this.prevTime);
		return compound;
	}

}
