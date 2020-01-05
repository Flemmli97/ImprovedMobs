package com.flemmli97.improvedmobs.handler.tilecap;

import net.minecraft.nbt.NBTTagCompound;
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
	public void writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("HasBeenOpened", this.opened);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.opened = nbt.getBoolean("HasBeenOpened");
	}
}
