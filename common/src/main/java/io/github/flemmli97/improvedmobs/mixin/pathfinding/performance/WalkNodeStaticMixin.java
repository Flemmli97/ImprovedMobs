package io.github.flemmli97.improvedmobs.mixin.pathfinding.performance;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Lithium already does all the things (and more) applied here.
 * They override the affected method though so this mixins requirement is false
 */
@Mixin(WalkNodeEvaluator.class)
public class WalkNodeStaticMixin {

    /**
     * {@link WalkNodeEvaluator#checkNeighbourBlocks}
     * We dont wanna call another "getBlockState" when we already have the blockstate.
     * We use {@link #fluidState} below to get the FluidState from the BlockState
     */
    @Redirect(method = "checkNeighbourBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private static FluidState stop(BlockGetter level, BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Inject(method = "checkNeighbourBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;isBurningBlock(Lnet/minecraft/world/level/block/state/BlockState;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fluidState(BlockGetter level, BlockPos.MutableBlockPos centerPos, BlockPathTypes nodeType, CallbackInfoReturnable<BlockPathTypes> info,
                                   int i, int j, int k, int l, int i1, int j1, BlockState state) {
        if (state.getFluidState().is(FluidTags.WATER)) {
            info.setReturnValue(BlockPathTypes.WATER_BORDER);
            info.cancel();
        }
    }
}
