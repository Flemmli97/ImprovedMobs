package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.events.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Region;

public class NodeUtils {

    public static int collectAdditionalNodes(NodeProcessor processor, Region blockaccess, MobEntity entity, PathPoint[] points, PathPoint point, int i, TriFunction<Integer, Integer, Integer, PathPoint> func) {
        //long nano = System.nanoTime();
        if (((ILadderFlagNode) processor).canClimbLadder()) {
            i += NodeUtils.addLadderPoints(points, point, i, blockaccess, func);
        }
        //Rechecks all potential points. Needs more tests for performance.
        if (entity.getAttackTarget() != null && entity.getPersistentData().getBoolean(EventHandler.breaker)) {
            BlockPos.Mutable pos = new BlockPos.Mutable(point.x, point.y, point.z);
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || z != 0) {
                        PathPoint pathpoint = getPoint(processor, blockaccess, entity, pos.setPos(point.x + x, point.y, point.z + z), true, func);
                        if (pathpoint != null) {
                            points[i++] = pathpoint;
                            //this.toAdd += 1;
                        }
                    }
                }
        }
        //System.out.println("Total: " + (System.nanoTime() - nano));
        return i;
    }

    private static int addLadderPoints(PathPoint[] points, PathPoint currentPoint, int i, Region blockaccess, TriFunction<Integer, Integer, Integer, PathPoint> func) {
        int added = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable(currentPoint.x, currentPoint.y + 1, currentPoint.z);
        PathPoint ladderUp = func.apply(pos.getX(), pos.getY(), pos.getZ());
        if (ladderUp != null && !ladderUp.visited && blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderUp;
            added++;
        }
        pos = pos.move(0, -2, 0);
        PathPoint ladderDown = func.apply(pos.getX(), pos.getY(), pos.getZ());
        if (ladderDown != null && !ladderDown.visited && blockaccess.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            points[i++] = ladderDown;
            added++;
        }
        return added;
    }

    private static PathPoint getPoint(NodeProcessor processor, Region blockaccess, MobEntity entity, BlockPos.Mutable pos, boolean init, TriFunction<Integer, Integer, Integer, PathPoint> func) {
        BlockState baseState = blockaccess.getBlockState(pos);
        boolean baseBreak = canBreak(baseState, entity);
        if (baseBreak || canBreak(blockaccess.getBlockState(pos.move(Direction.UP)), entity)) {
            if (!baseBreak)
                pos.move(Direction.DOWN);
            PathNodeType ground = processor.getFloorNodeType(blockaccess, pos.getX(), pos.getY(), pos.getZ());
            if (entity.getPathPriority(ground) < 0 && (!baseBreak || checkEmpty(pos.move(Direction.UP), entity.getWidth() * 0.5, blockaccess, entity))) {
                PathPoint point = func.apply(pos.getX(), pos.getY(), pos.getZ());
                if (point != null && !point.visited) {
                    point.costMalus = Math.max(0, entity.getPathPriority(ground)) + 0.5f;
                    point.nodeType = PathNodeType.WALKABLE;
                    return point;
                }
            }
            if (baseBreak)
                pos.move(Direction.DOWN);
            if ((baseBreak || baseState.getCollisionShapeUncached(blockaccess, pos).isEmpty()) && checkFor(pos, entity.getWidth() * 0.5, blockaccess, entity)) {
                PathPoint point = func.apply(pos.getX(), pos.getY(), pos.getZ());
                if (point != null && !point.visited) {
                    point.costMalus = Math.max(0, entity.getPathPriority(ground)) + 0.5f;
                    point.nodeType = PathNodeType.WALKABLE;
                    return point;
                }
            }
        }
        return null;
    }

    private static boolean checkFor(BlockPos pos, double width, Region blockaccess, MobEntity entity) {
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos.getX() - width + 0.5, pos.getY() + 0.001D, pos.getZ() - width + 0.5, pos.getX() + width + 0.5, pos.getY() + entity.getHeight() - 0.002D, pos.getZ() + width + 0.5);
        return blockaccess.func_241457_a_(entity, axisalignedbb, (state, p) -> !Config.CommonConfig.breakableBlocks.canBreak(state)).allMatch(VoxelShape::isEmpty);
    }

    private static boolean checkEmpty(BlockPos pos, double width, Region blockaccess, MobEntity entity) {
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos.getX() - width + 0.5, pos.getY() + 0.001D, pos.getZ() - width + 0.5, pos.getX() + width + 0.5, pos.getY() + entity.getHeight() - 0.002D, pos.getZ() + width + 0.5);
        return blockaccess.getBlockCollisionShapes(entity, axisalignedbb).allMatch(VoxelShape::isEmpty);
    }

    private static boolean canBreak(BlockState state, MobEntity entity) {
        if (!GeneralHelperMethods.canHarvest(state, entity.getHeldItemMainhand()) && !GeneralHelperMethods.canHarvest(state, entity.getHeldItemOffhand()))
            return false;
        return entity.getPersistentData().getBoolean(EventHandler.breaker) && Config.CommonConfig.breakableBlocks.canBreak(state);
    }

    public interface TriFunction<A, B, C, D> {

        D apply(A a, B b, C c);
    }
}
