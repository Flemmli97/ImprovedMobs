package com.flemmli97.improvedmobs.mixin.pathfinding;

import com.flemmli97.improvedmobs.utils.PathFindingUtils;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PathNavigator.class)
public abstract class PathNavigationMixin {

    @Shadow
    protected MobEntity entity;
    @Shadow
    protected Path currentPath;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/controller/MovementController;setMoveTo(DDDD)V"), index = 1)
    private double noJumpBreakable(double y) {
        Vector3d vector3d2 = this.currentPath.getPosition(this.entity);
        if (PathFindingUtils.canBreak(new BlockPos(vector3d2), this.entity)) {
            return vector3d2.y - 0.5;
        }
        return y;
    }
}
