package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface NodeExtension {

    boolean improvedMobs$canBreakBlocks();

    default ResourceLocation improvedMobs$pathTypeOf(BlockPos pos, Direction direction, ResourceLocation... only) {
        return null;
    }
}
