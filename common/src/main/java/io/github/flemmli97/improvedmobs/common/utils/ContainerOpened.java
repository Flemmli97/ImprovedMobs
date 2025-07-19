package io.github.flemmli97.improvedmobs.common.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerOpened {

    private boolean opened = false;

    public boolean playerOpened() {
        return this.opened;
    }

    public void setOpened(BlockEntity tile) {
        this.opened = true;
        tile.setChanged();
    }

    public CompoundTag writeToNBT(CompoundTag compound) {
        compound.putBoolean("HasBeenOpened", this.opened);
        return compound;
    }

    public void readFromNBT(CompoundTag nbt) {
        this.opened = nbt.contains("IMHasBeenOpened") ? nbt.getBoolean("IMHasBeenOpened") : nbt.getBoolean("HasBeenOpened");
    }
}
