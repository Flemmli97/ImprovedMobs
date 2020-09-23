package com.flemmli97.improvedmobs.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class TileCapNetwork implements Capability.IStorage<ITileOpened> {

    @Override
    public INBT writeNBT(Capability<ITileOpened> capability, ITileOpened instance, Direction side) {
        CompoundNBT compound = new CompoundNBT();
        instance.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readNBT(Capability<ITileOpened> capability, ITileOpened instance, Direction side, INBT nbt) {
        instance.readFromNBT((CompoundNBT) nbt);
    }
}
