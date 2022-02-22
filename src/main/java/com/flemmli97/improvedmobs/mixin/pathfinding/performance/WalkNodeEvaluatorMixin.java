package com.flemmli97.improvedmobs.mixin.pathfinding.performance;

import com.flemmli97.improvedmobs.utils.CachedRawPathTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Not sure if ill release this as a separate mod or something yet
 * Dont have enough stuff to merit a "performance" mod
 * <p>
 * The effect of this is more noticable with bigger mobs and especially when that mob also uses the
 * flying navigation
 */
@Mixin(WalkNodeProcessor.class)
public class WalkNodeEvaluatorMixin implements CachedRawPathTypes {

    /**
     * Vanilla has a cache already for BlockPos <-> PathNodeType but that cache is for already "processed" Types
     * By also caching the raw PathType we can further reduce unneeded expensive calculations and calls to {@link IBlockReader#getBlockState}
     * used by e.g. {@link WalkNodeProcessor#collectSurroundingNodeTypes}
     */
    private final Long2ObjectMap<PathNodeType> pathTypeByPosCacheRaw = new Long2ObjectOpenHashMap<>();

    @Inject(method = "postProcess", at = @At("RETURN"))
    private void reset(CallbackInfo info) {
        this.pathTypeByPosCacheRaw.clear();
    }

    /**
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

    /**
     * {@link WalkNodeProcessor#getFloorNodeType(IBlockReader, int, int, int)}
     * Override would be prob easier but well.
     * Check if the position is already cached else goes to {@link #cache} to cache it
     */
    @Inject(method = "getFloorNodeType(Lnet/minecraft/world/IBlockReader;III)Lnet/minecraft/pathfinding/PathNodeType;", at = @At("HEAD"), cancellable = true)
    private void isCached(IBlockReader level, int x, int y, int z, CallbackInfoReturnable<PathNodeType> info) {
        long key = SectionPos.asLong(x, y, z);
        if (this.pathTypeByPosCacheRaw.containsKey(SectionPos.asLong(x, y, z))) {
            info.setReturnValue(this.pathTypeByPosCacheRaw.get(key));
            info.cancel();
        }
    }

    @Inject(method = "getFloorNodeType(Lnet/minecraft/world/IBlockReader;III)Lnet/minecraft/pathfinding/PathNodeType;", at = @At("RETURN"))
    private void cache(IBlockReader level, int x, int y, int z, CallbackInfoReturnable<PathNodeType> info) {
        this.pathTypeByPosCacheRaw.put(SectionPos.asLong(x, y, z), info.getReturnValue());
    }

    @Override
    public Long2ObjectMap<PathNodeType> getPathTypeByPosCacheRaw() {
        return this.pathTypeByPosCacheRaw;
    }
}
