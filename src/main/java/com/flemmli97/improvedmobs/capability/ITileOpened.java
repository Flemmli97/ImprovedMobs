package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public interface ITileOpened {

    boolean playerOpened();

    void setOpened(TileEntity tile);

    void writeToNBT(CompoundNBT compound);

    void readFromNBT(CompoundNBT nbt);

}
