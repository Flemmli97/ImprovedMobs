package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.fabric.mixinutil.ContainerOpenAccess;
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
public abstract class BlockEntityMixin implements ContainerOpenAccess {

    @Unique
    private final ContainerOpened improvedMobs$OpenedTile = new ContainerOpened();

    @Inject(method = "saveMetadata", at = @At(value = "RETURN"))
    private void saveData(CompoundTag tag, CallbackInfo info) {
        tag.put(ImprovedMobs.MODID + ":container", this.improvedMobs$OpenedTile.writeToNBT(new CompoundTag()));
    }

    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void loadData(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo info) {
        CompoundTag data;
        if (tag.contains("IMHasBeenOpened")) {
            data = tag;
        } else {
            data = tag.getCompound(ImprovedMobs.MODID + ":container");
        }
        this.improvedMobs$OpenedTile.readFromNBT(data);
    }

    @Override
    public ContainerOpened improvedMobs$getContainerState() {
        return this.improvedMobs$OpenedTile;
    }
}
