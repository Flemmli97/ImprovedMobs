package com.flemmli97.improvedmobs.handler.tilecap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface ITileOpened{
		
	public boolean playerOpened();
	
	public void setOpened(TileEntity tile);

	public void writeToNBT(NBTTagCompound compound);

	public void readFromNBT(NBTTagCompound nbt);
	
}
