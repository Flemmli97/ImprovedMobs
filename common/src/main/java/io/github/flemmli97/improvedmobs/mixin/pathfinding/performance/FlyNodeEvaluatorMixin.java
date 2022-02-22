package io.github.flemmli97.improvedmobs.mixin.pathfinding.performance;

import io.github.flemmli97.improvedmobs.mixinhelper.CachedRawPathTypes;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Same as {@link WalkNodeEvaluatorMixin}
 */
@Mixin(FlyNodeEvaluator.class)
public class FlyNodeEvaluatorMixin {

    @Inject(method = "getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;", at = @At("HEAD"), cancellable = true)
    private void isCached(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<BlockPathTypes> info) {
        long key = SectionPos.asLong(x, y, z);
        if (((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().containsKey(SectionPos.asLong(x, y, z))) {
            info.setReturnValue(((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().get(key));
            info.cancel();
        }
    }

    @Inject(method = "getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;", at = @At("RETURN"))
    private void cache(BlockGetter level, int x, int y, int z, CallbackInfoReturnable<BlockPathTypes> info) {
        ((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().put(SectionPos.asLong(x, y, z), info.getReturnValue());
    }
}
