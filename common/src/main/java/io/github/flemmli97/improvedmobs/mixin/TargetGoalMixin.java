package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.SensingExt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin {

    @Shadow
    protected Mob mob;

    @Inject(method = "canContinueToUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getSensing()Lnet/minecraft/world/entity/ai/sensing/Sensing;"))
    private void onContinueUse(CallbackInfoReturnable<Boolean> info) {
        ((SensingExt) this.mob.getSensing()).doLineOfSightExt();
    }
}
