package com.flemmli97.improvedmobs.handler.tilecap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class TileCapProvider implements ICapabilitySerializable<NBTBase> {

	@CapabilityInject(ITileOpened.class)
	public static final Capability<ITileOpened> OpenedCap = null;

	private ITileOpened instance = OpenedCap.getDefaultInstance();

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == OpenedCap;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == OpenedCap)
			return OpenedCap.cast(this.instance);
		else
			return null;
	}

	@Override
	public NBTBase serializeNBT() {
		return OpenedCap.getStorage().writeNBT(OpenedCap, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		OpenedCap.getStorage().readNBT(OpenedCap, this.instance, null, nbt);
	}

}
