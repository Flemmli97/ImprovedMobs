package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FlyNodeEvaluator.class)
public abstract class FlyNodeMixin extends NodeEvaluator {

    @Unique
    private final BlockPos.MutableBlockPos improvedMobs$pos = new BlockPos.MutableBlockPos();

    @Inject(method = "getPathType", at = @At(value = "HEAD"), cancellable = true)
    private void breakableNodes(PathfindingContext context, int x, int y, int z, CallbackInfoReturnable<PathType> info) {
        if (!((NodeExtension) this).improvedMobs$canBreakBlocks())
            return;
        if (PathFindingUtils.canBreak(context.getBlockState(this.improvedMobs$pos.set(x, y, z)), this.improvedMobs$pos, this.mob)) {
            info.setReturnValue(PathType.WALKABLE);
        }
    }
}
