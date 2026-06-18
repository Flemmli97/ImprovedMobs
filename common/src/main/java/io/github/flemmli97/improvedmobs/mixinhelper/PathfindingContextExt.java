package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.core.BlockPos;

import java.util.function.Predicate;

public interface PathfindingContextExt {

    void improvedMobs$setPathHandler(AdditionalPathTypeHandler pathTypeHandler);

    AdditionalPathTypeHandler improvedMobs$getPathHandler();

    record AdditionalPathTypeHandler(int height, Predicate<BlockPos> walkable) {
    }
}
