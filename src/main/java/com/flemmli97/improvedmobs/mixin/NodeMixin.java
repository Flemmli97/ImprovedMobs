package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.events.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkAndSwimNodeProcessor;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = {WalkNodeProcessor.class, WalkAndSwimNodeProcessor.class, FlyingNodeProcessor.class}, priority = 500)
public abstract class NodeMixin extends NodeProcessor implements ILadderFlagNode {
    //@Unique
    //private int toAdd;

    @Inject(method = "func_222859_a", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void ignoreLadder(PathPoint[] points, PathPoint point, CallbackInfoReturnable<Integer> info, int i) {
        if (this.entity != null) {
            if (((LadderFlagMixin) (Object) this).canClimbLadder()) {
                i += this.addLadderPoints(points, point, i);
            }
            //Rechecks all potential points. Needs more tests for performance.
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || z != 0) {
                        PathPoint pathpoint = this.getPoint(point.x + x, point.y, point.z + z, true);
                        if (pathpoint != null) {
                            points[i++] = pathpoint;
                            //this.toAdd += 1;
                        }
                    }
                }
            info.setReturnValue(i);
            info.cancel();
        }
    }

    /* Doesnt work...
    @ModifyVariable(method = "func_222859_a", at = @At(value = "RETURN"), ordinal = 0)
    private int updateVal(int orig){
        int x = orig+this.toAdd;
        return x;
    }*/

    private int addLadderPoints(PathPoint[] points, PathPoint currentPoint, int i) {
        int added = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable(currentPoint.x, currentPoint.y + 1, currentPoint.z);
        PathPoint ladderUp = this.openPoint(pos.getX(), pos.getY(), pos.getZ());
        if (!ladderUp.visited && this.blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderUp;
            added++;
        }
        pos = pos.move(0, -2, 0);
        PathPoint ladderDown = this.openPoint(pos.getX(), pos.getY(), pos.getZ());
        if (!ladderDown.visited && this.blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderDown;
            added++;
        }
        return added;
    }

    private PathPoint getPoint(int x, int y, int z, boolean init) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        if (this.canBreak(this.blockaccess.getBlockState(pos))) {
            if (this.checkFor(pos, this.entity.getWidth() * 0.5)) {
                PathPoint point = this.openPoint(x, y, z);
                point.costMalus = 0.5f;
                point.nodeType = PathNodeType.WALKABLE;
                if (!point.visited)
                    return point;
            }
        } else if (this.canBreak(this.blockaccess.getBlockState(pos.move(Direction.UP)))) {
            if (this.checkFor(pos, this.entity.getWidth() * 0.5)) {
                PathPoint point = this.openPoint(x, y, z);
                point.costMalus = 0.5f;
                point.nodeType = PathNodeType.WALKABLE;
                if (!point.visited)
                    return point;
            }
        }
        double height = this.entity.getHeight();
        double width = this.entity.getWidth() * 0.5;
        while (pos.getY() - y < height) {
            boolean currentEmpty = this.blockaccess.getBlockState(pos).getCollisionShape(this.blockaccess, pos).isEmpty();
            pos.move(Direction.UP);
            if (currentEmpty && this.canBreak(this.blockaccess.getBlockState(pos)) && this.checkFor(pos, width)) {
                PathPoint point = this.openPoint(x, y + 1, z);
                point.costMalus = 0.5f;
                point.nodeType = PathNodeType.WALKABLE;
                if (!point.visited)
                    return point;
            }
        }
        return null;
    }

    private boolean checkFor(BlockPos pos, double width) {
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos.getX() - width + 0.5, pos.getY() + 0.001D, pos.getZ() - width + 0.5, pos.getX() + width + 0.5, pos.getY() + this.entity.getHeight() - 0.002D, pos.getZ() + width + 0.5);
        return this.blockaccess.getBlockCollisions(this.entity, axisalignedbb, (state, p) -> !Config.CommonConfig.breakableBlocks.canBreak(state)).allMatch(VoxelShape::isEmpty);
    }

    private boolean canBreak(BlockState state) {
        return this.entity.getPersistentData().getBoolean(EventHandler.breaker) && Config.CommonConfig.breakableBlocks.canBreak(state);
    }
}
