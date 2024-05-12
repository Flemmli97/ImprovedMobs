package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.mixinhelper.INodeBreakable;
import io.github.flemmli97.improvedmobs.utils.PathFindingUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WalkNodeEvaluator.class)
public abstract class GroundNodeMixin extends NodeEvaluator {

    @Unique
    private final Object2BooleanMap<AABB> collisionBreakableCache = new Object2BooleanOpenHashMap<>();
    @Unique
    private final Object2BooleanMap<Long> breakableMap = new Object2BooleanOpenHashMap<>();

    @Shadow
    private Object2BooleanMap<AABB> collisionCache;

    @ModifyVariable(method = "getNeighbors", at = @At(value = "RETURN"), ordinal = 0)
    private int addAdditionalPoints(int nodeCounts, Node[] points, Node origin) {
        if (((INodeBreakable) this).canClimbLadder())
            return PathFindingUtils.createLadderNodeFor(nodeCounts, points, origin, p -> this.getNode(p), this.currentContext.level(), this.mob);
        return nodeCounts;
    }

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.collisionBreakableCache.clear();
        this.breakableMap.clear();
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"), cancellable = true)
    private void breakableNodes(int x, int y, int z, int steps, double groundY, Direction direction, PathType PathType, CallbackInfoReturnable<Node> info) {
        if (!((INodeBreakable) this).canBreakBlocks())
            return;
        Node node = PathFindingUtils.notFloatingNodeModifier(this.mob, this.currentContext.level(), x, y, z, steps, direction, PathType,
                pos -> this.getCachedPathType(pos.getX(), pos.getY(), pos.getZ()),
                aabb -> this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.currentContext.level(), this.mob, aabb)),
                aabb -> this.collisionCache.computeIfAbsent(aabb, object -> !this.currentContext.level().noCollision(this.mob, aabb)),
                p -> this.getNode(p), this.breakableMap);
        if (node != null) {
            info.setReturnValue(node);
            info.cancel();
        }
    }

    @WrapOperation(method = "getPathTypeWithinMobBB", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getPathType(Lnet/minecraft/world/level/pathfinder/PathfindingContext;III)Lnet/minecraft/world/level/pathfinder/PathType;"))
    private PathType breakable(WalkNodeEvaluator nodeEvaluator, PathfindingContext ctx, int x, int y, int z, Operation<PathType> original) {
        if (!((INodeBreakable) this).canBreakBlocks())
            return original.call(nodeEvaluator, ctx, x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = ctx.getBlockState(pos);
        if (this.breakableMap.computeIfAbsent(BlockPos.asLong(x, y, z), l -> Config.CommonConfig.breakableBlocks.canBreak(state, pos, ctx.level(), this.mob, this.mob == null ? CollisionContext.empty() : CollisionContext.of(this.mob)))) {
            return PathType.WALKABLE;
        }
        return original.call(nodeEvaluator, ctx, x, y, z);
    }

    @Inject(method = "hasCollisions", at = @At(value = "HEAD"), cancellable = true)
    private void hasNoBreakableCollisions(AABB aabb, CallbackInfoReturnable<Boolean> info) {
        if (((INodeBreakable) this).canBreakBlocks()) {
            info.setReturnValue(this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.currentContext.level(), this.mob, aabb)));
            info.cancel();
        }
    }

    @Shadow
    protected abstract PathType getCachedPathType(int i, int j, int k);
}
