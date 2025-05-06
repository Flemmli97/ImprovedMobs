package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.utils.ContainerOpened;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerCap extends ContainerOpened implements ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<ContainerOpened> holder = LazyOptional.of(() -> this);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        return CapabilityProvider.CAP.orEmpty(capability, this.holder);
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
