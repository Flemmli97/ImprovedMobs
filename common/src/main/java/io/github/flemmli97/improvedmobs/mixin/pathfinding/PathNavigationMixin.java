package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.improvedmobs.mixinhelper.PathNavigateData;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin implements PathNavigateData {

    @Unique
    private boolean improvedMobs$isMining;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/MoveControl;setWantedPosition(DDDD)V"))
    private void onSetWanted(MoveControl instance, double x, double y, double z, double speed, Operation<Void> original) {
        if (this.improvedMobs$isMining) {
            return;
        }
        original.call(instance, x, y, z, speed);
    }

    @Override
    public void improvedMobs$setMining(boolean mining) {
        this.improvedMobs$isMining = mining;
    }
}
