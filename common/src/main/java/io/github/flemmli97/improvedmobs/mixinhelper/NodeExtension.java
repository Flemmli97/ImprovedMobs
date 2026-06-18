package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface NodeExtension {

    boolean improvedMobs$canBreakBlocks();

    default ResourceLocation improvedMobs$pathTypeOf(BlockPos pos, ResourceLocation... only) {
        return null;
    }
}
