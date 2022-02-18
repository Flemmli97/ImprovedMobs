package io.github.flemmli97.improvedmobs.utils;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class PathFindingUtils {

    public static Node notFloatingNodeModifier(Mob mob, BlockGetter getter, int x, int y, int z, int stepModifier, Direction dir, BlockPathTypes standingType,
                                               Function<BlockPos, BlockPathTypes> func, Function<AABB, Boolean> collision, Function<AABB, Boolean> collisionDefault,
                                               Function<BlockPos, Node> nodeGetter, Object2BooleanMap<Long> breakableMap) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = getter.getBlockState(pos);
        if (breakableMap.computeIfAbsent(BlockPos.asLong(x, y, z), p -> canBreak(state, pos, mob))) {
            AABB aabb = createAABBForPos(getter, x, y, z, mob.getBbWidth() / 2.0, mob.getBbHeight());
            if (stepModifier > 0 && !collisionDefault.apply(aabb.expandTowards(-dir.getStepX(), 0, -dir.getStepZ()))) {
                Node node = nodeGetter.apply(pos.set(x, y + 1, z));
                node.type = BlockPathTypes.WALKABLE;
                node.costMalus = Math.max(0, node.costMalus);
                return node;
            }
            if (collision.apply(aabb.move(0, -1, 0))) {
                return null;
            }
            Node node = nodeGetter.apply(pos);
            node.type = BlockPathTypes.WALKABLE;
            node.costMalus = Math.max(0, node.costMalus);
            BlockPathTypes below = func.apply(pos.set(x, y - 1, z));
            if (below == BlockPathTypes.OPEN) {
                float mobPathingMalus;
                int fall = 0;
                BlockPos.MutableBlockPos lower = new BlockPos.MutableBlockPos(x, y, z);
                while (below == BlockPathTypes.OPEN) {
                    if (--y < mob.level.getMinBuildHeight()) {
                        return null;
                    }
                    if (fall++ >= mob.getMaxFallDistance()) {
                        Node node2 = nodeGetter.apply(lower.set(x, y, z));
                        node2.type = BlockPathTypes.BLOCKED;
                        node2.costMalus = -1.0f;
                        return node2;
                    }
                    below = func.apply(lower.set(x, y, z));
                    mobPathingMalus = mob.getPathfindingMalus(below);
                    if (below != BlockPathTypes.OPEN && mobPathingMalus >= 0.0f) {
                        node = nodeGetter.apply(lower.set(x, y, z));
                        node.type = below;
                        node.costMalus = Math.max(node.costMalus, mobPathingMalus);
                        break;
                    }
                    if (!(mobPathingMalus < 0.0f)) continue;
                    return null;
                }
            }
            node.costMalus += 6;
            return node;
        } else if (stepModifier > 0) {
            BlockState above = getter.getBlockState(pos.set(x, y + 1, z));
            if (!breakableMap.computeIfAbsent(BlockPos.asLong(x, y + 1, z), p -> canBreak(above, pos, mob)))
                return null;
            AABB aabb = createAABBForPos(getter, x, y + 1, z, mob.getBbWidth() / 2.0, mob.getBbHeight());
            if (collision.apply(aabb)) {
                return null;
            }
            Node node = nodeGetter.apply(pos.set(x, y, z));
            node.costMalus = Math.max(0, node.costMalus);
            node.type = BlockPathTypes.WALKABLE;
            node.costMalus += 6;
            return node;
        }
        return null;
    }

    public static Node floatingNodeModifier(Mob mob, BlockGetter getter, int x, int y, int z, Function<AABB, Boolean> collision, Function<BlockPos, Node> nodeGetter) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = getter.getBlockState(pos);
        if (canBreak(state, pos, mob)) {
            AABB aabb = createAABBForPos(getter, x, y, z, mob.getBbWidth() / 2.0, mob.getBbHeight());
            if (collision.apply(aabb)) {
                return null;
            }
            Node node = nodeGetter.apply(pos);
            node.type = BlockPathTypes.WALKABLE;
            node.costMalus += 2;
            return node;
        }
        return null;
    }

    public static boolean noCollision(PathNavigationRegion level, Entity entity, AABB aABB) {
        Iterable<VoxelShape> shapes = () -> new CustomBlockCollision(level, entity, aABB);
        for (VoxelShape voxelShape : shapes) {
            if (!voxelShape.isEmpty())
                return false;
        }
        if (entity != null) {
            WorldBorder worldBorder = level.getWorldBorder();
            VoxelShape voxelShape2 = worldBorder.isInsideCloseToBorder(entity, aABB) ? worldBorder.getCollisionShape() : null;
            return voxelShape2 == null || !Shapes.joinIsNotEmpty(voxelShape2, Shapes.create(aABB), BooleanOp.AND);
        }
        return true;
    }

    public static AABB createAABBForPos(BlockGetter getter, int x, int y, int z, double widthHalf, double height) {
        double floor1 = WalkNodeEvaluator.getFloorLevel(getter, new BlockPos(x, y + 1, z));
        return new AABB(x - widthHalf + 0.5, floor1 + 0.001, z - widthHalf + 0.5, x + widthHalf + 0.5, height + floor1, z + widthHalf + 0.5);
    }

    public static int createLadderNodeFor(int nodeID, Node[] nodes, Node origin, Function<BlockPos, Node> nodeGetter, BlockGetter getter, Mob mob) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(origin.x, origin.y + 1, origin.z);
        if (CrossPlatformStuff.instance().isLadder(getter.getBlockState(pos), mob, pos)) {
            Node node = nodeGetter.apply(pos);
            if (node != null && !node.closed) {
                node.costMalus = 0;
                node.type = BlockPathTypes.WALKABLE;
                if (nodeID + 1 < nodes.length)
                    nodes[nodeID++] = node;
            }
        }
        pos.set(pos.getX(), pos.getY() - 2, pos.getZ());
        if (CrossPlatformStuff.instance().isLadder(getter.getBlockState(pos), mob, pos)) {
            Node node = nodeGetter.apply(pos);
            if (node != null && !node.closed) {
                node.costMalus = 0;
                node.type = BlockPathTypes.WALKABLE;
                if (nodeID + 1 < nodes.length)
                    nodes[nodeID++] = node;
            }
        }
        return nodeID;
    }

    private static boolean canBreak(BlockState state, BlockPos pos, Mob entity) {
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.level, CollisionContext.of(entity)) && (Utils.canHarvest(state, entity.getMainHandItem()) || Utils.canHarvest(state, entity.getOffhandItem()));
    }

    public static boolean canBreak(BlockPos pos, Mob entity) {
        BlockState state = entity.level.getBlockState(pos);
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.level, CollisionContext.of(entity)) && (Utils.canHarvest(state, entity.getMainHandItem()) || Utils.canHarvest(state, entity.getOffhandItem()));
    }
}
