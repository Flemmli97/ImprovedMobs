package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDifficultyData implements IPlayerDifficulty, ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<IPlayerDifficulty> holder = LazyOptional.of(() -> this);

    private float difficultyLevel;

    @Override
    public void setDifficultyLevel(float level) {
        this.difficultyLevel = level;
    }

    @Override
    public float getDifficultyLevel() {
        return this.difficultyLevel;
    }

    @Override
    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("IMDifficulty");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("IMDifficulty", this.difficultyLevel);
        return compound;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        return TileCapProvider.PLAYER_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.save(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.load(arg);
    }
}
