package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GuardianMixin {

    @Inject(method = "rideableUnderWater", at = @At(value = "HEAD"), cancellable = true)
    private void ridable(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this instanceof Guardian && EntityFlags.get((Guardian) (Object) this).rideSummon) {
            info.setReturnValue(true);
            info.cancel();
        }
    }
}
