package com.flemmli97.improvedmobs.mixin.pathfinding.performance;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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
@Mixin(WalkNodeProcessor.class)
public class WalkNodeStaticMixin {

    /**
     * {@link WalkNodeProcessor#getSurroundingDanger}
     * We dont wanna call another "getBlockState" when we already have the blockstate.
     * We use {@link #fluidState} below to get the FluidState from the BlockState
     */
    @Redirect(method = "getSurroundingDanger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockReader;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    private static FluidState stop(IBlockReader level, BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }

    @Inject(method = "getSurroundingDanger", at = @At(value = "INVOKE", target = "Lnet/minecraft/pathfinding/WalkNodeProcessor;isFiery(Lnet/minecraft/block/BlockState;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fluidState(IBlockReader level, BlockPos.Mutable centerPos, PathNodeType nodeType, CallbackInfoReturnable<PathNodeType> info,
                                   int i, int j, int k, int l, int i1, int j1, BlockState state) {
        if (state.getFluidState().isTagged(FluidTags.WATER)) {
            info.setReturnValue(PathNodeType.WATER_BORDER);
            info.cancel();
        }
    }
}
