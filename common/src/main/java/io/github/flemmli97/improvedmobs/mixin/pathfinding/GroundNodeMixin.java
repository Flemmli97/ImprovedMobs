package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.utils.PathFindingUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {WalkNodeEvaluator.class})
public abstract class GroundNodeMixin extends NodeEvaluator {

    @Unique
    private final Object2BooleanMap<AABB> collisionBreakableCache = new Object2BooleanOpenHashMap<>();
    @Unique
    private final Object2BooleanMap<Long> breakableMap = new Object2BooleanOpenHashMap<>();

    @Shadow
    private Object2BooleanMap<AABB> collisionCache;

    @ModifyVariable(method = "getNeighbors", at = @At(value = "RETURN"), ordinal = 0)
    private int addAdditionalPoints(int nodeCounts, Node[] points, Node origin) {
        return PathFindingUtils.createLadderNodeFor(nodeCounts, points, origin, p -> this.getNode(p), this.level, this.mob);
    }

    @Inject(method = "done", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.collisionBreakableCache.clear();
        this.breakableMap.clear();
    }

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"), cancellable = true)
    private void breakableNodes(int x, int y, int z, int steps, double groundY, Direction direction, BlockPathTypes blockPathTypes, CallbackInfoReturnable<Node> info) {
        Node node = PathFindingUtils.notFloatingNodeModifier(this.mob, this.level, x, y, z, steps, direction, blockPathTypes,
                pos -> this.getCachedBlockType(this.mob, pos.getX(), pos.getY(), pos.getZ()),
                aabb -> this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.level, this.mob, aabb)),
                aabb -> this.collisionCache.computeIfAbsent(aabb, object -> !this.level.noCollision(this.mob, aabb)),
                p -> this.getNode(p), this.breakableMap);
        if (node != null) {
            info.setReturnValue(node);
            info.cancel();
        }
    }

    @Redirect(method = "getBlockPathTypes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;"))
    private BlockPathTypes breakable(WalkNodeEvaluator nodeEvaluator, BlockGetter getter, int x, int y, int z) {
        BlockState state = getter.getBlockState(new BlockPos.MutableBlockPos(x, y, z));
        if (this.breakableMap.computeIfAbsent(BlockPos.asLong(x, y, z), pos -> Config.CommonConfig.breakableBlocks.canBreak(state))) {
            return BlockPathTypes.WALKABLE;
        }
        return nodeEvaluator.getBlockPathType(getter, x, y, z);
    }

    @Inject(method = "hasCollisions", at = @At(value = "HEAD"), cancellable = true)
    private void hasNoBreakableCollisions(AABB aabb, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.level, this.mob, aabb)));
        info.cancel();
    }

    @Shadow
    protected abstract BlockPathTypes getCachedBlockType(Mob mob, int i, int j, int k);
}
