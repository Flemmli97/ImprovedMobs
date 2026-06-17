package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.core.BlockPos;

import java.util.function.Predicate;

public interface PathfindingContextExt {

    void improvedMobs$setBreakingHandler(BreakingHandler breakingHandler);

    BreakingHandler improvedMobs$getBreakingHandler();

    record BreakingHandler(int height, Predicate<BlockPos> breakingHandler) {
    }
}
