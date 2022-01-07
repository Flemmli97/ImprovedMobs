package io.github.flemmli97.improvedmobs.fabric.capability;

import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OpenTileData implements ITileOpened {

    private boolean opened = false;

    @Override
    public boolean playerOpened() {
        return this.opened;
    }

    @Override
    public void setOpened(BlockEntity tile) {
        this.opened = true;
        tile.setChanged();
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        compound.putBoolean("HasBeenOpened", this.opened);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.opened = nbt.getBoolean("HasBeenOpened");
    }
}
