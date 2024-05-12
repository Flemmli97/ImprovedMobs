package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.utils.ContainerOpened;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements ContainerOpened {

    @Unique
    private boolean openedTileIM = false;

    @Override
    public boolean playerOpened() {
        return this.openedTileIM;
    }

    @Override
    public void setOpened(BlockEntity tile) {
        this.openedTileIM = true;
        tile.setChanged();
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        compound.putBoolean("IMHasBeenOpened", this.openedTileIM);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.openedTileIM = nbt.getBoolean("IMHasBeenOpened");
    }

    @Inject(method = "saveMetadata", at = @At(value = "RETURN"))
    private void saveData(CompoundTag tag, CallbackInfo info) {
        this.writeToNBT(tag);
    }

    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void loadData(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo info) {
        this.readFromNBT(tag);
    }
}
