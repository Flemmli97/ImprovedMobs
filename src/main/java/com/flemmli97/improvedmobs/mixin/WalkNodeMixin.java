package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import com.flemmli97.improvedmobs.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.PathType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WalkNodeProcessor.class)
public abstract class WalkNodeMixin extends NodeProcessor implements ILadderFlagNode {

    @Unique
    private boolean canClimbLadder;

    @Inject(method = "getCommonNodeType", at = @At(value = "HEAD"), cancellable = true)
    private static void canBreak(IBlockReader reader, BlockPos pos, CallbackInfoReturnable<PathNodeType> info) {
        if (Config.CommonConfig.breakableBlocks.canBreak(reader.getBlockState(pos))) {
            info.setReturnValue(PathNodeType.BREACH);
            info.cancel();
        }
    }

    @Inject(method = "func_222859_a", at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void ignoreLadder(PathPoint[] points, PathPoint point, CallbackInfoReturnable<Integer> info, int i){
        if(this.canClimbLadder)
            i = this.addLadderPoints(points, point, i);
        info.setReturnValue(i);
        info.cancel();
    }

    private int addLadderPoints(PathPoint[] points, PathPoint currentPoint, int i){
        PathPoint ladderUp = this.openPoint(currentPoint.x, currentPoint.y + 1, currentPoint.z);
        PathPoint ladderDown = this.openPoint(currentPoint.x, currentPoint.y - 1, currentPoint.z);
        if(ladderUp!=null && !ladderUp.visited && this.blockaccess.getBlockState(new BlockPos(ladderUp.x, ladderUp.y, ladderUp.z)).isIn(BlockTags.CLIMBABLE)){
            points[i++] = ladderUp;
        }
        if(ladderDown!=null && !ladderDown.visited && this.blockaccess.getBlockState(new BlockPos(ladderDown.x, ladderDown.y, ladderDown.z)).isIn(BlockTags.CLIMBABLE)){
            points[i++] = ladderDown;
        }
        return i;
    }

    @Override
    public void setCanClimbLadder(boolean flag) {
        this.canClimbLadder = flag;
    }

    @Override
    public boolean canClimbLadder() {
        return this.canClimbLadder;
    }
}
