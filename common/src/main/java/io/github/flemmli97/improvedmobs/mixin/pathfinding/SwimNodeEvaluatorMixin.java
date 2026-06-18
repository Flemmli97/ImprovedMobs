package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SwimNodeEvaluator.class)
public abstract class SwimNodeEvaluatorMixin extends NodeEvaluator implements NodeExtension {

    @Unique
    private final Long2ObjectMap<ResourceLocation> improvedMobs$pathTypePosCache = new Long2ObjectOpenHashMap<>();

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.improvedMobs$pathTypePosCache.clear();
    }

    @WrapOperation(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isPathfindable(Lnet/minecraft/world/level/pathfinder/PathComputationType;)Z", ordinal = 0))
    private boolean pathTypeCheckFirst(BlockState state, PathComputationType type, Operation<Boolean> original, @Local BlockPos.MutableBlockPos pos) {
        if (PathFindingUtils.BREAKABLE.equals(this.improvedMobs$pathTypeOf(pos, null))) {
            return true;
        }
        return original.call(state, type);
    }

    @WrapOperation(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isPathfindable(Lnet/minecraft/world/level/pathfinder/PathComputationType;)Z", ordinal = 1))
    private boolean pathTypeCheck(BlockState state, PathComputationType type, Operation<Boolean> original, @Local BlockPos.MutableBlockPos pos) {
        if (PathFindingUtils.BREAKABLE.equals(this.improvedMobs$pathTypeOf(pos, null))) {
            return true;
        }
        return original.call(state, type);
    }

    @ModifyExpressionValue(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean fluidCheck(boolean original, @Local BlockPos.MutableBlockPos pos, @Local BlockState state) {
        if (PathFindingUtils.BREAKABLE.equals(this.improvedMobs$pathTypeOf(pos, null))) {
            return true;
        }
        return original;
    }

    @Override
    public ResourceLocation improvedMobs$pathTypeOf(BlockPos pos, ResourceLocation... only) {
        return this.improvedMobs$pathTypePosCache.computeIfAbsent(pos.asLong(),
                p -> PathFindingUtils.pathType(this.currentContext.getBlockState(pos), pos, this.mob, PathFindingUtils.BREAKABLE));
    }
}