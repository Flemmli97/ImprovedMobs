package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    // Legacy handling
    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void loadData(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo info) {
        CompoundTag data = null;
        if (tag.contains("IMHasBeenOpened")) {
            data = tag;
        } else if (tag.contains(ImprovedMobs.MODID + ":container")) {
            data = tag.getCompound(ImprovedMobs.MODID + ":container");
        }
        if (data != null) {
            ImprovedMobsAttachments.HAS_BEEN_OPENED.get().get(this).read(data, provider);
        }
    }
}
