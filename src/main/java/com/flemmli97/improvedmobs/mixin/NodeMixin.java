package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.utils.NodeUtils;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkAndSwimNodeProcessor;
import net.minecraft.pathfinding.WalkNodeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = {WalkNodeProcessor.class, WalkAndSwimNodeProcessor.class, FlyingNodeProcessor.class}, priority = 500)
public abstract class NodeMixin extends NodeProcessor {

    @Inject(method = "func_222859_a", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void addAdditionalPoints(PathPoint[] points, PathPoint point, CallbackInfoReturnable<Integer> info, int i) {
        if (this.entity != null) {
            info.setReturnValue(NodeUtils.collectAdditionalNodes(this, this.blockaccess, this.entity, points, point, i, this::openPoint));
            info.cancel();
        }
    }
}
