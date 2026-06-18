package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import io.github.flemmli97.improvedmobs.mixinhelper.PathfindingContextExt;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = WalkNodeEvaluator.class)
public abstract class WalkNodeMixin extends NodeEvaluator implements NodeExtension {

    @Shadow
    @Final
    private Object2BooleanMap<AABB> collisionCache;

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
    private final Long2ObjectMap<ResourceLocation> improvedMobs$pathTypePosCache = new Long2ObjectOpenHashMap<>();
    @Unique
    private final Long2ObjectMap<ResourceLocation> improvedMobs$pathTypeNodePosCache = new Long2ObjectOpenHashMap<>();

    @Unique
    private final Long2ObjectMap<PathType> improvedMobs$defaultPathTypesCache = new Long2ObjectOpenHashMap<>();
    @Unique
    private boolean improvedMobs$useDefaultPathTypes = false;

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.improvedMobs$pathTypePosCache.clear();
        this.improvedMobs$pathTypeNodePosCache.clear();
        this.improvedMobs$defaultPathTypesCache.clear();
    }

    @ModifyReturnValue(method = "getNeighbors", at = @At(value = "RETURN"))
    private int addAdditionalPoints(int nodeCounts, Node[] points, Node origin,
                                    @Local(ordinal = 1) int jump, @Local(ordinal = 1) PathType pathType) {
        if (this.improvedMobs$canBreakBlocks()) {
            // Current nodes should be nodes in all 8 horizontal direction
            // For some reason local cannot find this value...
            double floor = this.getFloorLevel(new BlockPos(origin.x, origin.y, origin.z));
            int count = nodeCounts;
            for (int i = 0; i < count; i++) {
                Node point = points[i];
                if (point == null)
                    continue;
                Direction direction = PathFindingUtils.fromNodes(origin.x, origin.z, point.x, point.z);
                if (direction == null)
                    continue;
                // Check if this node was marked as a breakable node
                ResourceLocation type = this.improvedMob$pathTypeForNode(point.x, point.y, point.z);
                if (PathFindingUtils.BREAKABLE.equals(type)) {
                    float malus = Math.max(point.costMalus, 2);
                    // Try add the node above to allow entities to jump over breakable blocks instead of taking time to dig
                    Node node = this.findAcceptedNode(point.x, point.y + 1, point.z, jump, floor, direction, pathType);
                    if (this.isNeighborValid(node, origin) && node.y != point.y) {
                        if (PathFindingUtils.BREAKABLE.equals(this.improvedMob$pathTypeForNode(node.x, node.y, node.z))) {
                            node.costMalus = Math.max(node.costMalus, 2);
                        }
                        points[nodeCounts++] = node;
                    }
                    point.costMalus = malus;
                }
                // Try add the node below to allow entity to also dig down
                Node node = this.findAcceptedNode(point.x, point.y - 1, point.z, jump, floor, direction, pathType);
                if (this.isNeighborValid(node, origin) && node.y != point.y) {
                    if (PathFindingUtils.BREAKABLE.equals(this.improvedMob$pathTypeForNode(node.x, node.y, node.z))) {
                        node.costMalus = Math.max(node.costMalus, 2);
                    }
                    points[nodeCounts++] = node;
                }
            }
        }
        int y = origin.y + 1;
        if (PathFindingUtils.LADDER.equals(this.improvedMob$pathTypeForNode(origin.x, y, origin.z)) && !this.improvedMob$collidesFrom(origin.x, y, origin.z, Direction.UP)) {
            Node node = this.getNodeAndUpdateCostToMax(origin.x, y, origin.z, PathType.WALKABLE, 0);
            if (node != null && !node.closed) {
                node.costMalus = PathType.WALKABLE.getMalus();
                points[nodeCounts++] = node;
            }
        }
        y = origin.y - 1;
        if (PathFindingUtils.LADDER.equals(this.improvedMob$pathTypeForNode(origin.x, y, origin.z)) && !this.improvedMob$collidesFrom(origin.x, y, origin.z, Direction.DOWN)) {
            Node node = this.getNodeAndUpdateCostToMax(origin.x, y, origin.z, PathType.WALKABLE, 0);
            if (node != null && !node.closed) {
                node.costMalus = PathType.WALKABLE.getMalus();
                points[nodeCounts++] = node;
            }
        }
        return nodeCounts;
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"))
    private void onFindingAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel,
                                       Direction direction, PathType pathType, CallbackInfoReturnable<Node> info) {
        ((PathfindingContextExt) this.currentContext).improvedMobs$setPathHandler(this.improvedMobs$canBreakBlocks()
                ? new PathfindingContextExt.AdditionalPathTypeHandler(y, pos -> this.improvedMobs$pathTypeOf(pos) != null) : null);
    }

    @ModifyReturnValue(method = "findAcceptedNode", at = @At("RETURN"))
    private Node updateNode(Node original, @Local(argsOnly = true) Direction direction) {
        if (original != null && PathFindingUtils.LADDER.equals(this.improvedMob$pathTypeForNode(original.x, original.y, original.z))) {
            if (this.improvedMob$collidesFrom(original.x, original.y, original.z, direction)) {
                // This node should not be reachable from this direction
                original.type = PathType.BLOCKED;
                original.costMalus = -1;
            }
        }
        return original;
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
        if (this.improvedMobs$canBreakBlocks() && this.improvedMobs$useDefaultPathTypes) {
            PathfindingContextExt.AdditionalPathTypeHandler current = ((PathfindingContextExt) this.currentContext).improvedMobs$getPathHandler();
            ((PathfindingContextExt) this.currentContext).improvedMobs$setPathHandler(null);
            info.setReturnValue(this.improvedMobs$defaultPathTypesCache.computeIfAbsent(BlockPos.asLong(x, y, z), l -> this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob)));
            ((PathfindingContextExt) this.currentContext).improvedMobs$setPathHandler(current);
        }
    }

    @Unique
    private ResourceLocation improvedMob$pathTypeForNode(int x, int y, int z) {
        return this.improvedMobs$pathTypeNodePosCache.computeIfAbsent(BlockPos.asLong(x, y, z), l -> PathFindingUtils.getPathTypeNode(x, y, z,
                this.entityWidth, this.entityHeight, this.entityDepth, this::improvedMobs$pathTypeOf));
    }

    @Unique
    private boolean improvedMob$collidesFrom(int x, int y, int z, Direction direction) {
        return PathFindingUtils.collidesFromSide(x, y, z, this.mob, direction,
                aabb -> this.collisionCache.computeIfAbsent(aabb, object -> !this.currentContext.level().noBlockCollision(this.mob, aabb)));
    }

    @Override
    public ResourceLocation improvedMobs$pathTypeOf(BlockPos pos, ResourceLocation... only) {
        return this.improvedMobs$pathTypePosCache.computeIfAbsent(pos.asLong(),
                p -> PathFindingUtils.pathType(this.currentContext.getBlockState(pos), pos, this.mob));
    }
}
