package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.config.Config;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

import java.util.function.Function;

public class PathFindingUtils {

    public static PathPoint notFloatingPathPointModifier(MobEntity mob, IBlockReader getter, int x, int y, int z, int stepModifier, Direction dir, PathNodeType standingType,
                                                         Function<BlockPos, PathNodeType> func, Function<AxisAlignedBB, Boolean> collision, Function<AxisAlignedBB, Boolean> collisionDefault,
                                                         Function<BlockPos, PathPoint> PathPointGetter, Object2BooleanMap<Long> breakableMap) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        BlockState state = getter.getBlockState(pos);
        if (breakableMap.computeIfAbsent(BlockPos.pack(x, y, z), p -> canBreak(state, pos, mob))) {
            AxisAlignedBB aabb = createAxisAlignedBBForPos(getter, x, y, z, mob.getWidth() / 2.0, mob.getHeight());
            if (stepModifier > 0 && !collisionDefault.apply(aabb.expand(-dir.getXOffset(), 0, -dir.getZOffset()))) {
                PathPoint PathPoint = PathPointGetter.apply(pos.setPos(x, y + 1, z));
                PathPoint.nodeType = PathNodeType.WALKABLE;
                PathPoint.costMalus = Math.max(0, PathPoint.costMalus);
                return PathPoint;
            }
            if (collision.apply(aabb.offset(0, -1, 0))) {
                return null;
            }
            PathPoint PathPoint = PathPointGetter.apply(pos);
            PathPoint.nodeType = PathNodeType.WALKABLE;
            PathPoint.costMalus = Math.max(0, PathPoint.costMalus);
            PathNodeType below = func.apply(pos.setPos(x, y - 1, z));
            if (below == PathNodeType.OPEN) {
                float mobPathingMalus;
                int fall = 0;
                BlockPos.Mutable lower = new BlockPos.Mutable(x, y, z);
                while (below == PathNodeType.OPEN) {
                    if (--y < 0) {
                        return null;
                    }
                    if (fall++ >= mob.getMaxFallHeight()) {
                        PathPoint PathPoint2 = PathPointGetter.apply(lower.setPos(x, y, z));
                        PathPoint2.nodeType = PathNodeType.BLOCKED;
                        PathPoint2.costMalus = -1.0f;
                        return PathPoint2;
                    }
                    below = func.apply(lower.setPos(x, y, z));
                    mobPathingMalus = mob.getPathPriority(below);
                    if (below != PathNodeType.OPEN && mobPathingMalus >= 0.0f) {
                        PathPoint = PathPointGetter.apply(lower.setPos(x, y, z));
                        PathPoint.nodeType = below;
                        PathPoint.costMalus = Math.max(PathPoint.costMalus, mobPathingMalus);
                        break;
                    }
                    if (!(mobPathingMalus < 0.0f)) continue;
                    return null;
                }
            }
            PathPoint.costMalus += 6;
            return PathPoint;
        } else if (stepModifier > 0) {
            BlockState above = getter.getBlockState(pos.setPos(x, y + 1, z));
            if (!breakableMap.computeIfAbsent(BlockPos.pack(x, y + 1, z), p -> canBreak(above, pos, mob)))
                return null;
            AxisAlignedBB aabb = createAxisAlignedBBForPos(getter, x, y + 1, z, mob.getWidth() / 2.0, mob.getHeight());
            if (collision.apply(aabb)) {
                return null;
            }
            PathPoint PathPoint = PathPointGetter.apply(pos.setPos(x, y, z));
            PathPoint.costMalus = Math.max(0, PathPoint.costMalus);
            PathPoint.nodeType = PathNodeType.WALKABLE;
            PathPoint.costMalus += 6;
            return PathPoint;
        }
        return null;
    }

    public static PathPoint floatingPathPointModifier(MobEntity mob, IBlockReader getter, int x, int y, int z, Function<AxisAlignedBB, Boolean> collision, Function<BlockPos, PathPoint> PathPointGetter) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        BlockState state = getter.getBlockState(pos);
        if (canBreak(state, pos, mob)) {
            AxisAlignedBB aabb = createAxisAlignedBBForPos(getter, x, y, z, mob.getWidth() / 2.0, mob.getHeight());
            if (collision.apply(aabb)) {
                return null;
            }
            PathPoint PathPoint = PathPointGetter.apply(pos);
            PathPoint.nodeType = PathNodeType.WALKABLE;
            PathPoint.costMalus += 2;
            return PathPoint;
        }
        return null;
    }

    public static boolean noCollision(Region level, Entity entity, AxisAlignedBB aABB) {
        Iterable<VoxelShape> shapes = () -> new CustomBlockCollision(level, entity, aABB);
        for (VoxelShape voxelShape : shapes) {
            if (!voxelShape.isEmpty())
                return false;
        }
        if (entity != null) {
            VoxelShape voxelShape2 = VoxelShapeSpliterator.func_234877_a_(level.getWorldBorder(), aABB) ? level.getWorldBorder().getShape() : null;
            return voxelShape2 == null || !VoxelShapes.compare(voxelShape2, VoxelShapes.create(aABB), IBooleanFunction.AND);
        }
        return true;
    }

    public static AxisAlignedBB createAxisAlignedBBForPos(IBlockReader getter, int x, int y, int z, double widthHalf, double height) {
        double floor1 = WalkNodeProcessor.getGroundY(getter, new BlockPos(x, y + 1, z));
        return new AxisAlignedBB(x - widthHalf + 0.5, floor1 + 0.001, z - widthHalf + 0.5, x + widthHalf + 0.5, height + floor1, z + widthHalf + 0.5);
    }

    public static int createLadderPathPointFor(int PathPointID, PathPoint[] PathPoints, PathPoint origin, Function<BlockPos, PathPoint> PathPointGetter, IBlockReader getter, MobEntity mob) {
        BlockPos.Mutable pos = new BlockPos.Mutable(origin.x, origin.y + 1, origin.z);
        if (getter.getBlockState(pos).isLadder(mob.world, pos, mob)) {
            PathPoint PathPoint = PathPointGetter.apply(pos);
            if (PathPoint != null && !PathPoint.visited) {
                PathPoint.costMalus = 0;
                PathPoint.nodeType = PathNodeType.WALKABLE;
                if (PathPointID + 1 < PathPoints.length)
                    PathPoints[PathPointID++] = PathPoint;
            }
        }
        pos.setPos(pos.getX(), pos.getY() - 2, pos.getZ());
        if (getter.getBlockState(pos).isLadder(mob.world, pos, mob)) {
            PathPoint PathPoint = PathPointGetter.apply(pos);
            if (PathPoint != null && !PathPoint.visited) {
                PathPoint.costMalus = 0;
                PathPoint.nodeType = PathNodeType.WALKABLE;
                if (PathPointID + 1 < PathPoints.length)
                    PathPoints[PathPointID++] = PathPoint;
            }
        }
        return PathPointID;
    }

    private static boolean canBreak(BlockState state, BlockPos pos, MobEntity entity) {
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.world, ISelectionContext.forEntity(entity)) && (GeneralHelperMethods.canHarvest(state, entity.getHeldItemMainhand()) || GeneralHelperMethods.canHarvest(state, entity.getHeldItemOffhand()));
    }

    public static boolean canBreak(BlockPos pos, MobEntity entity) {
        BlockState state = entity.world.getBlockState(pos);
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.world, ISelectionContext.forEntity(entity)) && (GeneralHelperMethods.canHarvest(state, entity.getHeldItemMainhand()) || GeneralHelperMethods.canHarvest(state, entity.getHeldItemOffhand()));
    }
}
