package com.flemmli97.improvedmobs.mixin.pathfinding;

import com.flemmli97.improvedmobs.utils.INodeBreakable;
import com.flemmli97.improvedmobs.utils.PathFindingUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {FlyingNodeProcessor.class})
public abstract class FlyNodeMixin extends NodeProcessor {

    @Unique
    private final Object2BooleanMap<AxisAlignedBB> collisionBreakableCache = new Object2BooleanOpenHashMap<>();

    @Inject(method = "postProcess", at = @At(value = "RETURN"))
    private void clearStuff(CallbackInfo info) {
        this.collisionBreakableCache.clear();
    }

    @Inject(method = "openPoint", at = @At(value = "HEAD"), cancellable = true)
    private void breakableNodes(int x, int y, int z, CallbackInfoReturnable<PathPoint> info) {
        if (!((INodeBreakable) this).canBreakBlocks())
            return;
        PathPoint node = PathFindingUtils.floatingPathPointModifier(this.entity, this.blockaccess, x, y, z,
                aabb -> this.collisionBreakableCache.computeIfAbsent(aabb, object -> !PathFindingUtils.noCollision(this.blockaccess, this.entity, aabb)),
                p -> super.openPoint(p.getX(), p.getY(), p.getZ()));
        if (node != null) {
            info.setReturnValue(node);
            info.cancel();
        }
    }
}
