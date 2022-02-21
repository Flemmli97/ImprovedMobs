package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerDifficultyData implements ICapabilitySerializable<CompoundNBT> {

    private final LazyOptional<PlayerDifficultyData> holder = LazyOptional.of(() -> this);

    private float difficultyLevel;

    public void setDifficultyLevel(float level) {
        this.difficultyLevel = level;
    }

    public float getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public void load(CompoundNBT nbt) {
        this.difficultyLevel = nbt.getFloat("IMDifficulty");
    }

    public CompoundNBT save(CompoundNBT compound) {
        compound.putFloat("IMDifficulty", this.difficultyLevel);
        return compound;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction arg) {
        return TileCapProvider.PlayerCap.orEmpty(capability, this.holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT arg) {
        this.load(arg);
    }
}
