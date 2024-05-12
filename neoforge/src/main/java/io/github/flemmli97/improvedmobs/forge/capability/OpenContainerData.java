package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.utils.ContainerOpened;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class OpenContainerData implements ContainerOpened {

    public static final IAttachmentSerializer<CompoundTag, OpenContainerData> SERIALIZER = new IAttachmentSerializer<>() {

        @Override
        public OpenContainerData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            OpenContainerData cap = new OpenContainerData();
            cap.readFromNBT(tag);
            return cap;
        }

        @Override
        public CompoundTag write(OpenContainerData object, HolderLookup.Provider provider) {
            CompoundTag compound = new CompoundTag();
            object.writeToNBT(compound);
            return compound;
        }
    };

    private boolean opened = false;

    public OpenContainerData() {
    }

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
