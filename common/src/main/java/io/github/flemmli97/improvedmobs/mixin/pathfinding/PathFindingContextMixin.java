package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.mixinhelper.PathfindingContextExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathfindingContext.class)
public abstract class PathFindingContextMixin implements PathfindingContextExt {

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Unique
    private AdditionalPathTypeHandler improvedMobs$pathTypeHandler;

    @Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
    private void onGetPathType(int x, int y, int z, CallbackInfoReturnable<PathType> info) {
        // The ground below should also return default as otherwise mobs think they cant walk on it.
        // See WalkNodeEvaluator#getPathTypeStatic(PathfindingContext context, BlockPos.MutableBlockPos pos)
        if (this.improvedMobs$pathTypeHandler != null && y >= this.improvedMobs$pathTypeHandler.height()) {
            if (this.improvedMobs$pathTypeHandler.walkable().test(new BlockPos(x, y, z))) {
                info.setReturnValue(PathType.WALKABLE);
            }
        }
    }

    @Override
    public void improvedMobs$setPathHandler(AdditionalPathTypeHandler pathTypeHandler) {
        this.improvedMobs$pathTypeHandler = pathTypeHandler;
    }

    @Override
    public AdditionalPathTypeHandler improvedMobs$getPathHandler() {
        return this.improvedMobs$pathTypeHandler;
    }
}
