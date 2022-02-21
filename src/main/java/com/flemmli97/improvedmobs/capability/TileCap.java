package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TileCap implements ITileOpened, ICapabilitySerializable<CompoundNBT> {

    private final LazyOptional<ITileOpened> holder = LazyOptional.of(() -> this);

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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction arg) {
        return TileCapProvider.OpenedCap.orEmpty(capability, this.holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        this.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT arg) {
        this.readFromNBT(arg);
    }
}
