package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import io.github.flemmli97.improvedmobs.mixinhelper.PathfindingContextExt;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = WalkNodeEvaluator.class)
public abstract class WalkNodeMixin extends NodeEvaluator {

    @Shadow
    protected abstract double getFloorLevel(BlockPos pos);

    @Shadow
    protected abstract Node getNodeAndUpdateCostToMax(int x, int y, int z, PathType pathType, float malus);

    @Shadow
    @Nullable
    protected abstract Node findAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel, Direction direction, PathType pathType);

    @Shadow
    protected abstract boolean isNeighborValid(@org.jetbrains.annotations.Nullable Node neighbor, Node node);

    @Unique
    private final Long2BooleanMap improvedMobs$ladderMap = new Long2BooleanOpenHashMap();
    @Unique
    private final Long2BooleanMap improvedMobs$breakablePosCache = new Long2BooleanOpenHashMap();
    @Unique
    private final Long2ObjectMap<PathType> improvedMobs$defaultPathTypesCache = new Long2ObjectOpenHashMap<>();
    @Unique
    private boolean improvedMobs$useDefaultPathTypes = false;

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.improvedMobs$ladderMap.clear();
        this.improvedMobs$breakablePosCache.clear();
        this.improvedMobs$defaultPathTypesCache.clear();
    }

    @ModifyReturnValue(method = "getNeighbors", at = @At(value = "RETURN"))
    private int addAdditionalPoints(int nodeCounts, Node[] points, Node origin,
                                    @Local(ordinal = 1) int jump, @Local(ordinal = 1) PathType pathType) {
        if (((NodeExtension) this).improvedMobs$canBreakBlocks()) {
            // Current nodes should be nodes in all 8 horizontal direction
            // For some reason local cannot find this value...
            double floor = this.getFloorLevel(new BlockPos(origin.x, origin.y, origin.z));
            int count = nodeCounts;
            for (int i = 0; i < count; i++) {
                Node point = points[i];
                if (point == null)
                    continue;
                Direction direction = PathFindingUtils.fromNodes(point, origin);
                if (direction == null)
                    continue;
                // Check if this node was marked as a breakable node
                if (this.improvedMobs$hasBreakableBlocksAt(new BlockPos(point.x, point.y, point.z))) {
                    float malus = Math.max(point.costMalus, 2);
                    // Try add the node above to allow entities to jump over breakable blocks instead of taking time to dig
                    Node node = this.findAcceptedNode(point.x, point.y + 1, point.z, jump, floor, direction, pathType);
                    if (this.isNeighborValid(node, origin) && node.y != point.y) {
                        if (this.improvedMobs$hasBreakableBlocksAt(new BlockPos(node.x, node.y, node.z)))
                            node.costMalus = Math.max(node.costMalus, 2);
                        points[nodeCounts++] = node;
                    }
                    point.costMalus = malus;
                }
                // Try add the node below to allow entity to also dig down
                Node node = this.findAcceptedNode(point.x, point.y - 1, point.z, jump, floor, direction, pathType);
                if (this.isNeighborValid(node, origin) && node.y != point.y) {
                    if (this.improvedMobs$hasBreakableBlocksAt(new BlockPos(node.x, node.y, node.z)))
                        node.costMalus = Math.max(node.costMalus, 2);
                    points[nodeCounts++] = node;
                }
            }
        }
        if (((NodeExtension) this).improvedMobs$canClimb())
            nodeCounts = PathFindingUtils.createLadderNodeFor(nodeCounts, points, origin,
                    p -> this.getNodeAndUpdateCostToMax(p.getX(), p.getY(), p.getZ(), PathType.WALKABLE, 0), this.mob, this.improvedMobs$ladderMap);
        return nodeCounts;
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"), cancellable = true)
    private void onFindingAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel,
                                       Direction direction, PathType pathType, CallbackInfoReturnable<Node> info) {
        if (((NodeExtension) this).improvedMobs$canClimb() && this.improvedMobs$ladderMap.computeIfAbsent(BlockPos.asLong(x, y, z), l -> {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.currentContext.getBlockState(pos);
            return CrossPlatformStuff.INSTANCE.isClimbable(state, this.mob, pos);
        })) {
            info.setReturnValue(this.getNodeAndUpdateCostToMax(x, y, z, PathType.WALKABLE, 0));
        }
        ((PathfindingContextExt) this.currentContext).improvedMobs$setBreakingHandler(((NodeExtension) this).improvedMobs$canBreakBlocks()
                ? new PathfindingContextExt.BreakingHandler(y, this::improvedMobs$isBlockBreakable) : null);
    }

    @WrapMethod(method = "tryFindFirstNonWaterBelow")
    private Node wrapWaterNode(int x, int y, int z, Node node, Operation<Node> original) {
        this.improvedMobs$useDefaultPathTypes = true;
        Node res = original.call(x, y, z, node);
        this.improvedMobs$useDefaultPathTypes = false;
        return res;
    }

    @WrapMethod(method = "tryFindFirstGroundNodeBelow")
    private Node wrapGroundNode(int x, int y, int z, Operation<Node> original) {
        this.improvedMobs$useDefaultPathTypes = true;
        Node node = original.call(x, y, z);
        this.improvedMobs$useDefaultPathTypes = false;
        return node;
    }

    @Inject(method = "getCachedPathType", at = @At("HEAD"), cancellable = true)
    private void onGetPathType(int x, int y, int z, CallbackInfoReturnable<PathType> info) {
        if (((NodeExtension) this).improvedMobs$canBreakBlocks() && this.improvedMobs$useDefaultPathTypes) {
            PathfindingContextExt.BreakingHandler current = ((PathfindingContextExt) this.currentContext).improvedMobs$getBreakingHandler();
            ((PathfindingContextExt) this.currentContext).improvedMobs$setBreakingHandler(null);
            info.setReturnValue(this.improvedMobs$defaultPathTypesCache.computeIfAbsent(BlockPos.asLong(x, y, z), l -> this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob)));
            ((PathfindingContextExt) this.currentContext).improvedMobs$setBreakingHandler(current);
        }
    }

    @Unique
    private boolean improvedMobs$hasBreakableBlocksAt(BlockPos pos) {
        return PathFindingUtils.hasBreakable(pos.getX(), pos.getY(), pos.getZ(),
                this.entityWidth, this.entityHeight, this.entityDepth, this::improvedMobs$isBlockBreakable);
    }

    @Unique
    private boolean improvedMobs$isBlockBreakable(BlockPos pos) {
        return this.improvedMobs$breakablePosCache.computeIfAbsent(pos.asLong(),
                p -> PathFindingUtils.canBreak(this.currentContext.getBlockState(pos), pos, this.mob));
    }
}
