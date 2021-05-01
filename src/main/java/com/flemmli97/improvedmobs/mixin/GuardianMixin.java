package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GuardianMixin {

    @Inject(method = "canBeRiddenInWater", at = @At(value = "HEAD"), cancellable = true)
    private void ridable(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this instanceof GuardianEntity && ((GuardianEntity) (Object) this).getPersistentData().getBoolean(ImprovedMobs.waterRiding)) {
            info.setReturnValue(true);
            info.cancel();
        }

    }
}
