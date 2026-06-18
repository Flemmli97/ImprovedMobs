package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathFindingUtils {

    public static final ResourceLocation BREAKABLE = ImprovedMobs.modRes("breakable");
    public static final ResourceLocation LADDER = ImprovedMobs.modRes("ladder");

    public static Direction fromNodes(int x1, int z1, int x2, int z2) {
        // Assumes the nodes if diagonal have the direction applied as direction + direction.clockwise().
        // See diagonal nodes of #WalkNodeEvaluator#getNeighbors
        int dx = x1 + x2;
        int dz = z1 + z2;
        if (dx >= 0 && dz < 0) {
            return Direction.NORTH;
        }
        if (dx <= 0 && dz > 0) {
            return Direction.SOUTH;
        }
        if (dx < 0 && dz == 0) {
            return Direction.WEST;
        }
        return Direction.EAST;
    }

    public static ResourceLocation getPathTypeNode(int x, int y, int z,
                                                   int entityWidth, int entityHeight, int entityDepth,
                                                   Function<BlockPos, ResourceLocation> pathTypeGetter) {
        Set<ResourceLocation> types = new HashSet<>();
        for (int dx = 0; dx < entityWidth; ++dx) {
            for (int dy = 0; dy < entityHeight; ++dy) {
                for (int dz = 0; dz < entityDepth; ++dz) {
                    int xN = dx + x;
                    int yN = dy + y;
                    int zN = dz + z;
                    ResourceLocation pathType = pathTypeGetter.apply(new BlockPos(xN, yN, zN));
                    types.add(pathType);
                    if (BREAKABLE.equals(pathType))
                        return pathType;
                }
            }
        }
        return types.contains(LADDER) ? LADDER : null;
    }

    public static boolean collidesFromSide(int x, int y, int z, Mob entity, Direction direction, Predicate<AABB> collision) {
        double widthHalf = entity.getBbWidth() * 0.5;
        double height = entity.getBbHeight();
        AABB aabb = new AABB(x + 0.5 - widthHalf, y, z + 0.5 - widthHalf,
                x + 0.5 + widthHalf, y + height, z + 0.5 + widthHalf)
                .move(-direction.getStepX() * 0.5, 0, -direction.getStepZ() * 0.5);
        return collision.test(aabb);
    }

    public static ResourceLocation pathType(BlockGetter level, BlockState state, BlockPos pos, Mob entity, ResourceLocation... only) {
        Set<ResourceLocation> included = only.length == 0 ? Collections.emptySet() : Set.of(only);
        if ((included.isEmpty() || included.contains(LADDER)) && Utils.canClimb(entity) && CrossPlatformStuff.INSTANCE.isClimbable(state, entity, pos)) {
            return LADDER;
        }
        if ((included.isEmpty() || included.contains(BREAKABLE)) && Utils.canBreakBlocks(entity) && Utils.canBreakState(entity, state)) {
            // No collision states can just walk through so let vanilla handle them
            // Previously in BreakableBlocks#canBreak but now moved here to let the blocks be broken with the goals
            if (state.getCollisionShape(level, pos, CollisionContext.of(entity)).isEmpty()) {
                return null;
            }
            return BREAKABLE;
        }
        return null;
    }
}
