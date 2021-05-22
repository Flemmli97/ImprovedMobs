package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.events.EventHandler;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
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
    private void addAdditionalPoints(PathPoint[] points, PathPoint point, CallbackInfoReturnable<Integer> info, int i) {
        if (this.entity != null) {
            //long nano = System.nanoTime();
            if (((LadderFlagMixin) (Object) this).canClimbLadder()) {
                i += this.addLadderPoints(points, point, i);
            }
            //Rechecks all potential points. Needs more tests for performance.
            if (this.entity.getAttackTarget() != null && this.entity.getPersistentData().getBoolean(EventHandler.breaker)) {
                BlockPos.Mutable pos = new BlockPos.Mutable(point.x, point.y, point.z);
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || z != 0) {
                            PathPoint pathpoint = this.getPoint(pos.setPos(point.x + x, point.y, point.z + z), true);
                            if (pathpoint != null) {
                                points[i++] = pathpoint;
                                //this.toAdd += 1;
                            }
                        }
                    }
            }
            //System.out.println("Total: " + (System.nanoTime() - nano));
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
        if (ladderUp != null && !ladderUp.visited && this.blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderUp;
            added++;
        }
        pos = pos.move(0, -2, 0);
        PathPoint ladderDown = this.openPoint(pos.getX(), pos.getY(), pos.getZ());
        if (ladderDown != null && !ladderDown.visited && this.blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderDown;
            added++;
        }
        return added;
    }

    private PathPoint getPoint(BlockPos.Mutable pos, boolean init) {
        BlockState baseState = this.blockaccess.getBlockState(pos);
        boolean baseBreak = this.canBreak(baseState);
        if (baseBreak || this.canBreak(this.blockaccess.getBlockState(pos.move(Direction.UP)))) {
            if (!baseBreak)
                pos.move(Direction.DOWN);
            PathNodeType ground = this.getPathNodeType(this.blockaccess, pos.getX(), pos.getY(), pos.getZ());
            if (this.checkEmpty(pos.move(Direction.UP), this.entity.getWidth() * 0.5) && this.entity.getPathPriority(ground) >= 0 && ground != PathNodeType.OPEN) {
                PathPoint point = this.openPoint(pos.getX(), pos.getY(), pos.getZ());
                if (point != null && !point.visited) {
                    point.costMalus = 0.5f;
                    point.nodeType = PathNodeType.WALKABLE;
                    return point;
                }
            }
            pos.move(Direction.DOWN);
            if ((baseBreak || baseState.getCollisionShape(this.blockaccess, pos).isEmpty()) && this.checkFor(pos, this.entity.getWidth() * 0.5)) {
                PathPoint point = this.openPoint(pos.getX(), pos.getY(), pos.getZ());
                if (point != null && !point.visited) {
                    point.costMalus = 0.5f;
                    point.nodeType = PathNodeType.WALKABLE;
                    return point;
                }
            }
        }
        return null;
    }

    private boolean checkFor(BlockPos pos, double width) {
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos.getX() - width + 0.5, pos.getY() + 0.001D, pos.getZ() - width + 0.5, pos.getX() + width + 0.5, pos.getY() + this.entity.getHeight() - 0.002D, pos.getZ() + width + 0.5);
        return this.blockaccess.getBlockCollisions(this.entity, axisalignedbb, (state, p) -> !Config.CommonConfig.breakableBlocks.canBreak(state)).allMatch(VoxelShape::isEmpty);
    }

    private boolean checkEmpty(BlockPos pos, double width) {
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos.getX() - width + 0.5, pos.getY() + 0.001D, pos.getZ() - width + 0.5, pos.getX() + width + 0.5, pos.getY() + this.entity.getHeight() - 0.002D, pos.getZ() + width + 0.5);
        return this.blockaccess.getBlockCollisions(this.entity, axisalignedbb).allMatch(VoxelShape::isEmpty);
    }

    private boolean canBreak(BlockState state) {
        if (!GeneralHelperMethods.canHarvest(state, this.entity.getHeldItemMainhand()) && !GeneralHelperMethods.canHarvest(state, this.entity.getHeldItemOffhand()))
            return false;
        return this.entity.getPersistentData().getBoolean(EventHandler.breaker) && Config.CommonConfig.breakableBlocks.canBreak(state);
    }
}
