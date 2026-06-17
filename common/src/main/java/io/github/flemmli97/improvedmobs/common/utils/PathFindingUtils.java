package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.function.Function;
import java.util.function.Predicate;

public class PathFindingUtils {

    public static Direction fromNodes(Node point, Node origin) {
        // Assumes the nodes if diagonal have the direction applied as direction + direction.clockwise().
        // See diagonal nodes of #WalkNodeEvaluator#getNeighbors
        int dx = origin.x + point.x;
        int dz = origin.z + point.z;
        if (dx >= 0 && dz < 0) {
            return Direction.NORTH;
        }
        if (dx <= 0 && dz > 0) {
            return Direction.SOUTH;
        }
        if (dx < 0 && dz == 0) {
            return Direction.WEST;
        }
        if (dx > 0) {
            return Direction.EAST;
        }
        return null;
    }

    public static boolean hasBreakable(int x, int y, int z,
                                       int entityWidth, int entityHeight, int entityDepth,
                                       Predicate<BlockPos> breakable) {
        for (int i = 0; i < entityWidth; ++i) {
            for (int j = 0; j < entityHeight; ++j) {
                for (int k = 0; k < entityDepth; ++k) {
                    int l = i + x;
                    int i1 = j + y;
                    int j1 = k + z;
                    if (breakable.test(new BlockPos(l, i1, j1)))
                        return true;
                }
            }
        }
        return false;
    }

    public static int createLadderNodeFor(int nodeID, Node[] nodes, Node origin, Function<BlockPos, Node> nodeGetter, Mob mob, Long2BooleanMap cache) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(origin.x, origin.y + 1, origin.z);
        if (cache.computeIfAbsent(pos.asLong(), l -> {
            BlockState state = mob.level().getBlockState(pos);
            return CrossPlatformStuff.INSTANCE.isClimbable(state, mob, pos);
        })) {
            Node node = nodeGetter.apply(pos);
            if (node != null && !node.closed) {
                if (nodeID + 1 < nodes.length)
                    nodes[nodeID++] = node;
            }
        }
        pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
        if (cache.computeIfAbsent(pos.asLong(), l -> {
            BlockState state = mob.level().getBlockState(pos);
            return CrossPlatformStuff.INSTANCE.isClimbable(state, mob, pos);
        })) {
            Node node = nodeGetter.apply(pos);
            if (node != null && !node.closed) {
                if (nodeID + 1 < nodes.length)
                    nodes[nodeID++] = node;
            }
        }
        return nodeID;
    }

    public static boolean canBreak(BlockState state, BlockPos pos, Mob entity) {
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.level(), entity, CollisionContext.of(entity)) && (Utils.canHarvest(state, entity.getMainHandItem()) || Utils.canHarvest(state, entity.getOffhandItem()));
    }
}
