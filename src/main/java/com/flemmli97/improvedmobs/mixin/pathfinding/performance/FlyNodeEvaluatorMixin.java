package com.flemmli97.improvedmobs.mixin.pathfinding.performance;

import com.flemmli97.improvedmobs.utils.CachedRawPathTypes;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Same as {@link WalkNodeEvaluatorMixin}
 */
@Mixin(FlyingNodeProcessor.class)
public class FlyNodeEvaluatorMixin {

    @Inject(method = "getFloorNodeType(Lnet/minecraft/world/IBlockReader;III)Lnet/minecraft/pathfinding/PathNodeType;", at = @At("HEAD"), cancellable = true)
    private void isCached(IBlockReader level, int x, int y, int z, CallbackInfoReturnable<PathNodeType> info) {
        long key = SectionPos.asLong(x, y, z);
        if (((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().containsKey(SectionPos.asLong(x, y, z))) {
            info.setReturnValue(((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().get(key));
            info.cancel();
        }
    }

    @Inject(method = "getFloorNodeType(Lnet/minecraft/world/IBlockReader;III)Lnet/minecraft/pathfinding/PathNodeType;", at = @At("RETURN"))
    private void cache(IBlockReader level, int x, int y, int z, CallbackInfoReturnable<PathNodeType> info) {
        ((CachedRawPathTypes) this).getPathTypeByPosCacheRaw().put(SectionPos.asLong(x, y, z), info.getReturnValue());
    }
}
