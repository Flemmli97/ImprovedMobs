package io.github.flemmli97.improvedmobs.mixin.pathfinding.performance;

import io.github.flemmli97.improvedmobs.mixinhelper.CachedRawPathTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Not sure if I will release this as a separate mod or something yet
 * Dont have enough stuff to merit a "performance" mod
 * <p>
 * The effect of this is more noticable with bigger mobs and especially when that mob also uses the
 * flying navigation
 */
@Mixin(WalkNodeEvaluator.class)
public class WalkNodeEvaluatorMixin implements CachedRawPathTypes {

    /**
     * Vanilla has a cache already for BlockPos <-> BlockPathTypes but that cache is for already "processed" Types
     * By also caching the raw PathType we can further reduce unneeded expensive calculations and calls to {@link BlockGetter#getBlockState}
     * used by e.g. {@link WalkNodeEvaluator#getBlockPathTypes}
     */
    private final Long2ObjectMap<BlockPathTypes> pathTypeByPosCacheRaw = new Long2ObjectOpenHashMap<>();

    @Inject(method = "done", at = @At("RETURN"))
    private void reset(CallbackInfo info) {
        this.pathTypeByPosCacheRaw.clear();
    }

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

    /**
     * {@link WalkNodeEvaluator#getBlockPathType(BlockGetter, int, int, int)}
     * Override would be prob easier but well.
     * Check if the position is already cached else goes to {@link #cache} to cache it
     */
    @Inject(method = "getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;", at = @At("HEAD"), cancellable = true)
    private void isCached(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<BlockPathTypes> info) {
        long key = SectionPos.asLong(x, y, z);
        if (this.pathTypeByPosCacheRaw.containsKey(SectionPos.asLong(x, y, z))) {
            info.setReturnValue(this.pathTypeByPosCacheRaw.get(key));
            info.cancel();
        }
    }

    @Inject(method = "getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;", at = @At("RETURN"))
    private void cache(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<BlockPathTypes> info) {
        this.pathTypeByPosCacheRaw.put(SectionPos.asLong(x, y, z), info.getReturnValue());
    }

    @Override
    public Long2ObjectMap<BlockPathTypes> getPathTypeByPosCacheRaw() {
        return this.pathTypeByPosCacheRaw;
    }
}
