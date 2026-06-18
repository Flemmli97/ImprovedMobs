package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.common.utils.PathFindingUtils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import io.github.flemmli97.improvedmobs.mixinhelper.PathfindingContextExt;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FlyNodeEvaluator.class)
public abstract class FlyNodeMixin extends NodeEvaluator {

    @Inject(method = "findAcceptedNode", at = @At(value = "HEAD"))
    private void onFindingAcceptedNode(int x, int y, int z, CallbackInfoReturnable<Node> info) {
        ((PathfindingContextExt) this.currentContext).improvedMobs$setPathHandler(new PathfindingContextExt.AdditionalPathTypeHandler(Integer.MIN_VALUE,
                pos -> PathFindingUtils.BREAKABLE.equals(((NodeExtension) this).improvedMobs$pathTypeOf(pos, null, PathFindingUtils.BREAKABLE))));
    }
}
