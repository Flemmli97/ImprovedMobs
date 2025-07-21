package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
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
public abstract class SwimNodeEvaluatorMixin extends NodeEvaluator {

    @Unique
    private final Object2BooleanMap<Long> improvedMobs$breakableMap = new Object2BooleanOpenHashMap<>();

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.improvedMobs$breakableMap.clear();
    }

    @WrapOperation(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isPathfindable(Lnet/minecraft/world/level/pathfinder/PathComputationType;)Z", ordinal = 0))
    private boolean pathTypeCheckFirst(BlockState state, PathComputationType type, Operation<Boolean> original, @Local BlockPos.MutableBlockPos pos) {
        if (!((NodeExtension) this).improvedMobs$canBreakBlocks())
            return original.call(state, type);
        if (this.improvedMobs$breakableMap.computeIfAbsent(pos.asLong(), l -> PathFindingUtils.canBreak(state, pos, this.mob))) {
            return false;
        }
        return original.call(state, type);
    }

    @WrapOperation(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isPathfindable(Lnet/minecraft/world/level/pathfinder/PathComputationType;)Z", ordinal = 1))
    private boolean pathTypeCheck(BlockState state, PathComputationType type, Operation<Boolean> original, @Local BlockPos.MutableBlockPos pos) {
        if (!((NodeExtension) this).improvedMobs$canBreakBlocks())
            return original.call(state, type);
        if (this.improvedMobs$breakableMap.computeIfAbsent(pos.asLong(), l -> PathFindingUtils.canBreak(state, pos, this.mob))) {
            return true;
        }
        return original.call(state, type);
    }

    @ModifyExpressionValue(method = "getPathTypeOfMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean fluidCheck(boolean original, @Local BlockPos.MutableBlockPos pos, @Local BlockState state) {
        if (!((NodeExtension) this).improvedMobs$canBreakBlocks())
            return original;
        if (this.improvedMobs$breakableMap.computeIfAbsent(pos.asLong(), l -> PathFindingUtils.canBreak(state, pos, this.mob))) {
            return true;
        }
        return original;
    }
}