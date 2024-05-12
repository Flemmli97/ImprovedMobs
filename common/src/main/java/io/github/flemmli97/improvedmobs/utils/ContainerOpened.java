package io.github.flemmli97.improvedmobs.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerOpened {

    boolean playerOpened();

    void setOpened(BlockEntity tile);

    void writeToNBT(CompoundTag compound);

    void readFromNBT(CompoundTag nbt);

}
