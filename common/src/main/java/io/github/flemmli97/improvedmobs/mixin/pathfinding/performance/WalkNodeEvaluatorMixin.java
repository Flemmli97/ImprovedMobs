package io.github.flemmli97.improvedmobs.mixin.pathfinding.performance;

import io.github.flemmli97.improvedmobs.mixinhelper.CachedRawPathTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
     * {@link WalkNodeEvaluator#getBlockPathType(BlockGetter, int, int, int)}
     * Override would be prob easier but more incompatible.
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
