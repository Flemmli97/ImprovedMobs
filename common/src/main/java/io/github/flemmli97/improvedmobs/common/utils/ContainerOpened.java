package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.tenshilib.common.attachment.SerializableAttachment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class ContainerOpened implements SerializableAttachment<CompoundTag, ContainerOpened> {

    private boolean opened = false;

    public boolean playerOpened() {
        return this.opened;
    }

    public void setOpened(BlockEntity tile) {
        this.opened = true;
        tile.setChanged();
    }

    @Override
    public ContainerOpened read(CompoundTag tag, HolderLookup.Provider provider) {
        this.opened = tag.contains("IMHasBeenOpened") ? tag.getBoolean("IMHasBeenOpened") : tag.getBoolean("HasBeenOpened");
        return this;
    }

    @Override
    public @Nullable CompoundTag write(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasBeenOpened", this.opened);
        return tag;
    }
}
