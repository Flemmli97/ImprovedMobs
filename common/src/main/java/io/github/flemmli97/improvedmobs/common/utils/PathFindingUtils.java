package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class PathFindingUtils {

    public static Node handleBreakableNode(Mob mob, BlockGetter getter, int x, int y, int z, Direction dir,
                                           Node origin, Function<AABB, Boolean> collision, Function<AABB, Boolean> collisionDefault,
                                           Function<BlockPos, Node> nodeGetter) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        AABB aabb = createAABBForPos(getter, x, y, z, mob.getBbWidth() / 2.0, mob.getBbHeight());
        // The mob can jump over it. Ignore
        boolean defaultCollides = collisionDefault.apply(aabb.expandTowards(-dir.getStepX(), 0, -dir.getStepZ()));
        if (!defaultCollides) {
            return null;
        }
        boolean breakableCollides = collision.apply(aabb.expandTowards(-dir.getStepX(), 0, -dir.getStepZ()));
        if (!breakableCollides) {
            if (origin.y != y) {
                pos.set(x, origin.y, z);
            }
            return nodeGetter.apply(pos);
        }
        return null;
    }

    public static boolean noCollision(CollisionGetter level, LivingEntity entity, AABB aABB, boolean breakable, boolean ladder) {
        Iterable<VoxelShape> shapes = () -> new CustomBlockCollision(level, entity, aABB, breakable, ladder);
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

    public static AABB createAABBForPos(int x, int y, int z, double widthHalf, double height) {
        return new AABB(x - widthHalf + 0.5, y, z - widthHalf + 0.5, x + widthHalf + 0.5, y + height, z + widthHalf + 0.5);
    }

    public static AABB createAABBForPos(BlockGetter getter, int x, int y, int z, double widthHalf, double height) {
        double floor1 = WalkNodeEvaluator.getFloorLevel(getter, new BlockPos(x, y + 1, z));
        return new AABB(x - widthHalf + 0.5, floor1 + 0.001, z - widthHalf + 0.5, x + widthHalf + 0.5, height + floor1, z + widthHalf + 0.5);
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

    public static int createBreakableNodeBelow(int nodeID, Node[] nodes, Node origin, Function<BlockPos, Node> nodeGetter, Mob mob, Long2BooleanMap cache) {
        BlockPos pos = new BlockPos(origin.x, origin.y - 1, origin.z);
        if (cache.computeIfAbsent(pos.asLong(), l -> {
            BlockState state = mob.level().getBlockState(pos);
            return canBreak(state, pos, mob);
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
