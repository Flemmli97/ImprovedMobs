package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.utils.PathFindingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public class PathNavigationMixin {

    @Shadow
    protected Mob mob;

    @Inject(method = "getGroundY", at = @At(value = "HEAD"), cancellable = true)
    private void noJumpBreakable(Vec3 pos, CallbackInfoReturnable<Double> info) {
        if (PathFindingUtils.canBreak(new BlockPos(pos), this.mob)) {
            info.setReturnValue(pos.y - 0.5);
            info.cancel();
        }
    }
}
