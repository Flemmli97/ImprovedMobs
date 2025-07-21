package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
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

@Mixin(value = WalkNodeEvaluator.class)
public abstract class WalkNodeMixin extends NodeEvaluator {

    @Shadow
    @Final
    private Object2BooleanMap<AABB> collisionCache;

    @Shadow
    protected abstract Node getNodeAndUpdateCostToMax(int x, int y, int z, PathType pathType, float malus);

    @Unique
    private final Object2BooleanMap<AABB> improvedMobs$collisionBreakableCache = new Object2BooleanOpenHashMap<>();
    @Unique
    private final Long2BooleanMap improvedMobs$breakableMap = new Long2BooleanOpenHashMap();
    @Unique
    private final Long2BooleanMap improvedMobs$ladderMap = new Long2BooleanOpenHashMap();
    @Unique
    private Node improvedMobs$origin;

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.improvedMobs$collisionBreakableCache.clear();
        this.improvedMobs$ladderMap.clear();
        this.improvedMobs$breakableMap.clear();
        this.improvedMobs$origin = null;
    }

    @Inject(method = "getNeighbors", at = @At(value = "HEAD"))
    private void addAdditionalPoints(Node[] nodes, Node origin, CallbackInfoReturnable<Integer> info) {
        this.improvedMobs$origin = origin;
    }

    @ModifyReturnValue(method = "getNeighbors", at = @At(value = "RETURN"))
    private int addAdditionalPoints(int nodeCounts, Node[] points, Node origin) {
        if (((NodeExtension) this).improvedMobs$canClimb())
            nodeCounts = PathFindingUtils.createLadderNodeFor(nodeCounts, points, origin,
                    p -> this.getNodeAndUpdateCostToMax(p.getX(), p.getY(), p.getZ(), PathType.WALKABLE, 0), this.mob, this.improvedMobs$ladderMap);
        if (((NodeExtension) this).improvedMobs$canBreakBlocks())
            nodeCounts = PathFindingUtils.createBreakableNodeBelow(nodeCounts, points, origin,
                    p -> this.getNodeAndUpdateCostToMax(p.getX(), p.getY(), p.getZ(), PathType.WALKABLE, 2), this.mob, this.improvedMobs$breakableMap);
        return nodeCounts;
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"), cancellable = true)
    private void climbableNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel,
                               Direction direction, PathType pathType, CallbackInfoReturnable<Node> info) {
        if (((NodeExtension) this).improvedMobs$canClimb() && this.improvedMobs$ladderMap.computeIfAbsent(BlockPos.asLong(x, y, z), l -> {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.currentContext.getBlockState(pos);
            return CrossPlatformStuff.INSTANCE.isClimbable(state, this.mob, pos);
        })) {
            info.setReturnValue(this.getNodeAndUpdateCostToMax(x, y, z, PathType.WALKABLE, 0));
        }
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getCachedPathType(III)Lnet/minecraft/world/level/pathfinder/PathType;"), cancellable = true)
    private void breakableNodes(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel,
                                Direction direction, PathType pathType, CallbackInfoReturnable<Node> info) {
        if (!((NodeExtension) this).improvedMobs$canBreakBlocks())
            return;
        Node node = PathFindingUtils.handleBreakableNode(this.mob, this.currentContext.level(), x, y, z, direction,
                this.improvedMobs$origin,
                aabb -> this.improvedMobs$collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.currentContext.level(), this.mob, aabb, true, ((NodeExtension) this).improvedMobs$canClimb())),
                aabb -> this.collisionCache.computeIfAbsent(aabb, object -> !this.currentContext.level().noCollision(this.mob, aabb)),
                p -> this.getNodeAndUpdateCostToMax(p.getX(), p.getY(), p.getZ(), PathType.WALKABLE, 2));
        if (node != null) {
            info.setReturnValue(node);
        }
    }
}
