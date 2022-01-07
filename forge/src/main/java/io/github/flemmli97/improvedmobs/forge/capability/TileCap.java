package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileCap implements ITileOpened, ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<ITileOpened> holder = LazyOptional.of(() -> this);

    private boolean opened = false;

    public TileCap() {
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        return TileCapProvider.CAP.orEmpty(capability, this.holder);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        this.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.readFromNBT(arg);
    }
}
