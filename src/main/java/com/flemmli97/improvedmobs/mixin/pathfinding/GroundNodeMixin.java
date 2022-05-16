package com.flemmli97.improvedmobs.mixin.pathfinding;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.utils.INodeBreakable;
import com.flemmli97.improvedmobs.utils.PathFindingUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {WalkNodeProcessor.class})
public abstract class GroundNodeMixin extends NodeProcessor {

    @Unique
    private final Object2BooleanMap<AxisAlignedBB> collisionBreakableCache = new Object2BooleanOpenHashMap<>();
    @Unique
    private final Object2BooleanMap<Long> breakableMap = new Object2BooleanOpenHashMap<>();

    @Shadow
    private Object2BooleanMap<AxisAlignedBB> field_237227_l_;

    @ModifyVariable(method = "func_222859_a", at = @At(value = "RETURN"), ordinal = 0)
    private int addAdditionalPoints(int nodeCounts, PathPoint[] points, PathPoint origin) {
        if (((INodeBreakable) this).canClimbLadder())
            return PathFindingUtils.createLadderPathPointFor(nodeCounts, points, origin, p -> this.func_237223_a_(p), this.blockaccess, this.entity);
        return nodeCounts;
    }

    @Inject(method = "postProcess", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.collisionBreakableCache.clear();
        this.breakableMap.clear();
    }

    @Inject(method = "getSafePoint", at = @At(value = "HEAD"), cancellable = true)
    private void breakableNodes(int x, int y, int z, int steps, double groundY, Direction direction, PathNodeType blockPathTypes, CallbackInfoReturnable<PathPoint> info) {
        if (!((INodeBreakable) this).canBreakBlocks())
            return;
        PathPoint node = PathFindingUtils.notFloatingPathPointModifier(this.entity, this.blockaccess, x, y, z, steps, direction, blockPathTypes,
                pos -> this.getNodeType(this.entity, pos.getX(), pos.getY(), pos.getZ()),
                aabb -> this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.blockaccess, this.entity, aabb)),
                aabb -> this.field_237227_l_.computeIfAbsent(aabb, object -> !this.blockaccess.hasNoCollisions(this.entity, aabb)),
                p -> this.func_237223_a_(p), this.breakableMap);
        if (node != null) {
            info.setReturnValue(node);
            info.cancel();
        }
    }

    @Redirect(method = "collectSurroundingNodeTypes", at = @At(value = "INVOKE", target = "Lnet/minecraft/pathfinding/WalkNodeProcessor;getFloorNodeType(Lnet/minecraft/world/IBlockReader;III)Lnet/minecraft/pathfinding/PathNodeType;"))
    private PathNodeType breakable(WalkNodeProcessor nodeEvaluator, IBlockReader getter, int x, int y, int z) {
        if (!((INodeBreakable) this).canBreakBlocks())
            return nodeEvaluator.getFloorNodeType(getter, x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = getter.getBlockState(pos);
        if (this.breakableMap.computeIfAbsent(BlockPos.pack(x, y, z), l -> Config.CommonConfig.breakableBlocks.canBreak(state, pos, getter, this.entity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(this.entity)))) {
            return PathNodeType.WALKABLE;
        }
        return nodeEvaluator.getFloorNodeType(getter, x, y, z);
    }

    @Inject(method = "func_237236_a_", at = @At(value = "HEAD"), cancellable = true)
    private void hasNoBreakableCollisions(AxisAlignedBB aabb, CallbackInfoReturnable<Boolean> info) {
        if (((INodeBreakable) this).canBreakBlocks()) {
            info.setReturnValue(this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.blockaccess, this.entity, aabb)));
            info.cancel();
        }
    }

    @Shadow
    protected abstract PathNodeType getNodeType(MobEntity entityIn, int x, int y, int z);
}
