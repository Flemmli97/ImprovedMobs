package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkAndSwimNodeProcessor;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WalkAndSwimNodeProcessor.class)
public abstract class SwimWalkMixin extends WalkNodeProcessor {

    @Inject(method = "func_222859_a", at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void ignoreLadder(PathPoint[] points, PathPoint point, CallbackInfoReturnable<Integer> info, int i) {
        if (((ILadderFlagNode) this).canClimbLadder())
            i = this.addLadderPoints(points, point, i);
        info.setReturnValue(i);
        info.cancel();
    }

    private int addLadderPoints(PathPoint[] points, PathPoint currentPoint, int i) {
        PathPoint ladderUp = this.openPoint(currentPoint.x, currentPoint.y + 1, currentPoint.z);
        PathPoint ladderDown = this.openPoint(currentPoint.x, currentPoint.y - 1, currentPoint.z);
        if (ladderUp != null && !ladderUp.visited && this.blockaccess.getBlockState(new BlockPos(ladderUp.x, ladderUp.y, ladderUp.z)).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderUp;
        }
        if (ladderDown != null && !ladderDown.visited && this.blockaccess.getBlockState(new BlockPos(ladderDown.x, ladderDown.y, ladderDown.z)).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderDown;
        }
        return i;
    }
}
