package com.flemmli97.improvedmobs.handler.tilecap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TileCapNetwork implements IStorage<ITileOpened> {

	@Override
	public NBTBase writeNBT(Capability<ITileOpened> capability, ITileOpened instance, EnumFacing side) {
		NBTTagCompound compound = new NBTTagCompound();
		instance.writeToNBT(compound);
		return compound;
	}

	@Override
	public void readNBT(Capability<ITileOpened> capability, ITileOpened instance, EnumFacing side, NBTBase nbt) {
		instance.readFromNBT((NBTTagCompound) nbt);
	}
}
