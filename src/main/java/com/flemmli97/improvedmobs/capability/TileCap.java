package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class TileCap implements ITileOpened {

    private boolean opened = false;

    public TileCap() {
    }

    @Override
    public boolean playerOpened() {
        return this.opened;
    }

    @Override
    public void setOpened(TileEntity tile) {
        this.opened = true;
        tile.markDirty();
    }

    @Override
    public void writeToNBT(CompoundNBT compound) {
        compound.putBoolean("HasBeenOpened", this.opened);
    }

    @Override
    public void readFromNBT(CompoundNBT nbt) {
        this.opened = nbt.getBoolean("HasBeenOpened");
    }
}
