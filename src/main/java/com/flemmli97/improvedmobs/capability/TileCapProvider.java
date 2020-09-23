package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TileCapProvider implements ICapabilitySerializable<INBT> {

    @CapabilityInject(ITileOpened.class)
    public static final Capability<ITileOpened> OpenedCap = null;

    private ITileOpened instance = OpenedCap.getDefaultInstance();

    @Override
    public INBT serializeNBT() {
        return OpenedCap.getStorage().writeNBT(OpenedCap, this.instance, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        OpenedCap.getStorage().readNBT(OpenedCap, this.instance, null, nbt);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return OpenedCap.orEmpty(cap, LazyOptional.of(() -> this.instance));
    }
}
