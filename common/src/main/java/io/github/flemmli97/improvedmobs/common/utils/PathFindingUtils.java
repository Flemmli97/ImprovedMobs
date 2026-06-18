package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathFindingUtils {

    public static final ResourceLocation BREAKABLE = ImprovedMobs.modRes("breakable");
    public static final ResourceLocation LADDER = ImprovedMobs.modRes("ladder");

    public static boolean canBreakBlocks(Mob mob) {
        if (mob.getTarget() == null && !Config.CommonConfig.idleBreak)
            return false;
        return EntityFlags.get(mob).canBreakBlocks == EntityFlags.FlagType.TRUE
                && mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public static boolean canClimb(Mob mob) {
        return EntityFlags.get(mob).ladderClimber;
    }

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

    public static ResourceLocation getPathTypeNode(int x, int y, int z,
                                                   int entityWidth, int entityHeight, int entityDepth,
                                                   Function<BlockPos, ResourceLocation> pathTypeGetter) {
        Set<ResourceLocation> types = new HashSet<>();
        for (int i = 0; i < entityWidth; ++i) {
            for (int j = 0; j < entityHeight; ++j) {
                for (int k = 0; k < entityDepth; ++k) {
                    int l = i + x;
                    int i1 = j + y;
                    int j1 = k + z;
                    ResourceLocation pathType = pathTypeGetter.apply(new BlockPos(l, i1, j1));
                    types.add(pathType);
                    if (BREAKABLE.equals(pathType))
                        return pathType;
                }
            }
        }
        return types.contains(LADDER) ? LADDER : null;
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

    public static ResourceLocation pathType(BlockState state, BlockPos pos, Mob entity, @Nullable Direction direction, Predicate<AABB> collision, ResourceLocation... only) {
        Set<ResourceLocation> included = only.length == 0 ? Collections.emptySet() : Set.of(only);
        if ((included.isEmpty() || included.contains(LADDER)) && canClimb(entity) && CrossPlatformStuff.INSTANCE.isClimbable(state, entity, pos)) {
            double widthHalf = entity.getBbWidth() * 0.5;
            double height = entity.getBbHeight();
            AABB aabb = new AABB(pos.getX() + 0.5 - widthHalf + 0.5, pos.getY(), pos.getZ() + 0.5 - widthHalf + 0.5,
                    pos.getX() + widthHalf + 0.5, pos.getY() + height, pos.getZ() + 0.5 + widthHalf + 0.5);
            if (direction != null) {
                aabb = aabb.move(-direction.getStepX() + 0.5, 0, -direction.getStepZ() + 0.5);
            }
            if (!collision.test(aabb)) {
                return LADDER;
            }
        }
        if ((included.isEmpty() || included.contains(BREAKABLE)) && canBreakBlocks(entity) && Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.level(), entity, CollisionContext.of(entity))
                && (Utils.canHarvest(state, entity.getMainHandItem()) || Utils.canHarvest(state, entity.getOffhandItem()))) {
            return BREAKABLE;
        }
        return null;
    }
}
