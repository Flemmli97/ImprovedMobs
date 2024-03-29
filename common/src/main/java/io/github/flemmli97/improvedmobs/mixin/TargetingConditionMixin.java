package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.SensingExt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionMixin {

    @Inject(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getSensing()Lnet/minecraft/world/entity/ai/sensing/Sensing;"))
    private void onTest(@Nullable LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> info) {
        ((SensingExt) ((Mob) attacker).getSensing()).doLineOfSightExt();
    }
}
